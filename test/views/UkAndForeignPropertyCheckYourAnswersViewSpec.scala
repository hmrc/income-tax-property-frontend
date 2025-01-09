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

package views

import base.SpecBase
import models.{ReportIncome, TotalPropertyIncome, UserAnswers}
import org.scalatest.matchers.must.Matchers
import pages.ukandforeignproperty.{ReportIncomePage, TotalPropertyIncomePage}
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.ReportIncomeSummary
import viewmodels.checkAnswers.ukandforeignproperty.TotalPropertyIncomeSummary
import views.html.ukandforeignproperty.UkAndForeignPropertyCheckYourAnswersView

import java.time.LocalDate

class UkAndForeignPropertyCheckYourAnswersViewSpec extends SpecBase with Matchers {

  val application: Application = new GuiceApplicationBuilder().build()

  implicit val messages: Messages = application.injector.instanceOf[MessagesApi].preferred(Seq.empty)

  val view: UkAndForeignPropertyCheckYourAnswersView = application.injector.instanceOf[UkAndForeignPropertyCheckYourAnswersView]

  def createView(list: SummaryList,  taxYear: Int)(implicit request: Request[_]): Html = {
    view(list, taxYear)(request, messages)
  }

  "UkAndForeignPropertyCheckYourAnswersView" - {

    "render correctly" in {
      val list = SummaryList(Seq.empty)
      val taxYear: Int = LocalDate.now.getYear

      val request = FakeRequest(GET, "/")
      val result = createView(list, taxYear)(request)

      contentAsString(result) must include(messages("checkYourAnswers.title"))
      contentAsString(result) must include(messages("checkYourAnswers.heading"))
      contentAsString(result) must include(messages("site.saveAndContinue"))
    }

    "render the summary list with TotalPropertyIncomeSummary and ReportIncomeSummary" in {
      val taxYear: Int = LocalDate.now.getYear
      val individualOrAgent = "individual"

      val userAnswers = UserAnswers("id")
        .set(TotalPropertyIncomePage, TotalPropertyIncome.LessThan).success.value
        .set(ReportIncomePage, ReportIncome.DoNoWantToReport).success.value
      val rows = Seq(
        TotalPropertyIncomeSummary.row(taxYear, userAnswers).get,
        ReportIncomeSummary.row(taxYear, individualOrAgent, userAnswers).get
      )
      val list = SummaryList(rows)

      val request = FakeRequest(GET, "/")
      val result = createView(list, taxYear)(request)

      contentAsString(result) must include(messages("site.change"))

      contentAsString(result) must include("Less than £1,000")
      contentAsString(result) must include("No, I do not want to report my property income")

      contentAsString(result) must include("Check your answers")
      contentAsString(result) must include("How much total income did you get from all of your UK and foreign properties?")
      contentAsString(result) must include("Do you want to report your property income?")
      contentAsString(result) must include("Save and continue")
    }
  }

}
