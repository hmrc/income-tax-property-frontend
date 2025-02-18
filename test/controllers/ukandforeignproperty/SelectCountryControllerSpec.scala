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
import forms.ukandforeignproperty.SelectCountryFormProvider
import models.{CheckMode, Index, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.Country
import pages.ukandforeignproperty.SelectCountryPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import service.CountryNamesDataSource.countrySelectItems
import service.SessionService
import views.html.ukandforeignproperty.SelectCountryView

import scala.concurrent.Future

class SelectCountryControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = routes.ForeignCountriesRentedController.onPageLoad(taxYear, NormalMode)
  val taxYear = 2025
  val index: Index = Index(0)
  val country: Country = Country(name = "France", code = "FRA")
  val formProvider = new SelectCountryFormProvider()

  lazy val selectCountryRoute: String = routes.SelectCountryController.onPageLoad(taxYear, index, NormalMode).url

  "SelectCountry Controller" - {

    "GET" - {
      Seq(("individual", false), ("agent", true)) foreach { case (userType, isAgent) =>
        val form: Form[String] = formProvider(userType, Nil, Index(1))
        s"must return OK and the correct view for a GET for $userType" in {
          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = isAgent).build()

          running(application) {
            val controller = application.injector.instanceOf[SelectCountryController]
            val request = FakeRequest(GET, selectCountryRoute)

            val result = controller.onPageLoad(taxYear, index, NormalMode)(request)

            val view = application.injector.instanceOf[SelectCountryView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, taxYear, index, userType, NormalMode, countrySelectItems("en"))(
              request,
              messages(application)
            ).toString
          }
        }

        s"must populate the view correctly on a GET when the question has previously been answered for $userType" in {
          val userAnswers = UserAnswers(userAnswersId).set(SelectCountryPage, List(country)).success.value
          val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = isAgent).build()

          running(application) {
            val controller = application.injector.instanceOf[SelectCountryController]
            val request = FakeRequest(GET, selectCountryRoute)
            val view = application.injector.instanceOf[SelectCountryView]

            val result = controller.onPageLoad(taxYear, index, NormalMode)(request)

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(
              form.fill("France"),
              taxYear,
              index,
              userType,
              NormalMode,
              countrySelectItems("en")
            )(
              request,
              messages(application)
            ).toString
          }
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgent = true).build()

        running(application) {
          val controller = application.injector.instanceOf[SelectCountryController]
          val request = FakeRequest(GET, selectCountryRoute)

          val result = controller.onPageLoad(taxYear, index, NormalMode)(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "POST" - {
      "must redirect to the next page when valid data is submitted" in {
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false)
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val controller = application.injector.instanceOf[SelectCountryController]
          val request =
            FakeRequest(POST, selectCountryRoute)
              .withFormUrlEncodedBody(("country", "IND"))

          val result = controller.onSubmit(taxYear, index, NormalMode)(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      Seq(("individual", false), ("agent", true)) foreach { case (userType, isAgent) =>
        val form: Form[String] = formProvider(userType, Nil, Index(1))
        s"must return a Bad Request and errors when invalid data is submitted for $userType" in {
          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = isAgent).build()

          running(application) {
            val controller = application.injector.instanceOf[SelectCountryController]
            val request = FakeRequest(POST, selectCountryRoute).withFormUrlEncodedBody(("country", ""))
            val boundForm = form.bind(Map("country" -> ""))
            val view = application.injector.instanceOf[SelectCountryView]

            val result = controller.onSubmit(taxYear, index, NormalMode)(request)

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, taxYear, index, userType, NormalMode, countrySelectItems("en"))(
              request,
              messages(application)
            ).toString
          }
        }

        s"must update the $userType's answer and redirect to ForeignPropertiesRentedController in CheckMode" in {
          val userAnswers = emptyUserAnswers.set(SelectCountryPage, List(Country("Spain", "ESP"))).success.value
          val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = isAgent).build()

          running(application) {
            val controller = application.injector.instanceOf[SelectCountryController]
            val sessionService = application.injector.instanceOf[SessionService]
            val request = FakeRequest(POST, selectCountryRoute).withFormUrlEncodedBody(("country", "FRA"))

            val result = controller.onSubmit(taxYear, Index(1), CheckMode)(request)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.ukandforeignproperty.routes.ForeignCountriesRentedController.onPageLoad(taxYear, CheckMode).url
            val userAnswers = await(sessionService.get(userAnswersId))

            userAnswers.flatMap(_.get(SelectCountryPage)) mustBe Some(List(Country("France", "FRA")))
          }
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {
        val application = applicationBuilder(userAnswers = None, isAgent = true).build()

        running(application) {
          val controller = application.injector.instanceOf[SelectCountryController]
          val request = FakeRequest(POST, selectCountryRoute).withFormUrlEncodedBody(("country", "answer"))
          val result = controller.onSubmit(taxYear, index, NormalMode)(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
