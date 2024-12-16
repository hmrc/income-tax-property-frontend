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

package controllers.ukandforeignproperty

import base.SpecBase
import forms.ukandforeignproperty.ForeignCountriesRentedFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.{Country, SelectIncomeCountryPage}
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.http.Status.OK
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.foreign.CountriesRentedPropertySummary
import viewmodels.govuk.all.SummaryListViewModel
import views.html.ukandforeignproperty.ForeignCountriesRentedView

import scala.concurrent.Future

class ForeignCountriesRentedControllerSpec extends SpecBase with MockitoSugar {

  val formProvider: ForeignCountriesRentedFormProvider = new ForeignCountriesRentedFormProvider()
  val form: Form[Boolean] = formProvider()

  val taxYear: Int = 2024
  lazy val foreignCountriesRentedRoute: String =
    controllers.ukandforeignproperty.routes.ForeignCountriesRentedController.onPageLoad(taxYear, mode = NormalMode).url

  val list: SummaryList = SummaryListViewModel(Seq.empty)
  val isAgentMessageKey: String = "individual"
  val index: Int = 0

  def onwardRoute: Call =
    Call("GET", "/update-and-submit-income-tax-return/property")

  def addCountryRoute(): Call =
    Call("GET", "/update-and-submit-income-tax-return/property/2024/uk-foreign-property/select-country/1")

  "ForeignCountriesRented Controller" - {

    "must return OK and correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignCountriesRentedRoute)
        val controller = application.injector.instanceOf[ForeignCountriesRentedController]
        val view = application.injector.instanceOf[ForeignCountriesRentedView]

        val result = controller.onPageLoad(taxYear, NormalMode)(request)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, list, taxYear, isAgentMessageKey, NormalMode)(
          request,
          messages(application)
        ).toString()
      }
    }

    "must redirect to next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(addCountryRoute())),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      val request = FakeRequest(POST, foreignCountriesRentedRoute)
        .withFormUrlEncodedBody(("foreignCountriesRentedPropertyYesOrNo", "true"))

      running(application) {
        val controller = application.injector.instanceOf[ForeignCountriesRentedController]

        val result = controller.onSubmit(taxYear, NormalMode)(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual addCountryRoute().url
      }
    }

    "must redirect to the previous select page when no is selected" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      val request =
        FakeRequest(POST, foreignCountriesRentedRoute)
          .withFormUrlEncodedBody(("foreignCountriesRentedPropertyYesOrNo", "false"))

      running(application) {
        val controller = application.injector.instanceOf[ForeignCountriesRentedController]

        val result = controller.onSubmit(taxYear, NormalMode)(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      val request =
        FakeRequest(POST, foreignCountriesRentedRoute)
          .withFormUrlEncodedBody(("value", "true"))

      running(application) {
        val controller = application.injector.instanceOf[ForeignCountriesRentedController]

        val result = controller.onSubmit(taxYear, NormalMode)(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return BAD_REQUEST when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(POST, foreignCountriesRentedRoute)
          .withFormUrlEncodedBody(("foreignCountriesRentedPropertyYesOrNo", ""))

        val controller = application.injector.instanceOf[ForeignCountriesRentedController]
        val view = application.injector.instanceOf[ForeignCountriesRentedView]

        val boundForm = form.bind(Map("foreignCountriesRentedPropertyYesOrNo" -> ""))
        val result = controller.onSubmit(taxYear, NormalMode)(request)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, list, taxYear, isAgentMessageKey, NormalMode)(
          request,
          messages(application)
        ).toString()
      }
    }

    "must correctly resolve the route to the controller" in {
      val expectedRoute = s"/update-and-submit-income-tax-return/property/$taxYear/uk-foreign-property/select-country/countries-list"

      val actualRoute = controllers.ukandforeignproperty.routes.ForeignCountriesRentedController.onPageLoad(taxYear, NormalMode).url

      actualRoute mustEqual expectedRoute
    }

    "must handle invalid url" in {
      val invalidRoute = "/invalid-route"

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, invalidRoute)

        val result = route(application, request).value

        status(result) mustEqual NOT_FOUND
      }
    }

    "must return OK and the list should display the country" in {

      val userAnswers: UserAnswers =
        UserAnswers("countries-rented-property-user-answers")
          .set(
            page = SelectIncomeCountryPage(index),
            value = Country("Greece", "GRC")
          )
          .get

      val application: Application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()
      running(application) {

        val result = CountriesRentedPropertySummary
          .row(taxYear, index, userAnswers)(messages(application))
          .get
          .key
          .content
          .toString
          .trim
          .substring(12)
          .dropRight(1)

        result mustEqual "Greece"

      }
    }
  }
}
