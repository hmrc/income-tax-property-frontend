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

package controllers.ukrentaroom.adjustments

import audit.AuditService
import base.SpecBase
import models.{BalancingCharge, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.{times, verify}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.ukrentaroom.adjustments.{RaRBalancingChargePage, RaRUnusedResidentialCostsPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.PropertySubmissionService
import viewmodels.govuk.SummaryListFluency
import views.html.ukrentaroom.adjustments.RaRAdjustmentsCYAView

import java.time.LocalDate
import scala.concurrent.Future
import scala.util.Try

class RaRAdjustmentsCYAControllerSpec extends SpecBase with SummaryListFluency {

  val taxYear: Int = LocalDate.now.getYear
  val onwardRoute: Call = Call("GET", s"/update-and-submit-income-tax-return/property/$taxYear/rent-a-room/adjustments/complete-yes-no")

  val raRBalancingChargeValue = 200
  val raRBalancingCharge: BalancingCharge = BalancingCharge(
    balancingChargeYesNo = true,
    balancingChargeAmount = Some(raRBalancingChargeValue)
  )

  "RaRAdjustmentsCYA Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()
      val list = SummaryListViewModel(Seq.empty)
      val taxYear = 2023
      running(application) {

        val request = FakeRequest(GET, routes.RaRAdjustmentsCYAController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RaRAdjustmentsCYAView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, taxYear)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val taxYear = 2023
      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, routes.RaRAdjustmentsCYAController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the POST for onSubmit() should redirect to the correct URL" in {

      val userAnswersTry: Try[UserAnswers] = UserAnswers("adjustments-user-answers")
        .set(RaRBalancingChargePage, raRBalancingCharge)

      val updatedUserAnswers: Option[UserAnswers] = userAnswersTry.toOption.flatMap { ua =>
        ua.set(RaRUnusedResidentialCostsPage, BigDecimal(12)).toOption
      }

      // mocks
      val propertySubmissionService = mock[PropertySubmissionService]
      val audit = mock[AuditService]

      when(propertySubmissionService.saveJourneyAnswers(any(), any())(any(), any())) thenReturn Future.successful(
        Right(())
      )

      val application = applicationBuilder(userAnswers = updatedUserAnswers, isAgent = true)
        .overrides(bind[PropertySubmissionService].toInstance(propertySubmissionService))
        .overrides(bind[AuditService].toInstance(audit))
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.RaRAdjustmentsCYAController.onSubmit(taxYear).url)

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        verify(audit, times(1)).sendRentARoomAuditEvent(any())(any(), any())
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }
}
