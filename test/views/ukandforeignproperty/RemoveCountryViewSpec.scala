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

package views.ukandforeignproperty

import forms.ukandforeignproperty.{RemoveCountryFormProvider, TotalPropertyIncomeFormProvider}
import models.{Index, NormalMode, TotalPropertyIncome}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import pages.foreign.Country
import play.api.data.Form
import views.ViewSpecBase
import views.html.ukandforeignproperty.RemoveCountryView

class RemoveCountryViewSpec extends ViewSpecBase {

  val testTaxYear = 2024
  val formProvider = new RemoveCountryFormProvider()
  val form: Form[Boolean] = formProvider()
  val testIndex: Index = Index(1)
  val testCountry: Country = Country("France", "FR")

  val view: RemoveCountryView = app.injector.instanceOf[RemoveCountryView]
  val doc: Document = Jsoup.parse(view(form, testTaxYear, NormalMode, testIndex, testCountry).body)

  object ExpectedMessages {
    val h1 = "Do you want to remove France?"
    val backLink = "Back"
    val continue = "Continue"
    val doNotRemoveButton = "Don’t remove"
  }

  "RemoveCountryView" must {

    "have the correct h1 title" in {
      doc.select("h1").text mustBe ExpectedMessages.h1
    }

    "have a back link" in {
      doc.select(".govuk-back-link").text mustBe ExpectedMessages.backLink
    }

    "have a 'continue' button" in {
      doc.select("main .govuk-button").first().text() mustBe ExpectedMessages.continue
    }
  }
}
