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

package controllers.allowances

import controllers.routes
import controllers.allowances.routes._
import base.SpecBase
import forms.allowances.ZeroEmissionGoodsVehicleAllowanceFormProvider
import models.{NormalMode, PropertyType, Rentals, RentalsRentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.allowances.ZeroEmissionGoodsVehicleAllowancePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.data.Form
import repositories.SessionRepository
import views.html.allowances.ZeroEmissionGoodsVehicleAllowanceView

import scala.concurrent.Future
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._

class ZeroEmissionGoodsVehicleAllowanceControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new ZeroEmissionGoodsVehicleAllowanceFormProvider()
  private val isAgentMessageKey = "individual"
  val form: Form[BigDecimal] = formProvider(isAgentMessageKey)

  def onwardRoute = Call("GET", "/foo")

  val validAnswer: BigDecimal = BigDecimal(0)
  val taxYear = 2023
  val scenarios = Table[PropertyType, String](
    ("property type", "type definition"),
    (RentalsRentARoom, "rentalsAndRaR"),
    (Rentals, "rentals")
  )

  forAll(scenarios) { (propertyType: PropertyType, propertyTypeDefinition: String) =>
    lazy val zeroEmissionGoodsVehicleAllowanceRoute =
      ZeroEmissionGoodsVehicleAllowanceController.onPageLoad(taxYear, NormalMode, propertyType).url

    s"ZeroEmissionGoodsVehicleAllowance Controller for $propertyTypeDefinition" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

        running(application) {
          val request = FakeRequest(GET, zeroEmissionGoodsVehicleAllowanceRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ZeroEmissionGoodsVehicleAllowanceView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, taxYear, isAgentMessageKey, NormalMode, propertyType)(
            request,
            messages(application)
          ).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers =
          UserAnswers(userAnswersId).set(ZeroEmissionGoodsVehicleAllowancePage(propertyType), validAnswer).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

        running(application) {
          val request = FakeRequest(GET, zeroEmissionGoodsVehicleAllowanceRoute)

          val view = application.injector.instanceOf[ZeroEmissionGoodsVehicleAllowanceView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            form.fill(validAnswer),
            taxYear,
            isAgentMessageKey,
            NormalMode,
            propertyType
          )(request, messages(application)).toString
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
            FakeRequest(POST, zeroEmissionGoodsVehicleAllowanceRoute)
              .withFormUrlEncodedBody(("zeroEmissionGoodsVehicleAllowanceAmount", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

        running(application) {
          val request =
            FakeRequest(POST, zeroEmissionGoodsVehicleAllowanceRoute)
              .withFormUrlEncodedBody(("zeroEmissionGoodsVehicleAllowanceAmount", "invalid value"))

          val boundForm = form.bind(Map("zeroEmissionGoodsVehicleAllowanceAmount" -> "invalid value"))

          val view = application.injector.instanceOf[ZeroEmissionGoodsVehicleAllowanceView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, taxYear, isAgentMessageKey, NormalMode, propertyType)(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgent = false).build()

        running(application) {
          val request = FakeRequest(GET, zeroEmissionGoodsVehicleAllowanceRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgent = false).build()

        running(application) {
          val request =
            FakeRequest(POST, zeroEmissionGoodsVehicleAllowanceRoute)
              .withFormUrlEncodedBody(("zeroEmissionGoodsVehicleAllowanceAmount", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
