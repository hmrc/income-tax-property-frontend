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

package controllers.foreign.allowances

import audit.AuditService
import base.SpecBase
import models.JourneyPath.ForeignPropertyAllowancesPath
import models.{JourneyContext, UserAnswers}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.foreign.allowances._
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.PropertySubmissionService
import viewmodels.govuk.SummaryListFluency
import views.html.foreign.allowances.ForeignAllowancesCheckYourAnswersView

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ForeignAllowancesCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  val taxYear = LocalDate.now.getYear
  val countryCode = "AUS"

  val onwardRoute: Call = Call(
    "GET",
    s"/update-and-submit-income-tax-return/property/$taxYear/foreign-property/allowances/$countryCode/complete-yes-no"
  )

  private val propertySubmissionService = mock[PropertySubmissionService]
  val audit: AuditService = mock[AuditService]

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), true).build()

      running(application) {
        val request =
          FakeRequest(GET, routes.ForeignAllowancesCheckYourAnswersController.onPageLoad(taxYear, countryCode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignAllowancesCheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, taxYear, countryCode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(GET, routes.ForeignAllowancesCheckYourAnswersController.onPageLoad(taxYear, countryCode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(POST, routes.ForeignAllowancesCheckYourAnswersController.onSubmit(taxYear, countryCode).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the POST for onSubmit() should redirect to the correct URL" in {

      val userAnswers = UserAnswers("test").set(ForeignAllowancesCompletePage("AUS"), value = true).get

      val userAnswersForeignPropertyAllowances = userAnswers
        .set(ForeignZeroEmissionCarAllowancePage(countryCode), BigDecimal(56.75))
        .flatMap(_.set(ForeignAllowancesSectionAddCountryCode(countryCode), countryCode))
        .flatMap(_.set(ForeignZeroEmissionGoodsVehiclesPage(countryCode), BigDecimal(67.75)))
        .flatMap(_.set(ForeignReplacementOfDomesticGoodsPage(countryCode), BigDecimal(78.75)))
        .flatMap(_.set(ForeignOtherCapitalAllowancesPage(countryCode), BigDecimal(89.91)))
        .toOption

      val context =
        JourneyContext(
          taxYear = taxYear,
          mtditid = "mtditid",
          nino = "nino",
          journeyPath = ForeignPropertyAllowancesPath
        )

      when(
        propertySubmissionService
          .saveJourneyAnswers(ArgumentMatchers.eq(context), any)(
            any(),
            any()
          )
      ) thenReturn Future(Right())

      val application = applicationBuilder(userAnswers = userAnswersForeignPropertyAllowances, isAgent = true)
        .overrides(bind[PropertySubmissionService].toInstance(propertySubmissionService))
        .overrides(bind[AuditService].toInstance(audit))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.ForeignAllowancesCheckYourAnswersController.onSubmit(taxYear, countryCode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(audit, times(1)).sendAuditEvent(any())(any(), any())
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }

}
