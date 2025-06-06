/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.foreign.adjustments

import base.SpecBase
import forms.foreign.adjustments.ForeignUnusedResidentialFinanceCostFormProvider
import models.{ForeignUnusedResidentialFinanceCost, NormalMode, UserAnswers}
import navigation.{FakeForeignPropertyNavigator, ForeignPropertyNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.prop.TableFor1
import org.scalatest.prop.Tables.Table
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.foreign.adjustments.ForeignUnusedResidentialFinanceCostPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.foreign.adjustments.ForeignUnusedResidentialFinanceCostView

import scala.concurrent.Future

class ForeignUnusedResidentialFinanceCostControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new ForeignUnusedResidentialFinanceCostFormProvider()
  val countryCode: String = "USA"
  def onwardRoute: Call = Call("GET", "/foo")
  val validAnswer: ForeignUnusedResidentialFinanceCost =
    ForeignUnusedResidentialFinanceCost(isForeignUnusedResidentialFinanceCost = true, Some(BigDecimal(0)))
  val taxYear = 2024
  val scenarios: TableFor1[String] = Table[String]("individualOrAgent", "individual", "agent")
  lazy val foreignUnusedResidentialFinanceCostRoute: String =
    routes.ForeignUnusedResidentialFinanceCostController.onPageLoad(taxYear, countryCode, NormalMode).url

  forAll(scenarios) { (individualOrAgent: String) =>
    val form = formProvider(individualOrAgent)
    val isAgent: Boolean = individualOrAgent == "agent"

    s"ForeignUnusedResidentialFinanceCost Controller for an $individualOrAgent" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, foreignUnusedResidentialFinanceCostRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ForeignUnusedResidentialFinanceCostView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, taxYear, countryCode, individualOrAgent, NormalMode)(
            request,
            messages(application)
          ).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = UserAnswers(userAnswersId)
          .set(ForeignUnusedResidentialFinanceCostPage(countryCode), validAnswer)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, foreignUnusedResidentialFinanceCostRoute)

          val view = application.injector.instanceOf[ForeignUnusedResidentialFinanceCostView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            form.fill(validAnswer),
            taxYear,
            countryCode,
            individualOrAgent,
            NormalMode
          )(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = isAgent)
            .overrides(
              bind[ForeignPropertyNavigator].toInstance(new FakeForeignPropertyNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, foreignUnusedResidentialFinanceCostRoute)
              .withFormUrlEncodedBody(
                ("isForeignUnusedResidentialFinanceCost", "true"),
                ("foreignUnusedResidentialFinanceCostAmount", "123.45")
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
            FakeRequest(POST, foreignUnusedResidentialFinanceCostRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))

          val boundForm = form.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[ForeignUnusedResidentialFinanceCostView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, taxYear, countryCode, individualOrAgent, NormalMode)(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, foreignUnusedResidentialFinanceCostRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgent = isAgent).build()

        running(application) {
          val request =
            FakeRequest(POST, foreignUnusedResidentialFinanceCostRoute)
              .withFormUrlEncodedBody(
                ("isForeignUnusedResidentialFinanceCost", "true"),
                ("foreignUnusedResidentialFinanceCostAmount", "123.45")
              )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
