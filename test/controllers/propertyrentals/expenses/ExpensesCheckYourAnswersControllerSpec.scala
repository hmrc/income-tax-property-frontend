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

package controllers.propertyrentals.expenses

import audit.RentalsExpense
import base.SpecBase
import models.JourneyPath.PropertyRentalExpenses
import models.{ConsolidatedExpenses, JourneyContext, Rentals, UserAnswers}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.propertyrentals.expenses.ConsolidatedExpensesPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.PropertySubmissionService
import viewmodels.govuk.SummaryListFluency
import views.html.propertyrentals.expenses.ExpensesCheckYourAnswersView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ExpensesCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency {

  val taxYear = 2024

  def onwardRoute: Call =
    Call(
      "POST",
      s"/update-and-submit-income-tax-return/property/$taxYear/rentals/expenses/complete-yes-no"
    )

  "ExpensesCheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()
      val list = SummaryListViewModel(Seq.empty)
      val taxYear = 2023
      running(application) {

        val request = FakeRequest(GET, routes.ExpensesCheckYourAnswersController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ExpensesCheckYourAnswersView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, taxYear)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val taxYear = 2023
      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, routes.ExpensesCheckYourAnswersController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the correct view for a POST (onSubmit)" in {

      val consolidatedExpenses = ConsolidatedExpenses(consolidatedExpensesYesOrNo = true, Some(12))
      val userAnswers = UserAnswers("test").set(ConsolidatedExpensesPage(Rentals), consolidatedExpenses).toOption

      val context =
        JourneyContext(taxYear = taxYear, mtditid = "mtditid", nino = "nino", journeyPath = PropertyRentalExpenses)
      val rentalsExpense = RentalsExpense(Some(consolidatedExpenses), None, None, None, None, None, None, None)

      when(
        propertySubmissionService
          .saveJourneyAnswers(ArgumentMatchers.eq(context), ArgumentMatchers.eq(rentalsExpense))(
            any(),
            any()
          )
      ) thenReturn Future(Right())

      val application = applicationBuilder(userAnswers = userAnswers, isAgent = true)
        .overrides(bind[PropertySubmissionService].toInstance(propertySubmissionService))
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.ExpensesCheckYourAnswersController.onSubmit(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }
}
