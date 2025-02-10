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

package controllers.rentalsandrentaroom.adjustments

import audit.AuditService
import base.SpecBase
import models.IncomeSourcePropertyType.UKProperty
import models.backend.PropertyDetails
import models.{BalancingCharge, RenovationAllowanceBalancingCharge, RentalsAndRentARoomAdjustment, UserAnswers}
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{times, verify}
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.{BusinessService, PropertySubmissionService}
import viewmodels.govuk.SummaryListFluency
import views.html.rentalsandrentaroom.adjustments.RentalsAndRentARoomAdjustmentsCheckYourAnswersView

import java.time.LocalDate
import scala.concurrent.Future

class RentalsAndRentARoomAdjustmentsCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  val taxYear = 2024

  "RentalsAndRentARoomAdjustmentsCheckYourAnswersControllerSpec Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()
      val list = SummaryListViewModel(Seq.empty)

      running(application) {

        val request = FakeRequest(
          GET,
          routes.RentalsAndRentARoomAdjustmentsCheckYourAnswersController
            .onPageLoad(taxYear)
            .url
        )

        val result = route(application, request).value

        val view = application.injector.instanceOf[RentalsAndRentARoomAdjustmentsCheckYourAnswersView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, taxYear)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(GET, routes.RentalsAndRentARoomAdjustmentsCheckYourAnswersController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the POST for onSubmit() should redirect to the correct URL" in {

      val userAnswers = UserAnswers("adjustments-user-answers")
        .set(
          RentalsAndRentARoomAdjustment,
          RentalsAndRentARoomAdjustment(
            privateUseAdjustment = 10,
            balancingCharge = BalancingCharge(balancingChargeYesNo = true, Some(10)),
            propertyIncomeAllowance = Some(10),
            renovationAllowanceBalancingCharge =
              RenovationAllowanceBalancingCharge(renovationAllowanceBalancingChargeYesNo = true, Some(10)),
            residentialFinanceCost = 10,
            unusedResidentialFinanceCost = Some(10)
          )
        )
        .toOption

      val propertyDetails = PropertyDetails(
        Some(UKProperty.toString),
        Some(LocalDate.of(taxYear, 1, 2)),
        accrualsOrCash = Some(false),
        "incomeSourceId"
      )

      val propertySubmissionService = mock[PropertySubmissionService]
      val businessService = mock[BusinessService]
      val auditService = mock[AuditService]

      when(businessService.getUkPropertyDetails(anyString(), anyString())(any())) thenReturn Future.successful(
        Right(Some(propertyDetails))
      )
      when(propertySubmissionService.saveJourneyAnswers(any(), any(), any())(any(), any())) thenReturn Future
        .successful(
          Right(())
        )

      val application = applicationBuilder(userAnswers = userAnswers, isAgent = true)
        .overrides(bind[PropertySubmissionService].toInstance(propertySubmissionService))
        .overrides(bind[BusinessService].toInstance(businessService))
        .overrides(bind[AuditService].toInstance(auditService))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.RentalsAndRentARoomAdjustmentsCheckYourAnswersController.onSubmit(taxYear).url)

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        verify(auditService, times(1)).sendAuditEvent(any())(any(), any())
        redirectLocation(result).value mustEqual "/update-and-submit-income-tax-return/property/2024/rentals-rent-a-room/adjustments/complete-yes-no"
      }
    }
  }

}
