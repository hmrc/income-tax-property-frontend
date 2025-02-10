/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.allowances

import audit.{AuditService, RentalsAllowance}
import base.SpecBase
import models.IncomeSourcePropertyType.UKProperty
import models.backend.PropertyDetails
import models.UserAnswers
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.doNothing
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.{BusinessService, PropertySubmissionService}
import viewmodels.govuk.SummaryListFluency
import views.html.allowances.AllowancesCheckYourAnswersView

import java.time.LocalDate
import scala.concurrent.Future

class AllowancesCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  val taxYear = LocalDate.now.getYear

  val onwardRoute: Call = Call(
    "GET",
    s"/update-and-submit-income-tax-return/property/$taxYear/rentals/allowances/complete-yes-no"
  )
  val annualInvestmentAllowanceSummaryValue = 100
  val annualInvestmentAllowanceSummary = BigDecimal.valueOf(annualInvestmentAllowanceSummaryValue)

  val otherCapitalAllowanceValue = 700
  val otherCapitalAllowance = BigDecimal.valueOf(otherCapitalAllowanceValue)

  val replacementOfDomesticGoodsValue = 600
  val replacementOfDomesticGoods = BigDecimal.valueOf(replacementOfDomesticGoodsValue)

  val businessPremisesRenovationValue = 500
  val businessPremisesRenovation = BigDecimal.valueOf(businessPremisesRenovationValue)

  val zeroEmissionGoodsVehicleAllowanceValue = 400
  val zeroEmissionGoodsVehicleAllowance = BigDecimal.valueOf(zeroEmissionGoodsVehicleAllowanceValue)

  val zeroEmissionCarAllowanceValue = 300
  val zeroEmissionCarAllowance = BigDecimal.valueOf(zeroEmissionCarAllowanceValue)


  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), true).build()

      running(application) {
        val request = FakeRequest(GET, routes.AllowancesCheckYourAnswersController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AllowancesCheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, taxYear)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, routes.AllowancesCheckYourAnswersController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the POST for onSubmit() should redirect to the correct URL" in {

      val userAnswers = UserAnswers("allowances-user-answers")
        .set(
          RentalsAllowance,
          RentalsAllowance(
            capitalAllowancesForACar = None,
            annualInvestmentAllowance = Some(annualInvestmentAllowanceSummary),
            zeroEmissionCarAllowance = Some(zeroEmissionCarAllowance),
            zeroEmissionGoodsVehicleAllowance = Some(zeroEmissionGoodsVehicleAllowance),
            businessPremisesRenovationAllowance = Some(businessPremisesRenovation),
            replacementOfDomesticGoodsAllowance = Some(replacementOfDomesticGoods),
            otherCapitalAllowance = Some(otherCapitalAllowance)
          )
        )
        .toOption

      val propertyDetails = PropertyDetails(
        Some(UKProperty.toString),
        Some(LocalDate.of(taxYear, 1, 2)),
        accrualsOrCash = Some(false),
        "incomeSourceId"
      )

      // mocks
      val propertySubmissionService = mock[PropertySubmissionService]
      val businessService = mock[BusinessService]
      val auditService = mock[AuditService]

      // when
      when(businessService.getUkPropertyDetails(anyString(), anyString())(any())) thenReturn Future.successful(
        Right(Some(propertyDetails))
      )
      when(propertySubmissionService.saveJourneyAnswers(any(), any())(any(), any())) thenReturn Future.successful(
        Right(())
      )
      doNothing().when(auditService).sendRentalsAuditEvent(any())(any(), any())

      val application = applicationBuilder(userAnswers = userAnswers, isAgent = true)
        .overrides(bind[PropertySubmissionService].toInstance(propertySubmissionService))
        .overrides(bind[BusinessService].toInstance(businessService))
        .overrides(bind[AuditService].toInstance(auditService))
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.AllowancesCheckYourAnswersController.onSubmit(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }

}
