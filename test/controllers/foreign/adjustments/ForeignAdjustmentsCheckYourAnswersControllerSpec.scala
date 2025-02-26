/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.foreign.adjustments

import audit.AuditService
import base.SpecBase
import controllers.foreign.adjustments.routes.{ForeignAdjustmentsCheckYourAnswersController, ForeignAdjustmentsCompleteController}
import controllers.routes
import models.JourneyPath.ForeignPropertyAdjustments
import models.{BalancingCharge, JourneyContext, UnusedLossesPreviousYears, UserAnswers}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import pages.foreign.ClaimPropertyIncomeAllowanceOrExpensesPage
import pages.foreign.adjustments._
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.{BusinessService, PropertySubmissionService}
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.adjustments.ForeignAdjustmentsCheckYourAnswersView

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ForeignAdjustmentsCheckYourAnswersControllerSpec extends SpecBase {

  val countryCode: String = "USA"
  val taxYear: Int = LocalDate.now.getYear


  "ForeignAdjustmentsCheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(GET, ForeignAdjustmentsCheckYourAnswersController.onPageLoad(taxYear, countryCode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignAdjustmentsCheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, taxYear, countryCode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(GET, ForeignAdjustmentsCheckYourAnswersController.onPageLoad(taxYear, countryCode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, ForeignAdjustmentsCheckYourAnswersController.onSubmit(taxYear, countryCode).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the POST for onSubmit() should redirect to the correct URL" in {
      val userAnswers = UserAnswers("foreign-property-adjustments-user-answers")
        .set(ClaimPropertyIncomeAllowanceOrExpensesPage, true)
        .flatMap(_.set(ForeignPrivateUseAdjustmentPage(countryCode), BigDecimal(25)))
        .flatMap(_.set(ForeignAdjustmentsSectionAddCountryCode(countryCode), countryCode))
        .flatMap(
          _.set(
            ForeignBalancingChargePage(countryCode),
            BalancingCharge(balancingChargeYesNo = true, balancingChargeAmount = Some(BigDecimal(50)))
          )
        )
        .flatMap(_.set(PropertyIncomeAllowanceClaimPage(countryCode), BigDecimal(75)))
        .flatMap(
          _.set(
            ForeignUnusedLossesPreviousYearsPage(countryCode),
            UnusedLossesPreviousYears(unusedLossesPreviousYearsYesNo = false, unusedLossesPreviousYearsAmount = None)
          )
        )
        .toOption
      val context =
        JourneyContext(taxYear = taxYear, mtditid = "mtditid", nino = "nino", journeyPath = ForeignPropertyAdjustments)

      when(
        propertySubmissionService
          .saveForeignPropertyJourneyAnswers(ArgumentMatchers.eq(context), any)(
            any(),
            any()
          )
      ) thenReturn Future(Right())

      when(businessService.getForeignPropertyDetails(any(), any())(any())) thenReturn Future(
        Right(Some(foreignPropertyDetails))
      )

      val application = applicationBuilder(userAnswers = userAnswers, isAgent = true)
        .overrides(bind[PropertySubmissionService].toInstance(propertySubmissionService))
        .overrides(bind[BusinessService].toInstance(businessService))
        .overrides(bind[AuditService].toInstance(audit))
        .build()

      def onwardRoute: Call = ForeignAdjustmentsCompleteController.onPageLoad(taxYear, countryCode)

      running(application) {
        val request = FakeRequest(POST, ForeignAdjustmentsCheckYourAnswersController.onSubmit(taxYear, countryCode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(audit, times(1)).sendAuditEvent(any())(any(), any())
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }
}
