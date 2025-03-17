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

package controllers.ukrentaroom.expenses

import audit.{AuditService, RentARoomExpenses}
import base.SpecBase
import controllers.ukrentaroom.expenses.routes._
import models.{JourneyContext, JourneyPath, RentARoom, UserAnswers}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import pages.PageConstants.expensesPath
import play.api.inject.bind
import play.api.libs.json.{JsNumber, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.PropertySubmissionService
import viewmodels.govuk.SummaryListFluency
import views.html.ukrentaroom.expenses.ExpensesCheckYourAnswersRRView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ExpensesCheckYourAnswersRRControllerSpec extends SpecBase with SummaryListFluency {

  val taxYear = 2024
  val context: JourneyContext =
    JourneyContext(taxYear = taxYear, mtditid = "mtditid", nino = "nino", journeyPath = JourneyPath.RentARoomExpenses)

  def onwardRoute: Call =
    Call("GET", "/update-and-submit-income-tax-return/property/2024/rent-a-room/expenses/complete-yes-no")

  "ExpensesCheckYourRRAnswers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()
      val list = SummaryListViewModel(Seq.empty)
      val taxYear = 2024
      running(application) {

        val request = FakeRequest(GET, ExpensesCheckYourAnswersRRController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ExpensesCheckYourAnswersRRView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, taxYear)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val taxYear = 2023
      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, ExpensesCheckYourAnswersRRController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the correct view for a POST (onSubmit)" in {

      val rentsRatesAndInsurance = 100
      val repairsAndMaintenanceCosts = 200
      val legalManagementOtherFee = 300
      val residentialPropertyFinanceCosts = 400
      val costsOfServicesProvided = 500
      val unusedResidentialPropertyFinanceCostsBroughtFwd = 600
      val otherPropertyExpenses = 700

      val answers = UserAnswers(
        id = "rent-a-room-expenses-userId",
        data = Json.obj(
          expensesPath(RentARoom) -> Json.obj(
            "rentsRatesAndInsurance"          -> JsNumber(rentsRatesAndInsurance),
            "repairsAndMaintenanceCosts"      -> JsNumber(repairsAndMaintenanceCosts),
            "legalManagementOtherFee"         -> JsNumber(legalManagementOtherFee),
            "residentialPropertyFinanceCosts" -> JsNumber(residentialPropertyFinanceCosts),
            "costOfServicesProvided"          -> JsNumber(costsOfServicesProvided),
            "unusedResidentialPropertyFinanceCostsBroughtFwd" -> JsNumber(
              unusedResidentialPropertyFinanceCostsBroughtFwd
            ),
            "otherPropertyExpenses" -> JsNumber(otherPropertyExpenses)
          )
        )
      )
      val application = applicationBuilder(userAnswers = Some(answers), isAgent = true)
        .overrides(bind[PropertySubmissionService].toInstance(propertySubmissionService))
        .overrides(bind[AuditService].toInstance(audit))
        .build()

      when(
        propertySubmissionService
          .saveJourneyAnswers(
            ArgumentMatchers.eq(context),
            ArgumentMatchers.eq(
              RentARoomExpenses(
                consolidatedExpenses = None,
                rentsRatesAndInsurance = Some(rentsRatesAndInsurance),
                repairsAndMaintenanceCosts = Some(repairsAndMaintenanceCosts),
                costOfServicesProvided = Some(costsOfServicesProvided),
                legalManagementOtherFee = Some(legalManagementOtherFee),
                otherPropertyExpenses = Some(otherPropertyExpenses)
              )
            )
          )(
            any(),
            any()
          )
      ) thenReturn Future(Right())

      running(application) {
        val request = FakeRequest(POST, routes.ExpensesCheckYourAnswersRRController.onSubmit(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(audit, times(1)).sendRentARoomAuditEvent(any())(any(), any())
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }
}
