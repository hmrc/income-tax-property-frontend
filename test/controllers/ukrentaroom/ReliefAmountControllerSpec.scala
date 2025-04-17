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

package controllers.ukrentaroom

import base.SpecBase
import forms.ukrentaroom.ReliefAmountFormProvider
import models.{BusinessConstants, NormalMode, RentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ukrentaroom.{JointlyLetPage, ReliefAmountPage, TotalIncomeAmountPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ukrentaroom.ReliefAmountView

import scala.concurrent.Future
import scala.util.Try

class ReliefAmountControllerSpec extends SpecBase with MockitoSugar {
  def onwardRoute: Call = Call("GET", "/foo")

  private val maxAllowedRelief = BigDecimal(5000)
  val formProvider = new ReliefAmountFormProvider()
  val form: Form[BigDecimal] = formProvider("individual", maxAllowedRelief)
  val taxYear = 2024

  lazy val reliefAmountRoute: String =
    routes.ReliefAmountController.onPageLoad(taxYear, NormalMode, RentARoom).url

  "ReliefAmount Controller" - {

    "must return OK and the correct view for a GET" in {
      val answers: Try[UserAnswers] = for {
        withJointLet    <- emptyUserAnswers.set(JointlyLetPage(RentARoom), true)
        withTotalIncome <- withJointLet.set(TotalIncomeAmountPage(RentARoom), maxAllowedRelief)
      } yield withTotalIncome

      val application = applicationBuilder(userAnswers = answers.toOption, isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, reliefAmountRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReliefAmountView]

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
        withJointLet     <- emptyUserAnswers.set(JointlyLetPage(RentARoom), false)
        withTotalIncome  <- withJointLet.set(TotalIncomeAmountPage(RentARoom), maxAllowedRelief)
        withReliefAmount <- withTotalIncome.set(ReliefAmountPage(RentARoom), BigDecimal(100.65))
      } yield withReliefAmount

      val application = applicationBuilder(userAnswers = answers.toOption, isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, reliefAmountRoute)

        val view = application.injector.instanceOf[ReliefAmountView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(BigDecimal(100.65)),
          taxYear,
          NormalMode,
          "individual",
          maxAllowedRelief,
          RentARoom
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val answers: Try[UserAnswers] = for {
        withJointLet    <- emptyUserAnswers.set(JointlyLetPage(RentARoom), true)
        withTotalIncome <- withJointLet.set(TotalIncomeAmountPage(RentARoom), maxAllowedRelief)
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
          FakeRequest(POST, reliefAmountRoute)
            .withFormUrlEncodedBody(("reliefAmount", "100.5"))

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
          FakeRequest(POST, reliefAmountRoute)
            .withFormUrlEncodedBody(("reliefAmount", ""))

        val boundForm = form.bind(Map("reliefAmount" -> ""))

        val view = application.injector.instanceOf[ReliefAmountView]

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
        val request = FakeRequest(GET, reliefAmountRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, reliefAmountRoute)
            .withFormUrlEncodedBody(("reliefAmount", "100.7"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
