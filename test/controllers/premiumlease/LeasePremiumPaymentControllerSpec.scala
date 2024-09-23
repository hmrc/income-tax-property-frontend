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

package controllers.premiumlease

import base.SpecBase
import controllers.premiumlease.routes._
import controllers.routes
import forms.premiumlease.PremiumForLeaseFormProvider
import models.{NormalMode, PropertyType, Rentals, RentalsRentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import pages.premiumlease.PremiumForLeasePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.premiumlease.PremiumForLeaseView

import java.time.LocalDate
import scala.concurrent.Future

class PremiumForLeaseControllerSpec extends SpecBase with MockitoSugar {

  def onwardRouteYes: Call = Call("GET", "/calculated-figure-yourself")
  def onwardRouteNo: Call = Call("GET", "/reverse-premiums-received")
  private val formProvider = new PremiumForLeaseFormProvider()
  private val form = formProvider("individual")
  private val taxYear = LocalDate.now.getYear
  val scenarios = Table[PropertyType, String](
    ("property type", "type definition"),
    (RentalsRentARoom, "rentalsAndRaR"),
    (Rentals, "rentals")
  )

  forAll(scenarios) { (propertyType: PropertyType, propertyTypeDefinition: String) =>
    lazy val premiumForLeaseRoute = PremiumForLeaseController.onPageLoad(taxYear, NormalMode, propertyType).url

    s"PremiumForLease Controller $propertyTypeDefinition" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

        running(application) {
          val request = FakeRequest(GET, premiumForLeaseRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PremiumForLeaseView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, taxYear, NormalMode, "individual", propertyType)(
            request,
            messages(application)
          ).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = UserAnswers(userAnswersId).set(PremiumForLeasePage(propertyType), true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

        running(application) {
          val request = FakeRequest(GET, premiumForLeaseRoute)

          val view = application.injector.instanceOf[PremiumForLeaseView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(true), taxYear, NormalMode, "individual", propertyType)(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to the next page when valid data is submitted - yes" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        when(mockSessionRepository.clear(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false)
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRouteYes)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, premiumForLeaseRoute)
              .withFormUrlEncodedBody(("premiumForLeaseYesOrNo", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRouteYes.url
        }
      }

      "must redirect to the next page when valid data is submitted - no" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        when(mockSessionRepository.clear(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false)
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRouteNo)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, premiumForLeaseRoute)
              .withFormUrlEncodedBody(("premiumForLeaseYesOrNo", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRouteNo.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

        running(application) {
          val request =
            FakeRequest(POST, premiumForLeaseRoute)
              .withFormUrlEncodedBody(("premiumForLeaseYesOrNo", ""))

          val boundForm = form.bind(Map("premiumForLeaseYesOrNo" -> ""))

          val view = application.injector.instanceOf[PremiumForLeaseView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode, "individual", propertyType)(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgent = true).build()

        running(application) {
          val request = FakeRequest(GET, premiumForLeaseRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgent = true).build()

        running(application) {
          val request =
            FakeRequest(POST, premiumForLeaseRoute)
              .withFormUrlEncodedBody(("premiumForLeaseYesOrNo", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
