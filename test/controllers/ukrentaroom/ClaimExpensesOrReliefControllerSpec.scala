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

package controllers.ukrentaroom

import base.SpecBase
import forms.ukrentaroom.ClaimExpensesOrReliefFormProvider
import models.{BusinessConstants, ClaimExpensesOrRelief, NormalMode, RentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ukrentaroom.{ClaimExpensesOrReliefPage, JointlyLetPage, TotalIncomeAmountPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ukrentaroom.ClaimExpensesOrReliefView

import scala.concurrent.Future
import scala.util.Try

class ClaimExpensesOrReliefControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  private val maxAllowedIncome = BigDecimal(5000)
  val formProvider = new ClaimExpensesOrReliefFormProvider()
  val form: Form[ClaimExpensesOrRelief] = formProvider("individual",maxAllowedIncome)
  val taxYear = 2024

  lazy val claimExpensesOrReliefRoute: String =
    routes.ClaimExpensesOrReliefController.onPageLoad(taxYear, NormalMode, RentARoom).url

  "ClaimExpensesOrRelief Controller" - {

    "must return OK and the correct view for a GET" in {
      val answers: Try[UserAnswers] = for {
        withJointLet    <- emptyUserAnswers.set(JointlyLetPage(RentARoom), true)
        withTotalIncome <- withJointLet.set(TotalIncomeAmountPage(RentARoom), maxAllowedIncome)
      } yield withTotalIncome

      val application = applicationBuilder(userAnswers = answers.toOption, isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, claimExpensesOrReliefRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ClaimExpensesOrReliefView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          taxYear,
          NormalMode,
          "individual",
          BusinessConstants.jointlyLetTaxFreeAmount,
          RentARoom
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val answers: Try[UserAnswers] = for {
        withJointLet    <- emptyUserAnswers.set(JointlyLetPage(RentARoom), false)
        withTotalIncome <- withJointLet.set(TotalIncomeAmountPage(RentARoom), maxAllowedIncome)
        withClaimExpenses <-
          withTotalIncome.set(
            ClaimExpensesOrReliefPage(RentARoom),
            ClaimExpensesOrRelief(claimExpensesOrReliefYesNo = true, Some(100.65))
          )
      } yield withClaimExpenses

      val application = applicationBuilder(userAnswers = answers.toOption, isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, claimExpensesOrReliefRoute)

        val view = application.injector.instanceOf[ClaimExpensesOrReliefView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(ClaimExpensesOrRelief(claimExpensesOrReliefYesNo = true, Some(100.65))),
          taxYear,
          NormalMode,
          "individual",
          maxAllowedIncome,
          RentARoom
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val answers: Try[UserAnswers] = for {
        withJointLet    <- emptyUserAnswers.set(JointlyLetPage(RentARoom), true)
        withTotalIncome <- withJointLet.set(TotalIncomeAmountPage(RentARoom), maxAllowedIncome)
      } yield withTotalIncome

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = answers.toOption, isAgent = false)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, claimExpensesOrReliefRoute)
            .withFormUrlEncodedBody(("claimExpensesOrRelief", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val answers: Try[UserAnswers] = for {
        withJointLet    <- emptyUserAnswers.set(JointlyLetPage(RentARoom), false)
        withTotalIncome <- withJointLet.set(TotalIncomeAmountPage(RentARoom), BigDecimal(8000))
      } yield withTotalIncome

      val application = applicationBuilder(userAnswers = answers.toOption, isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, claimExpensesOrReliefRoute)
            .withFormUrlEncodedBody(("claimExpensesOrRelief", ""))

        val boundForm = form.bind(Map("claimExpensesOrRelief" -> ""))

        val view = application.injector.instanceOf[ClaimExpensesOrReliefView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          boundForm,
          taxYear,
          NormalMode,
          "individual",
          BusinessConstants.notJointlyLetTaxFreeAmount,
          RentARoom
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, claimExpensesOrReliefRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, claimExpensesOrReliefRoute)
            .withFormUrlEncodedBody(("claimExpensesOrRelief", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
