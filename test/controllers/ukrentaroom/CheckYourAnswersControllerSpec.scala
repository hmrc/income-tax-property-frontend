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

package controllers.ukrentaroom

import audit.AuditService
import base.SpecBase
import models.{BalancingCharge, ClaimExpensesOrRRR, PropertyType, RaRAbout, RentARoom, RentalsAndRaRAbout, RentalsAndRentARoom, UserAnswers}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers._
import org.mockito.MockitoSugar.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.ukrentaroom.AboutSectionCompletePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.PropertySubmissionService
import viewmodels.govuk.SummaryListFluency
import views.html.ukrentaroom.CheckYourAnswersView

import scala.concurrent.Future
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar {

  val taxYear: Int = 2023

  def onwardRoute: Call =
    Call("GET", "/update-and-submit-income-tax-return/property/2023/uk-rent-a-room/about-section-complete-yes-no")

  val scenarios = Table[String, PropertyType](
    ("PropertyType Name", "PropertyType"),
    ("RaR", RentARoom),
    ("RentalsAndRaR", RentalsAndRentARoom)
  )

  forAll(scenarios) { (propertyTypeName: String, propertyType: PropertyType) =>
    s"UK Rent a Room Check Your Answers Controller $propertyTypeName" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), true).build()

        running(application) {
          val request =
            FakeRequest(
              GET,
              controllers.ukrentaroom.routes.CheckYourAnswersController.onPageLoad(taxYear, propertyType).url
            )

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckYourAnswersView]
          val list = SummaryListViewModel(Seq.empty)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(list, taxYear, propertyType)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, true).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(taxYear, propertyType).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to journey recovery page, when there is no data for rent a room about in session" in {
        val userAnswers = UserAnswers("test").set(AboutSectionCompletePage, false).get
        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

        running(application) {
          val request = FakeRequest(
            POST,
            controllers.ukrentaroom.routes.CheckYourAnswersController.onSubmit(taxYear, propertyType).url
          )

          val result = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        }
      }

      "must return OK and the correct view for a POST (onSubmit)" in {
        val userAnswers = UserAnswers("test").set(AboutSectionCompletePage, false).get

        val propertyPeriodSubmissionService: PropertySubmissionService = mock[PropertySubmissionService]
        val audit: AuditService = mock[AuditService]

        val rarAbout =
          RaRAbout(ukRentARoomJointlyLet = true, 22.23, ClaimExpensesOrRRR(claimRRROrExpenses = false, Some(22.11)))
        val userAnswersWithRaRAbout =
          userAnswers.set(RaRAbout, rarAbout).get

        val rentalsAndRaRAbout =
          RentalsAndRaRAbout(
            ukRentARoomJointlyLet = true,
            22.23,
            BalancingCharge(true, Some(33.45)),
            ClaimExpensesOrRRR(claimRRROrExpenses = false, Some(22.11))
          )

        val userAnswersWithRentalsAndRaRAbout =
          userAnswers.set(RentalsAndRaRAbout, rentalsAndRaRAbout).get

        val userAnswersForPropertyType = propertyType match {
          case RentARoom           => userAnswersWithRaRAbout
          case RentalsAndRentARoom => userAnswersWithRentalsAndRaRAbout
        }
        propertyType match {
          case RentARoom =>
            when(
              propertyPeriodSubmissionService.saveJourneyAnswers(any(), ArgumentMatchers.eq(rarAbout))(any(), any())
            ) thenReturn Future.successful(Right(()))
            when(
              propertyPeriodSubmissionService.saveJourneyAnswers(any(), ArgumentMatchers.eq(rarAbout))(any(), any())
            ) thenReturn Future.successful(Right(()))

          case RentalsAndRentARoom =>
            when(
              propertyPeriodSubmissionService
                .saveJourneyAnswers(any(), ArgumentMatchers.eq(rentalsAndRaRAbout))(any(), any())
            ) thenReturn Future.successful(Right(()))
            when(
              propertyPeriodSubmissionService
                .saveJourneyAnswers(any(), ArgumentMatchers.eq(rentalsAndRaRAbout))(any(), any())
            ) thenReturn Future.successful(Right(()))

        }

        val application = applicationBuilder(userAnswers = Some(userAnswersForPropertyType), isAgent = false)
          .overrides(
            bind[PropertySubmissionService].toInstance(propertyPeriodSubmissionService),
            bind[AuditService].toInstance(audit)
          )
          .build()

        running(application) {
          val request = FakeRequest(
            POST,
            controllers.ukrentaroom.routes.CheckYourAnswersController.onSubmit(taxYear, propertyType).url
          )

          val result = route(application, request).value
          status(result) mustEqual SEE_OTHER

          propertyType match {
            case RentARoom           => verify(audit, times(1)).sendRentARoomAuditEvent(any())(any(), any())
            case RentalsAndRentARoom => verify(audit, times(1)).sendRentalsAndRentARoomAuditEvent(any())(any(), any())
          }

          redirectLocation(result).value mustEqual controllers.ukrentaroom.routes.AboutSectionCompleteController
            .onPageLoad(taxYear)
            .url

        }
      }
    }
  }
}
