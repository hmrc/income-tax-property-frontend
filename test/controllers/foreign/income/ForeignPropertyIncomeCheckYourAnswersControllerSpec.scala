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

package controllers.foreign.income

import base.SpecBase
import controllers.routes
import models.{UserAnswers, ReversePremiumsReceived}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import controllers.foreign.income.routes._
import pages.foreign.ForeignYearLeaseAmountPage
import pages.foreign.income.{ForeignOtherIncomeFromPropertyPage, ForeignPropertyRentalIncomePage, ForeignReversePremiumsReceivedPage, PremiumsGrantLeaseYNPage}

import viewmodels.govuk.SummaryListFluency
import views.html.foreign.income.ForeignPropertyIncomeCheckYourAnswersView

import java.time.LocalDate

class ForeignPropertyIncomeCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  val countryCode: String = "USA"
  val taxYear: Int = LocalDate.now.getYear
  def onwardRoute = ForeignIncomeSectionCompleteController.onPageLoad(taxYear, countryCode)
  val controller = ForeignPropertyIncomeCheckYourAnswersController

  "ForeignPropertyIncomeCheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, controller.onPageLoad(taxYear, countryCode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignPropertyIncomeCheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, taxYear, countryCode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, controller.onPageLoad(taxYear, countryCode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(POST, controller.onSubmit(taxYear, countryCode).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the POST for onSubmit() should redirect to the correct URL" in {
      val userAnswers = UserAnswers("foreign-property-income-user-answers")
        .set(ForeignPropertyRentalIncomePage(countryCode), BigDecimal(67))
        .flatMap(_.set(PremiumsGrantLeaseYNPage(countryCode), true))
        .flatMap(_.set(ForeignYearLeaseAmountPage(countryCode), 24))
        .flatMap(_.set(ForeignReversePremiumsReceivedPage(countryCode), ReversePremiumsReceived(true, Some(BigDecimal(121)))))
        .flatMap(_.set(ForeignOtherIncomeFromPropertyPage(countryCode), BigDecimal(12)))
        .toOption

      val application = applicationBuilder(userAnswers = userAnswers, isAgent = true)
        .build()

      running(application) {
        val request = FakeRequest(POST, controller.onSubmit(taxYear, countryCode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }
}
