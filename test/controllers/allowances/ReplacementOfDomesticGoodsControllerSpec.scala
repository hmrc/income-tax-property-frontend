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

package controllers.allowances

import base.SpecBase
import forms.allowances.ReplacementOfDomesticGoodsFormProvider
import models.{NormalMode, PropertyType, Rentals, RentalsRentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.prop.Tables.Table
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.allowances.ReplacementOfDomesticGoodsPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.allowances.ReplacementOfDomesticGoodsView

import scala.concurrent.Future
import scala.util.Random

class ReplacementOfDomesticGoodsControllerSpec extends SpecBase with MockitoSugar {

  val taxYear = 2024
  val validAnswer: BigDecimal = BigDecimal(0)

  private val individualAgent = Array("individual", "agent")
  private val individualOrAgent = individualAgent(Random.nextInt(individualAgent.length))
  private val isAgent = individualOrAgent.equals("agent")

  private val formProvider = new ReplacementOfDomesticGoodsFormProvider()
  private val form: Form[BigDecimal] = formProvider(individualOrAgent)

  private val scenarios = Table[PropertyType, String](
    ("property type", "type definition"),
    (Rentals, "rentals"),
    (RentalsRentARoom, "rentalsAndRaR")
  )

  private def onwardRoute = Call("GET", "/foo")

  forAll(scenarios) { (propertyType: PropertyType, propertyTypeDefinition: String) =>
    lazy val replacementOfDomesticGoodsControllerRoute: String =
      routes.ReplacementOfDomesticGoodsController.onPageLoad(taxYear, NormalMode, propertyType).url

    s"ReplacementOfDomesticGoodsController for property type: $propertyTypeDefinition and for an $individualOrAgent " - {

      s"must return OK and the correct view for a GET " in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, replacementOfDomesticGoodsControllerRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ReplacementOfDomesticGoodsView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, taxYear, individualOrAgent, NormalMode, propertyType)(
            request,
            messages(application)
          ).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers =
          UserAnswers(userAnswersId).set(ReplacementOfDomesticGoodsPage(propertyType), validAnswer).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, replacementOfDomesticGoodsControllerRoute)

          val view = application.injector.instanceOf[ReplacementOfDomesticGoodsView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            form.fill(validAnswer),
            taxYear,
            individualOrAgent,
            NormalMode,
            propertyType
          )(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = isAgent)
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, replacementOfDomesticGoodsControllerRoute)
              .withFormUrlEncodedBody(("replacementOfDomesticGoodsAmount", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = isAgent).build()

        running(application) {
          val request =
            FakeRequest(POST, replacementOfDomesticGoodsControllerRoute)
              .withFormUrlEncodedBody(("replacementOfDomesticGoodsAmount", "invalid value"))

          val boundForm = form.bind(Map("replacementOfDomesticGoodsAmount" -> "invalid value"))

          val view = application.injector.instanceOf[ReplacementOfDomesticGoodsView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, taxYear, individualOrAgent, NormalMode, propertyType)(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, replacementOfDomesticGoodsControllerRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgent = isAgent).build()

        running(application) {
          val request =
            FakeRequest(POST, replacementOfDomesticGoodsControllerRoute)
              .withFormUrlEncodedBody(("replacementOfDomesticGoodsAmount", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
