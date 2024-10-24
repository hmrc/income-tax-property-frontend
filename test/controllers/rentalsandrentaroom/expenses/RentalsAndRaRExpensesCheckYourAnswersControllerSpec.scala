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

package controllers.rentalsandrentaroom.expenses

import audit.AuditService
import base.SpecBase
import connectors.error.ApiError
import models._
import models.backend.PropertyDetails
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.{times, verify}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import pages.propertyrentals.expenses.ConsolidatedExpensesPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.{BusinessService, PropertySubmissionService}
import viewmodels.govuk.SummaryListFluency
import views.html.rentalsandrentaroom.expenses.RentalsAndRaRExpensesCheckYourAnswersView

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RentalsAndRaRExpensesCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency {

  private val scenarios = Table[Boolean, String](
    ("isAgent", "individualOrAgent"),
    (false, "individual"),
    (true, "agent")
  )
  private val taxYear = 2024

  forAll(scenarios) { (isAgent: Boolean, agentOrIndividual: String) =>
    s"RentalsAndRentARoomExpenseCheckYourAnswers Controller for an $agentOrIndividual" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

        running(application) {
          val request = FakeRequest(
            GET,
            controllers.rentalsandrentaroom.expenses.routes.RentalsAndRaRExpensesCheckYourAnswersController
              .onPageLoad(taxYear)
              .url
          )

          val result = route(application, request).value

          val list = SummaryListViewModel(Seq.empty)

          val view = application.injector.instanceOf[RentalsAndRaRExpensesCheckYourAnswersView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(list, taxYear)(request, messages(application)).toString
        }
      }
    }

    s"must return OK and the correct view for a POST (onSubmit) for an $agentOrIndividual" in {

      val mockBusinessService = mock[BusinessService]
      val mockAuditService = mock[AuditService]
      val propertySubmissionService = mock[PropertySubmissionService]

      when(mockBusinessService.getUkPropertyDetails(any(), any())(any())) thenReturn Future
        .successful[Either[ApiError, Option[PropertyDetails]]](
          Right(
            Some(
              PropertyDetails(
                Some("incomeSourceType"),
                Some(LocalDate.now()),
                accrualsOrCash = Some(true), // true -> Accruals,false -> Cash
                incomeSourceId = "incomeSourceId"
              )
            )
          )
        )

      val expenses = 12
      val consolidatedExpenses =
        ConsolidatedExpenses(consolidatedExpensesYesOrNo = true, consolidatedExpensesAmount = Some(expenses))
      val userAnswers =
        UserAnswers("test").set(ConsolidatedExpensesPage(RentalsRentARoom), consolidatedExpenses).toOption

      val context =
        JourneyContext(
          taxYear = taxYear,
          mtditid = "mtditid",
          nino = "nino",
          journeyPath = JourneyPath.RentalsAndRentARoomExpenses
        )
      val rentalsExpense =
        RentalsAndRentARoomExpenses(Some(consolidatedExpenses), None, None, None, None, None, None, None)

      when(
        propertySubmissionService
          .saveJourneyAnswers(
            ArgumentMatchers.eq(context),
            ArgumentMatchers.eq(rentalsExpense),
            ArgumentMatchers.eq("incomeSourceId")
          )(
            any(),
            any()
          )
      ) thenReturn Future(Right())

      val application = applicationBuilder(userAnswers = userAnswers, isAgent = true)
        .overrides(
          bind[PropertySubmissionService].toInstance(propertySubmissionService),
          bind[BusinessService].toInstance(mockBusinessService),
          bind[AuditService].toInstance(mockAuditService)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.RentalsAndRaRExpensesCheckYourAnswersController.onSubmit(taxYear).url)

        val result = route(application, request).value

        whenReady(result) { _ =>
          status(result) mustEqual SEE_OTHER
          // redirectLocation(result).value mustEqual onwardRoute.url
          verify(mockAuditService, times(wantedNumberOfInvocations = 1)).sendAuditEvent(any())(any(), any())
          verify(mockBusinessService, times(wantedNumberOfInvocations = 1)).getUkPropertyDetails(any(), any())(any())
        }
      }
    }
  }
}
