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
import forms.propertyrentals.income.PropertyRentalIncomeFormProvider
import models.{NormalMode, Rentals, RentalsRentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import pages.propertyrentals.income.PropertyRentalIncomePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.propertyrentals.income.PropertyRentalIncomeView

import java.time.LocalDate
import scala.concurrent.Future

class PropertyRentalIncomeControllerSpec extends SpecBase with MockitoSugar with ScalaFutures {

  def onwardRoute = Call("GET", "/foo")

  private val formProvider = new PropertyRentalIncomeFormProvider()
  private val form: Form[BigDecimal] = formProvider("individual")
  private val taxYear: Int = LocalDate.now.getYear
  private val propertyRentalincome: BigDecimal = BigDecimal(12345)
  lazy val incomeFromPropertyRentalsRoute: String =
    routes.PropertyRentalIncomeController.onPageLoad(taxYear, NormalMode, Rentals).url
  lazy val incomeFromPropertyRentalsRentARoomRoute: String =
    routes.PropertyRentalIncomeController.onPageLoad(taxYear, NormalMode, RentalsRentARoom).url

  "incomeFromProperty Controller" - {

    "must return OK if the route is valid for both Rentals and RentalsRentARoom journeys" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val rentalsRequest = FakeRequest(GET, incomeFromPropertyRentalsRoute)
        val rentalsResult = route(application, rentalsRequest).value
        status(rentalsResult) mustEqual OK

        val rentalsRentARoomRequest = FakeRequest(GET, incomeFromPropertyRentalsRentARoomRoute)
        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value
        status(rentalsRentARoomResult) mustEqual OK
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered for both Rentals and RentalsRentARoom journeys" in {

      val rentalsUserAnswers =
        UserAnswers(userAnswersId).set(PropertyRentalIncomePage(Rentals), propertyRentalincome).success.value

      val rentalsRentARoomUserAnswers =
        UserAnswers(userAnswersId)
          .set(PropertyRentalIncomePage(RentalsRentARoom), propertyRentalincome)
          .success
          .value

      val rentalsApplication = applicationBuilder(userAnswers = Some(rentalsUserAnswers), isAgent = false).build()
      val rentalsRentARoomApplication =
        applicationBuilder(userAnswers = Some(rentalsRentARoomUserAnswers), isAgent = false).build()

      running(rentalsRentARoomApplication) {
        val request = FakeRequest(GET, incomeFromPropertyRentalsRentARoomRoute)
        val view = rentalsApplication.injector.instanceOf[PropertyRentalIncomeView]
        val result = route(rentalsRentARoomApplication, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(propertyRentalincome),
          taxYear,
          NormalMode,
          "individual",
          RentalsRentARoom
        )(
          request,
          messages(rentalsRentARoomApplication)
        ).toString
      }

      running(rentalsApplication) {
        val request = FakeRequest(GET, incomeFromPropertyRentalsRoute)
        val view = rentalsApplication.injector.instanceOf[PropertyRentalIncomeView]
        val result = route(rentalsApplication, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(propertyRentalincome),
          taxYear,
          NormalMode,
          "individual",
          Rentals
        )(
          request,
          messages(rentalsApplication)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted for both Rentals and RentalsRentARoom journeys" in {

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
        val rentalsRequest =
          FakeRequest(POST, incomeFromPropertyRentalsRoute)
            .withFormUrlEncodedBody(("propertyRentalIncome", propertyRentalincome.toString))

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual onwardRoute.url

        val rentalsRentARoomRequest =
          FakeRequest(POST, incomeFromPropertyRentalsRentARoomRoute)
            .withFormUrlEncodedBody(("propertyRentalIncome", propertyRentalincome.toString))

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual SEE_OTHER
        redirectLocation(rentalsRentARoomResult).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted for both Rentals and RentalsRentARoom journeys" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {

        val rentalsRequest =
          FakeRequest(POST, incomeFromPropertyRentalsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val rentalsRentARoomRequest =
          FakeRequest(POST, incomeFromPropertyRentalsRentARoomRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))
        val view = application.injector.instanceOf[PropertyRentalIncomeView]
        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual BAD_REQUEST
        contentAsString(rentalsResult) mustEqual view(boundForm, taxYear, NormalMode, "individual", Rentals)(
          rentalsRequest,
          messages(application)
        ).toString

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual BAD_REQUEST
        contentAsString(rentalsRentARoomResult) mustEqual view(
          boundForm,
          taxYear,
          NormalMode,
          "individual",
          RentalsRentARoom
        )(
          rentalsRentARoomRequest,
          messages(application)
        ).toString
      }
    }

    "must redirect to recovery for a GET if no existing data is found for both Rentals and RentalsRentARoom journeys" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val rentalsRequest = FakeRequest(GET, incomeFromPropertyRentalsRoute)
        val rentalsResult = route(application, rentalsRequest).value
        status(rentalsResult) mustEqual OK

        val rentalsRentARoomRequest = FakeRequest(GET, incomeFromPropertyRentalsRentARoomRoute)
        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value
        status(rentalsRentARoomResult) mustEqual OK
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found for both Rentals and RentalsRentARoom journeys" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val rentalsRequest =
          FakeRequest(POST, incomeFromPropertyRentalsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        val rentalsRentARoomRequest =
          FakeRequest(POST, incomeFromPropertyRentalsRentARoomRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual SEE_OTHER
        redirectLocation(rentalsRentARoomResult).value mustEqual controllers.routes.JourneyRecoveryController
          .onPageLoad()
          .url
      }
    }
  }
}
