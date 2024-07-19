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

package controllers.propertyrentals.income

import audit.RentalsIncome
import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.PropertySubmissionService
import viewmodels.govuk.SummaryListFluency
import views.html.propertyrentals.CheckYourAnswersView

import scala.concurrent.Future

class PropertyIncomeCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar {

  val taxYear: Int = 2024

  def onwardRoute: Call = Call(
    "GET",
    s"/update-and-submit-income-tax-return/property/$taxYear/rentals/income/complete-yes-no"
  )

  "Property Income Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), true).build()

      running(application) {
        val request = FakeRequest(GET, routes.PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        //        contentAsString(result) mustEqual view(list, taxYear)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, routes.PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the correct view for a POST (onSubmit)" in {
      val userAnswers = emptyUserAnswers
        .set(
            RentalsIncome,
            RentalsIncome(true, 500, 2, None, None, None, None, None, None)
          )
        .get

      val mockPropertySubmissionService = mock[PropertySubmissionService]
      when(mockPropertySubmissionService.savePropertyRentalsIncome(any(), any())(any())) thenReturn (Future.successful(
        Right(())
      ))

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false)
        .overrides(
          bind[PropertySubmissionService].toInstance(mockPropertySubmissionService)
        )
        .build()

      running(application) {
        val request = FakeRequest(
          POST,
          controllers.propertyrentals.income.routes.PropertyIncomeCheckYourAnswersController.onSubmit(taxYear).url
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }
}
