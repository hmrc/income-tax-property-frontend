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

package controllers.ukrentaroom.adjustments

import base.SpecBase
import forms.ukrentaroom.adjustments.RaRUnusedLossesBroughtForwardFormProvider
import models.{NormalMode, UnusedLossesBroughtForward, UserAnswers}
import navigation.{Navigator, FakeNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.prop.TableFor1
import org.scalatest.prop.Tables.Table
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.ukrentaroom.adjustments.RaRUnusedLossesBroughtForwardPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ukrentaroom.adjustments.RaRUnusedLossesBroughtForwardView

import java.time.LocalDate
import scala.concurrent.Future

class RaRUnusedLossesBroughtForwardControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val scenarios: TableFor1[String] = Table[String]("individualOrAgent", "individual", "agent")
  val taxYear: Int = LocalDate.now().getYear
  val formProvider = new RaRUnusedLossesBroughtForwardFormProvider()
  val validAnswer: UnusedLossesBroughtForward = UnusedLossesBroughtForward(
    unusedLossesBroughtForwardYesOrNo = true, unusedLossesBroughtForwardAmount = Some(123.45))
  lazy val raRUnusedLossesBroughtForwardRoute: String = routes.RaRUnusedLossesBroughtForwardController.onPageLoad(taxYear, NormalMode).url

  forAll(scenarios) { (individualOrAgent: String) =>
    val form = formProvider(individualOrAgent)
    val isAgent: Boolean = individualOrAgent == "agent"

    s"RaRUnusedLossesBroughtForward Controller for an $individualOrAgent" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, raRUnusedLossesBroughtForwardRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RaRUnusedLossesBroughtForwardView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, taxYear, individualOrAgent, NormalMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = UserAnswers(userAnswersId).set(RaRUnusedLossesBroughtForwardPage, validAnswer).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, raRUnusedLossesBroughtForwardRoute)

          val view = application.injector.instanceOf[RaRUnusedLossesBroughtForwardView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(validAnswer), taxYear, individualOrAgent, NormalMode)(request, messages(application)).toString
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
            FakeRequest(POST, raRUnusedLossesBroughtForwardRoute)
              .withFormUrlEncodedBody(
                "unusedLossesBroughtForwardYesOrNo" -> "true",
                "unusedLossesBroughtForwardAmount" -> "123.45"
              )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = isAgent).build()

        running(application) {
          val request =
            FakeRequest(POST, raRUnusedLossesBroughtForwardRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))

          val boundForm = form.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[RaRUnusedLossesBroughtForwardView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, taxYear, individualOrAgent, NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, raRUnusedLossesBroughtForwardRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgent = isAgent).build()

        running(application) {
          val request =
            FakeRequest(POST, raRUnusedLossesBroughtForwardRoute)
              .withFormUrlEncodedBody(
                "rnusedLossesBroughtForwardYesOrNo" -> validAnswer.unusedLossesBroughtForwardYesOrNo.toString,
                "unusedLossesBroughtForwardAmount" -> validAnswer.unusedLossesBroughtForwardAmount.toString
              )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}