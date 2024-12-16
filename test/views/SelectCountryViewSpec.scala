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
import forms.ukandforeignproperty.SelectCountryFormProvider
import models.{Index, Mode, NormalMode}
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem
import views.html.ukandforeignproperty.SelectCountryView

import java.time.LocalDate

class SelectCountryViewSpec extends SpecBase with Matchers {

  val application: Application = new GuiceApplicationBuilder().build()

  implicit val messages: Messages = application.injector.instanceOf[MessagesApi].preferred(Seq.empty)

  val view: SelectCountryView = application.injector.instanceOf[SelectCountryView]
  private val taxYear    = LocalDate.now.getYear

  def createView(form: Form[_], taxYear: Int, index: Index, userType: String, mode: Mode, countries: Seq[SelectItem])(implicit request: Request[_]): Html = {
    view(form, taxYear, index, userType, mode, countries)(request, messages)
  }

  "SelectCountryView" - {

    "render correctly" in {
      val userType = "individual"

      val formProvider = new SelectCountryFormProvider()
      val form = formProvider(userType)

      val index = Index(0)
      val mode = NormalMode
      val countries = Seq(
        SelectItem(Some("UK"), "United Kingdom"),
        SelectItem(Some("FR"), "France")
      )

      val request = FakeRequest(GET, "/")
      val result = createView(form, taxYear, index, userType, mode, countries)(request)

      form.hasErrors mustBe false

      contentAsString(result) must include(messages(s"selectCountry.title.$userType"))
      contentAsString(result) must include(messages("income.from.otherCountry.info"))
      contentAsString(result) must include(messages("selectIncomeCountry.legend"))
      contentAsString(result) must include(messages("start.typing.country.hint"))
      contentAsString(result) must include(messages("site.continue"))
    }

    "show error message when form is invalid" in {
      val userType = "individual"

      val formProvider = new SelectCountryFormProvider()
      val form = formProvider(userType).bind(Map("country" -> ""))

      val index = Index(0)
      val mode = NormalMode
      val countries = Seq(
        SelectItem(Some("UK"), "United Kingdom"),
        SelectItem(Some("FR"), "France")
      )

      val request = FakeRequest(GET, "/")
      val result = createView(form, taxYear, index, userType, mode, countries)(request)

      contentAsString(result) must include(messages(s"selectCountry.error.required.$userType"))
    }

    "show error message for invalid country selection" in {
      val userType = "individual"

      val formProvider = new SelectCountryFormProvider()
      val form = formProvider(userType).bind(Map("country" -> "InvalidCountry"))

      form.hasErrors mustBe true
      form.errors.head.message mustBe "error.select.validCountry"
    }
  }

}
