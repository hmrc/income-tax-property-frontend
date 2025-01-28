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

package controllers.ukandforeignproperty

import base.SpecBase
import controllers.ukandforeignproperty.routes.{UKPremiumsGrantLeaseController, UkAndForeignPropertyAmountReceivedForGrantOfLeaseController, UkYearLeaseAmountController}
import forms.ukandforeignproperty.UKPremiumsGrantLeaseFormProvider
import models.ukAndForeign.{UKPremiumsGrantLease, UkAndForeignPropertyAmountReceivedForGrantOfLease}
import models.{NormalMode, UserAnswers}
import navigation.{FakeUKAndForeignPropertyNavigator, UkAndForeignPropertyNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.prop.TableFor1
import org.scalatest.prop.Tables.Table
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.ukandforeignproperty.{UKPremiumsGrantLeasePage, UkAmountReceivedForGrantOfLeasePage, UkYearLeaseAmountPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ukandforeignproperty.UKPremiumsGrantLeaseView

import java.time.LocalDate
import scala.concurrent.Future

class UKPremiumsGrantLeaseControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new UKPremiumsGrantLeaseFormProvider()
  val form = formProvider("agent")
  private val taxYear = LocalDate.now.getYear

  def onwardRoute = Call("GET", "/foo")

  val validAnswer = BigDecimal(50)
  val validSubmitAnswer = BigDecimal(50)

  lazy val ukPremiumsGrantLeaseRoute: String = UKPremiumsGrantLeaseController.onPageLoad(taxYear, NormalMode).url

  val scenarios: TableFor1[String] = Table[String]("individualOrAgent", "individual", "agent")

  forAll(scenarios) { (individualOrAgent: String) =>
    val isAgent: Boolean = individualOrAgent == "agent"
    val form = formProvider(individualOrAgent)

    s"UKPremiumsGrantLease Controller for an $individualOrAgent" - {

      "must return OK and the correct view for a GET" in {

        val userAnswers = UserAnswers(userAnswersId)
          .set(UkAmountReceivedForGrantOfLeasePage, UkAndForeignPropertyAmountReceivedForGrantOfLease(BigDecimal(100)))
          .success
          .value
          .set(UkYearLeaseAmountPage, 10)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, ukPremiumsGrantLeaseRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[UKPremiumsGrantLeaseView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, taxYear, 10, BigDecimal(100), NormalMode, individualOrAgent)(
            request,
            messages(application)
          ).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = UserAnswers(userAnswersId)
          .set(UkAmountReceivedForGrantOfLeasePage, UkAndForeignPropertyAmountReceivedForGrantOfLease(BigDecimal(100)))
          .success
          .value
          .set(UkYearLeaseAmountPage, 10)
          .success
          .value
          .set(UKPremiumsGrantLeasePage, UKPremiumsGrantLease(premiumsGrantLeaseReceived = true, Some(validAnswer)))
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, ukPremiumsGrantLeaseRoute)

          val view = application.injector.instanceOf[UKPremiumsGrantLeaseView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            form.fill(UKPremiumsGrantLease(premiumsGrantLeaseReceived = true, Some(validAnswer))),
            taxYear,
            10,
            BigDecimal(100),
            NormalMode,
            individualOrAgent
          )(request, messages(application)).toString
        }
      }

      "must redirect to received grant amount page, when no reversePremiums is found in user data for a GET" in {

        val userAnswers = UserAnswers(userAnswersId)
          .set(UkYearLeaseAmountPage, 10)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, ukPremiumsGrantLeaseRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual UkAndForeignPropertyAmountReceivedForGrantOfLeaseController
            .onPageLoad(taxYear, NormalMode)
            .url
        }
      }

      "must redirect to year Lease reversePremiums page, when no period is found in user data for a GET" in {

        val userAnswers = UserAnswers(userAnswersId)
          .set(UkAmountReceivedForGrantOfLeasePage, UkAndForeignPropertyAmountReceivedForGrantOfLease(BigDecimal(100)))
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, ukPremiumsGrantLeaseRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual UkYearLeaseAmountController
            .onPageLoad(taxYear, NormalMode)
            .url
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = UserAnswers(userAnswersId)
          .set(UkAmountReceivedForGrantOfLeasePage, UkAndForeignPropertyAmountReceivedForGrantOfLease(BigDecimal(100)))
          .success
          .value
          .set(UkYearLeaseAmountPage, 10)
          .success
          .value

        val application =
          applicationBuilder(userAnswers = Some(userAnswers), isAgent = isAgent)
            .overrides(
              bind[UkAndForeignPropertyNavigator].toInstance(new FakeUKAndForeignPropertyNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, ukPremiumsGrantLeaseRoute)
              .withFormUrlEncodedBody(
                ("premiumsGrantLeaseReceived", "false"),
                ("premiumsGrantLeaseAmount", validAnswer.toString())
              )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must redirect to received grant reversePremiums page, when no reversePremiums is found in user data when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = UserAnswers(userAnswersId)
          .set(UkYearLeaseAmountPage, 3)
          .success
          .value

        val application =
          applicationBuilder(userAnswers = Some(userAnswers), isAgent = isAgent)
            .overrides(
              bind[UkAndForeignPropertyNavigator].toInstance(new FakeUKAndForeignPropertyNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, ukPremiumsGrantLeaseRoute)
              .withFormUrlEncodedBody(("value", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual UkAndForeignPropertyAmountReceivedForGrantOfLeaseController
            .onPageLoad(taxYear, NormalMode)
            .url
        }
      }

      "must redirect to year lease reversePremiums page, when no reversePremiums is found in user data when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = UserAnswers(userAnswersId)
          .set(UkAmountReceivedForGrantOfLeasePage, UkAndForeignPropertyAmountReceivedForGrantOfLease(BigDecimal(100)))
          .success
          .value

        val application =
          applicationBuilder(userAnswers = Some(userAnswers), isAgent = isAgent)
            .overrides(
              bind[UkAndForeignPropertyNavigator].toInstance(new FakeUKAndForeignPropertyNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, ukPremiumsGrantLeaseRoute)
              .withFormUrlEncodedBody(("value", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual UkYearLeaseAmountController
            .onPageLoad(taxYear, NormalMode)
            .url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val userAnswers = UserAnswers(userAnswersId)
          .set(UkAmountReceivedForGrantOfLeasePage, UkAndForeignPropertyAmountReceivedForGrantOfLease(BigDecimal(100)))
          .success
          .value
          .set(UkYearLeaseAmountPage, 10)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = isAgent).build()

        running(application) {
          val request =
            FakeRequest(POST, ukPremiumsGrantLeaseRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))

          val boundForm = form.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[UKPremiumsGrantLeaseView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, taxYear, 10, BigDecimal(100), NormalMode, individualOrAgent)(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgent = isAgent).build()

        running(application) {
          val rentalsRequest = FakeRequest(GET, ukPremiumsGrantLeaseRoute)

          val rentalsResult = route(application, rentalsRequest).value

          status(rentalsResult) mustEqual SEE_OTHER
          redirectLocation(rentalsResult).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgent = isAgent).build()

        running(application) {
          val rentalsRequest =
            FakeRequest(POST, ukPremiumsGrantLeaseRoute)
              .withFormUrlEncodedBody(("value", validAnswer.toString))

          val rentalsResult = route(application, rentalsRequest).value

          status(rentalsResult) mustEqual SEE_OTHER

          redirectLocation(rentalsResult).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
