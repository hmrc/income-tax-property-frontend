/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.ukrentaroom.allowances

import audit.{AuditService, RentARoomAllowance}
import base.SpecBase
import models.{RentARoom, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.PageConstants.allowancesPath
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.PropertySubmissionService
import viewmodels.govuk.SummaryListFluency
import views.html.ukrentaroom.allowances.RaRAllowancesCheckYourAnswersView

import java.time.LocalDate
import scala.concurrent.Future

class RaRAllowancesCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  val taxYear: Int = LocalDate.now.getYear

  val onwardRoute: Call =
    Call("GET", s"/update-and-submit-income-tax-return/property/$taxYear/rent-a-room/allowances/complete-yes-no")
  val annualInvestmentAllowanceSummaryValue = 100
  val annualInvestmentAllowanceSummary: BigDecimal = BigDecimal.valueOf(annualInvestmentAllowanceSummaryValue)

  val otherCapitalAllowanceValue = 700
  val otherCapitalAllowance: BigDecimal = BigDecimal.valueOf(otherCapitalAllowanceValue)

  val replacementOfDomesticGoodsValue = 600
  val replacementOfDomesticGoods: BigDecimal = BigDecimal.valueOf(replacementOfDomesticGoodsValue)

  val businessPremisesRenovationValue = 500
  val businessPremisesRenovation: BigDecimal = BigDecimal.valueOf(businessPremisesRenovationValue)

  val zeroEmissionCarAllowanceValue = 300
  val zeroEmissionCarAllowance: BigDecimal = BigDecimal.valueOf(zeroEmissionCarAllowanceValue)

  val zeroEmissionGoodsVehicleAllowanceValue = 800
  val zeroEmissionGoodsVehicleAllowance: BigDecimal = BigDecimal.valueOf(zeroEmissionGoodsVehicleAllowanceValue)

  "RaRAllowancesCheckYourAnswer Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()
      val list = SummaryListViewModel(Seq.empty)

      running(application) {

        val request = FakeRequest(GET, routes.RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RaRAllowancesCheckYourAnswersView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, taxYear)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, routes.RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the POST for onSubmit() should redirect to the Have you finished URL" in {

      val userAnswersJson = Json.obj(
        (
          allowancesPath(RentARoom),
          Json.toJson(
            RentARoomAllowance(
              capitalAllowancesForACar = None,
              annualInvestmentAllowance = Some(annualInvestmentAllowanceSummary),
              zeroEmissionCarAllowance = Some(zeroEmissionCarAllowance),
              replacementOfDomesticGoodsAllowance = Some(replacementOfDomesticGoods),
              otherCapitalAllowance = Some(otherCapitalAllowance),
              zeroEmissionGoodsVehicleAllowance = Some(zeroEmissionGoodsVehicleAllowance),
              businessPremisesRenovationAllowance = Some(businessPremisesRenovation)
            )
          )
        )
      )

      val userAnswers = Option(UserAnswers("id", userAnswersJson))

      // mocks
      val propertySubmissionService = mock[PropertySubmissionService]
      val auditService = mock[AuditService]

      when(propertySubmissionService.saveJourneyAnswers(any(), any())(any(), any())) thenReturn Future.successful(
        Right(())
      )

      val application = applicationBuilder(userAnswers = userAnswers, isAgent = true)
        .overrides(bind[PropertySubmissionService].toInstance(propertySubmissionService))
        .overrides(bind[AuditService].toInstance(auditService))
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.RaRAllowancesCheckYourAnswersController.onSubmit(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(auditService, times(1)).sendRentARoomAuditEvent(any())(any(), any())
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }
}
