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

package controllers.foreign.expenses

import base.SpecBase
import forms.foreign.expenses.ForeignExpensesSectionCompleteFormProvider
import models.JourneyPath.ForeignPropertyExpenses
import models.{JourneyContext, User, UserAnswers}
import navigation.{FakeForeignPropertyNavigator, ForeignPropertyNavigator}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{doReturn, when}
import org.scalatest.prop.TableFor1
import org.scalatest.prop.Tables.Table
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.foreign.expenses.ForeignExpensesSectionCompletePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import service.JourneyAnswersService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.foreign.expenses.ForeignExpensesSectionCompleteView

import java.time.LocalDate
import scala.concurrent.Future

class ForeignExpensesSectionCompleteControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val scenarios: TableFor1[String] = Table[String]("individualOrAgent", "individual", "agent")

  val formProvider = new ForeignExpensesSectionCompleteFormProvider()
  val form: Form[Boolean] = formProvider()
  val taxYear: Int = LocalDate.now().getYear
  val countryCode: String = "USA"
  implicit val hc: HeaderCarrier = new HeaderCarrier()

  lazy val foreignExpensesSectionCompleteRoute: String =
    routes.ForeignExpensesSectionCompleteController.onPageLoad(taxYear, countryCode).url

  forAll(scenarios) { (individualOrAgent: String) =>
    val isAgent: Boolean = individualOrAgent == "agent"
    val user: User = User(
      mtditid = "mtditid",
      nino = "nino",
      affinityGroup = "affinityGroup",
      agentRef = Option.when(isAgent)("agentReferenceNumber")
    )

    s"ForeignExpensesSectionComplete Controller for an $individualOrAgent" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, foreignExpensesSectionCompleteRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ForeignExpensesSectionCompleteView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, taxYear, countryCode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers =
          UserAnswers(userAnswersId).set(ForeignExpensesSectionCompletePage(countryCode), true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, foreignExpensesSectionCompleteRoute)

          val view = application.injector.instanceOf[ForeignExpensesSectionCompleteView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(true), taxYear, countryCode)(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]
        val mockJourneyAnswersService = mock[JourneyAnswersService]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        doReturn(Future.successful(Right("completed")))
          .when(mockJourneyAnswersService)
          .setForeignStatus(
            ArgumentMatchers.eq(
              JourneyContext(
                taxYear = taxYear,
                mtditid = user.mtditid,
                nino = user.nino,
                journeyPath = ForeignPropertyExpenses
              )
            ),
            ArgumentMatchers.eq("completed"),
            ArgumentMatchers.eq(user),
            ArgumentMatchers.eq(countryCode)
          )(any())

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = isAgent)
            .overrides(
              bind[ForeignPropertyNavigator].toInstance(new FakeForeignPropertyNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[JourneyAnswersService].toInstance(mockJourneyAnswersService)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, foreignExpensesSectionCompleteRoute)
              .withFormUrlEncodedBody(("foreignExpensesSectionComplete", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = isAgent).build()

        running(application) {
          val request =
            FakeRequest(POST, foreignExpensesSectionCompleteRoute)
              .withFormUrlEncodedBody(("foreignExpensesSectionComplete", ""))

          val boundForm = form.bind(Map("foreignExpensesSectionComplete" -> ""))

          val view = application.injector.instanceOf[ForeignExpensesSectionCompleteView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, taxYear, countryCode)(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, foreignExpensesSectionCompleteRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgent = isAgent).build()

        running(application) {
          val request =
            FakeRequest(POST, foreignExpensesSectionCompleteRoute)
              .withFormUrlEncodedBody(("foreignExpensesSectionComplete", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
