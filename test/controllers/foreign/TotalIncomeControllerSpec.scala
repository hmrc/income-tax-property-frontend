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
import forms.foreign.TotalIncomeFormProvider
import models.{ForeignTotalIncome, NormalMode, TotalIncome, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.prop.TableFor1
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.foreign.TotalIncomePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.foreign.TotalIncomeView
import org.scalatest.prop.Tables.Table

import scala.concurrent.Future

class TotalIncomeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/update-and-submit-income-tax-return/property/2024/foreign-property/about/0/select-income-country")
  private val taxYear = 2024

  lazy val totalIncomeRoute: String = controllers.foreign.routes.TotalIncomeController.onPageLoad(taxYear, NormalMode).url

  val formProvider = new TotalIncomeFormProvider()
  val scenarios: TableFor1[String] = Table[String]("individualOrAgent", "individual", "agent")

  forAll(scenarios) { (individualOrAgent: String) =>
    val form: Form[ForeignTotalIncome] = formProvider(individualOrAgent)
    val isAgent = individualOrAgent == "agent"
    s"TotalIncome Controller for an $individualOrAgent" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, totalIncomeRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[TotalIncomeView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, taxYear, NormalMode, individualOrAgent)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = UserAnswers(userAnswersId).set(TotalIncomePage, ForeignTotalIncome.values.head).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, totalIncomeRoute)

          val view = application.injector.instanceOf[TotalIncomeView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(ForeignTotalIncome.values.head),
            taxYear, NormalMode, individualOrAgent)(request, messages(application)).toString
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
            FakeRequest(POST, totalIncomeRoute)
              .withFormUrlEncodedBody(("foreignTotalIncome", ForeignTotalIncome.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = isAgent).build()

        running(application) {
          val request =
            FakeRequest(POST, totalIncomeRoute)
              .withFormUrlEncodedBody(("foreignTotalIncome", "invalid value"))

          val boundForm = form.bind(Map("foreignTotalIncome" -> "invalid value"))

          val view = application.injector.instanceOf[TotalIncomeView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode, individualOrAgent)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, totalIncomeRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgent = isAgent).build()

        running(application) {
          val request =
            FakeRequest(POST, totalIncomeRoute)
              .withFormUrlEncodedBody(("foreignTotalIncome", ForeignTotalIncome.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

  }


}
