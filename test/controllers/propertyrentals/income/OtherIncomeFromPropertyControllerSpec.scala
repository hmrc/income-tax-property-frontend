/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.propertyrentals.income

import base.SpecBase
import forms.propertyrentals.income.OtherIncomeFromPropertyFormProvider
import models.{NormalMode, Rentals, RentalsRentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import pages.propertyrentals.income.OtherIncomeFromPropertyPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.propertyrentals.income.OtherIncomeFromPropertyView

import java.time.LocalDate
import scala.concurrent.Future

class OtherPropertyRentalIncomeControllerSpec extends SpecBase with MockitoSugar with ScalaFutures {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new OtherIncomeFromPropertyFormProvider()
  val form: Form[BigDecimal] = formProvider("individual")
  val taxYear: Int = LocalDate.now.getYear
  val otherIncomeFromProperty: BigDecimal = BigDecimal(12345)

  lazy val RentalsRoute: String =
    routes.OtherPropertyRentalIncomeController.onPageLoad(taxYear, NormalMode, Rentals).url

  lazy val RentalsAndRaRRoute: String =
    routes.OtherPropertyRentalIncomeController.onPageLoad(taxYear, NormalMode, RentalsRentARoom).url

  "propertyRentalIncome Controller" - {

    "must return OK if the route is valid" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, RentalsRoute)
        val result = route(application, request).value
        status(result) mustEqual OK
      }
    }

    "Rentals journey must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(OtherIncomeFromPropertyPage(Rentals), otherIncomeFromProperty).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, RentalsRoute)
        val view = application.injector.instanceOf[OtherIncomeFromPropertyView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(otherIncomeFromProperty), taxYear, NormalMode, "individual", Rentals)(
          request,
          messages(application)
        ).toString
      }
    }

    "Rentals and RaR journey must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(OtherIncomeFromPropertyPage(RentalsRentARoom), otherIncomeFromProperty).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, RentalsAndRaRRoute)
        val view = application.injector.instanceOf[OtherIncomeFromPropertyView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(otherIncomeFromProperty), taxYear, NormalMode, "individual", RentalsRentARoom)(
          request,
          messages(application)
        ).toString
      }
    }

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
        val request =
          FakeRequest(POST, RentalsRoute)
            .withFormUrlEncodedBody(("otherIncomeFromProperty", otherIncomeFromProperty.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "Rentals journey must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, RentalsRoute)
            .withFormUrlEncodedBody(("otherIncomeFromProperty", ""))

        val boundForm = form.bind(Map("otherIncomeFromProperty" -> ""))

        val view = application.injector.instanceOf[OtherIncomeFromPropertyView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode, "individual", Rentals)(
          request,
          messages(application)
        ).toString
      }
    }

    "Rentals and RaR journey must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, RentalsAndRaRRoute)
            .withFormUrlEncodedBody(("otherIncomeFromProperty", ""))

        val boundForm = form.bind(Map("otherIncomeFromProperty" -> ""))

        val view = application.injector.instanceOf[OtherIncomeFromPropertyView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode, "individual", RentalsRentARoom)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, RentalsRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
        // redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, RentalsRoute)
            .withFormUrlEncodedBody(("otherIncomeFromProperty", "non-numeric-value"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
