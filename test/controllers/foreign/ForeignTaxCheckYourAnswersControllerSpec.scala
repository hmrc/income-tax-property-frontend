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

package controllers.foreign

import base.SpecBase
import models.{ForeignIncomeTax, UserAnswers}
import pages.foreign.{ClaimForeignTaxCreditReliefPage, ForeignIncomeTaxPage}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency
import views.html.foreign.ForeignTaxCheckYourAnswersView

import java.time.LocalDate

class ForeignTaxCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  val countryCode: String = "USA"
  val taxYear: Int = LocalDate.now.getYear
  def onwardRoute = controllers.foreign.routes.ForeignTaxSectionCompleteController.onPageLoad(taxYear)
  val foreignIncomeTaxAmount: BigDecimal = 234

  "Check Your Answers Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, routes.ForeignTaxCheckYourAnswersController.onPageLoad(taxYear, countryCode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignTaxCheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, taxYear, countryCode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, routes.ForeignTaxCheckYourAnswersController.onPageLoad(taxYear, countryCode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the POST for onSubmit() should redirect to the correct URL" in {
      val userAnswers = UserAnswers("foreign-tax-user-answers")
        .set(
          ForeignIncomeTaxPage(countryCode),
          ForeignIncomeTax(foreignIncomeTaxYesNo = true, Some(foreignIncomeTaxAmount))
        ).flatMap(_.set(ClaimForeignTaxCreditReliefPage(countryCode), true))
        .toOption


      val application = applicationBuilder(userAnswers = userAnswers, isAgent = true)
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.ForeignTaxCheckYourAnswersController.onSubmit(taxYear, countryCode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }
}
