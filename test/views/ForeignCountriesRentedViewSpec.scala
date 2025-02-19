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

package views

import base.SpecBase
import play.api.test.Helpers._
import play.api.test.FakeRequest
import models.{CheckMode, Index, Mode, NormalMode, UserAnswers}
import org.scalatest.matchers.must.Matchers
import pages.foreign.{Country, SelectIncomeCountryPage}
import play.api.Application
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.html.components.GovukTag
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.checkAnswers.foreign.CountriesRentedPropertySummary
import viewmodels.checkAnswers.ukandforeignproperty.SelectCountrySummary
import views.html.templates.Layout
import views.html.ukandforeignproperty.ForeignCountriesRentedView

import java.time.LocalDate

class ForeignCountriesRentedViewSpec extends SpecBase with Matchers {

  val application: Application = new GuiceApplicationBuilder().build()

  implicit val messages: Messages = application.injector.instanceOf[MessagesApi].preferred(Seq.empty)

  val layout: Layout     = application.injector.instanceOf[Layout]
  val govukTag: GovukTag = application.injector.instanceOf[GovukTag]
  val view: ForeignCountriesRentedView = application.injector.instanceOf[ForeignCountriesRentedView]
  private val taxYear    = LocalDate.now.getYear

  def createView(form: Form[_], list: SummaryList, individualOrAgent: String, mode: Mode)(implicit request: Request[_]): Html = {
    view(form, list, taxYear, individualOrAgent, mode)(request, messages)
  }

  "ForeignCountriesRentedView" - {

    "render correctly" in {
      val form = Form("addAnotherCountry" -> boolean)
      val list = SummaryList(Seq.empty)
      val mode = NormalMode
      val individualOrAgent = "individual"

      val request = FakeRequest(GET, "/")
      val result = createView(form, list, individualOrAgent, mode)(request)

      contentAsString(result) must include(messages("foreignCountriesRentList.title.individual"))
      contentAsString(result) must include(messages("foreignCountriesRentList.heading.individual"))
      contentAsString(result) must include(messages("site.saveAndContinue"))
    }

    "show error summary when form has errors" in {
      val form = Form("foreignCountriesRentedPropertyYesOrNo" -> boolean).withError("value", "selectIncomeCountry.error.required.individual")
      val list = SummaryList(Seq.empty)
      val mode = NormalMode
      val individualOrAgent = "individual"

      val request = FakeRequest(GET, "/")
      val result = createView(form, list, individualOrAgent, mode)(request)

      contentAsString(result) must include(messages("selectIncomeCountry.error.required.individual"))
    }

    "render the summary list with countries" in {
      val form = Form("addAnotherCountry" -> boolean)
      val country = Country("France", "FR")
      val userAnswers = UserAnswers("id").set(SelectIncomeCountryPage(0), country).success.value
      val countryFromData = userAnswers.get(SelectIncomeCountryPage(0))
      match {
        case Some(country) => country.name
        case _ => ""
      }
      val rows = Seq(
        SelectCountrySummary.row(taxYear, Index(0), countryFromData)
      )
      val list = SummaryList(rows)
      val mode = NormalMode
      val individualOrAgent = "individual"

      val request = FakeRequest(GET, "/")
      val result = createView(form, list, individualOrAgent, mode)(request)

      contentAsString(result) must include("France")
      contentAsString(result) must include(messages("countriesRentedProperty.staticContent"))
      contentAsString(result) must include(messages("site.change"))
      contentAsString(result) must include(messages("site.remove"))

      contentAsString(result) must include("Do you want to add another country?")
      contentAsString(result) must include("Foreign countries where you rented out property")

      contentAsString(result) must include("Yes")
      contentAsString(result) must include("No")
      contentAsString(result) must include("Change")
      contentAsString(result) must include("Remove")
      contentAsString(result) must include("Save and continue")
    }
  }

  "SelectCountrySummary.row" - {

    "return a summary list row for a given country" in {
      val result: SummaryListRow = SelectCountrySummary.row(2024, Index(1), "France")

      result.key.content.asHtml.toString must include("France")

      val actions = result.actions.head.items
      actions.head.href must include(controllers.ukandforeignproperty.routes.SelectCountryController.onPageLoad(2024, Index(1), CheckMode).url)
      actions.head.content.asHtml.toString must include(messages("site.change"))
      actions(1).href must include(controllers.ukandforeignproperty.routes.RemoveCountryController.onPageLoad(2024, Index(1), CheckMode).url)
      actions(1).content.asHtml.toString must include(messages("site.remove"))
    }

  }
}