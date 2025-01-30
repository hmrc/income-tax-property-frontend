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

  def createView(list: SummaryList, ukList: Option[SummaryList], foreignList:Option[SummaryList], taxYear: Int)(implicit request: Request[_]): Html = {
    view(list, ukList, foreignList, taxYear)(request, messages)
  }

  "UkAndForeignPropertyCheckYourAnswersView" - {

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
      val ukList = None
      val foreignList = None

      val request = FakeRequest(GET, "/")
      val result = createView(list, ukList, foreignList, taxYear)(request)

      contentAsString(result) mustEqual view(SummaryList(rows), None, None, taxYear)(request, messages(application)).toString
    }
  }

}
