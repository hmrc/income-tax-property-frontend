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

package controllers.furnishedholidaylettings

import base.SpecBase
import controllers.routes
import forms.furnishedholidaylettings.FhlReliefOrExpensesFormProvider
import models.requests.DataRequest
import models.{FhlReliefOrExpenses, NormalMode, User, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import pages.furnishedholidaylettings.{FhlJointlyLetPage, FhlReliefOrExpensesPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.furnishedholidaylettings.FhlReliefOrExpensesView
import models.FhlReliefOrExpenses._
import scala.concurrent.Future

class FhlReliefOrExpensesControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new FhlReliefOrExpensesFormProvider()

  val taxYear = 2024
  lazy val fhlReliefOrExpensesRoute = controllers.furnishedholidaylettings.routes.FhlReliefOrExpensesController.onPageLoad(taxYear, NormalMode).url

  val scenarios = Table[String, Boolean, Boolean, String](
    ("AgencyOrIndividual", "IsAgency", "IsJointlyLet", "Relief"),
    ("agent", true, true, "£3,750"),
    ("agent", true, false, "£7,500"),
    ("individual", false, true, "£3,750"),
    ("individual", false, false, "£7,500")
  )

  forAll(scenarios) { (agencyOrIndividual: String, isAgency: Boolean, isJointlyLet: Boolean, relief: String) => {
    val form = formProvider(agencyOrIndividual)
    val user = User(
      "",
      "",
      "",
      isAgency
    )
    val baseUserAnswers = emptyUserAnswers.set(FhlJointlyLetPage, isJointlyLet)

    s"FhlReliefOrExpenses Controller isAgency: $isAgency, isJointlyLet: $isJointlyLet" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = baseUserAnswers.toOption, isAgency).build()

        running(application) {
          val fakeRequest = FakeRequest(GET, fhlReliefOrExpensesRoute)
          val request = DataRequest(fakeRequest, "", user, emptyUserAnswers)

          val result = route(application, request).value

          val view = application.injector.instanceOf[FhlReliefOrExpensesView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, taxYear, NormalMode, relief)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = baseUserAnswers.flatMap(_.set(FhlReliefOrExpensesPage, FhlReliefOrExpenses.values.head)).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgency).build()

        running(application) {
          val fakeRequest = FakeRequest(GET, fhlReliefOrExpensesRoute)
          val request = DataRequest(fakeRequest, "", user, emptyUserAnswers)
          val view = application.injector.instanceOf[FhlReliefOrExpensesView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(FhlReliefOrExpenses.values.head), taxYear, NormalMode, relief)(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = baseUserAnswers.toOption, isAgency)
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, fhlReliefOrExpensesRoute)
              .withFormUrlEncodedBody(("fhlReliefOrExpenses", FhlReliefOrExpenses.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = baseUserAnswers.toOption, isAgency).build()

        running(application) {
          val fakeRequest =
            FakeRequest(POST, fhlReliefOrExpensesRoute)
              .withFormUrlEncodedBody(("fhlReliefOrExpenses", ""))
          val request = DataRequest(fakeRequest, "", user, emptyUserAnswers)

          val boundForm = form.bind(Map("fhlReliefOrExpenses" -> ""))

          val view = application.injector.instanceOf[FhlReliefOrExpensesView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode, relief)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgency).build()

        running(application) {
          val request = FakeRequest(GET, fhlReliefOrExpensesRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgency).build()

        running(application) {
          val request =
            FakeRequest(POST, fhlReliefOrExpensesRoute)
              .withFormUrlEncodedBody(("fhlReliefOrExpenses", FhlReliefOrExpenses.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
  }
}
