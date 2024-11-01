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
import controllers.routes
import pages.foreign.{Country, SelectIncomeCountryPage}
import forms.foreign.CountriesRentedPropertyFormProvider
import models.{UserAnswers, NormalMode}
import navigation.{Navigator, FakeNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.CountriesRentedPropertyPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import controllers.foreign.routes
import play.api.Application
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.foreign.CountriesRentedPropertySummary
import viewmodels.govuk.summarylist._
import views.html.foreign.CountriesRentedPropertyView

import scala.concurrent.Future

class CountriesRentedPropertyControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new CountriesRentedPropertyFormProvider()
  val form = formProvider()

  val taxYear = 2024
  lazy val countriesRentedPropertyRoute = controllers.foreign.routes.CountriesRentedPropertyController.onPageLoad(taxYear, NormalMode).url
  val list: SummaryList = SummaryListViewModel(Seq.empty)
  val agent = "agent"

  def onwardRoute = Call("GET", s"$countriesRentedPropertyRoute")

  "CountriesRentedProperty Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, countriesRentedPropertyRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CountriesRentedPropertyView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, list, taxYear, agent, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, countriesRentedPropertyRoute)
            .withFormUrlEncodedBody(("countriesRentedPropertyYesOrNo", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, countriesRentedPropertyRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[CountriesRentedPropertyView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, list, taxYear, agent, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, countriesRentedPropertyRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, countriesRentedPropertyRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the list should display the country" in {

      val userAnswers: UserAnswers =
        UserAnswers("countries-rented-property-user-answers")
          .set(
            page = SelectIncomeCountryPage,
            value = Country ("Greece", "GRC"
            )
              )
          .get

      val application: Application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()
      running(application){

        val result = CountriesRentedPropertySummary.row(taxYear, userAnswers)(messages(application))
          .get.key.content.toString.trim.substring(12).dropRight(1)

        result mustEqual "Greece"
      }


    }
  }
}
