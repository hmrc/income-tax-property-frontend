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

package controllers.rentalsandrentaroom.income

import audit.AuditService
import base.SpecBase
import connectors.error.ApiError
import models.backend.PropertyDetails
import models.{DeductingTax, RentalsRentARoom, ReversePremiumsReceived, User, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{doNothing, when}
import org.mockito.MockitoSugar.{times, verify}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import pages.propertyrentals.income._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.{BusinessService, PropertySubmissionService}
import viewmodels.govuk.SummaryListFluency
import views.html.rentalsandrentaroom.income.RentalsAndRentARoomIncomeCheckYourAnswersView

import java.time.LocalDate
import scala.concurrent.Future

class RentalsAndRentARoomIncomeCheckYourAnswersControllerSpec
    extends SpecBase with MockitoSugar with SummaryListFluency {
  private val taxYear = 2024

  val scenarios = Table[Boolean, String](
    ("isAgent", "individualOrAgent"),
    (false, "individual"),
    (true, "agent")
  )
  forAll(scenarios) { (isAgent: Boolean, agencyOrIndividual: String) =>
    val user = User(
      "",
      "",
      "",
      agentRef = Option.when(isAgent)("agentReferenceNumber")
    )
    s"RentalsAndRentARoomIncomeCheckYourAnswers Controller for $agencyOrIndividual" - {

      "must return OK and the correct view for a GET" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), false)
          .build()

        running(application) {
          val request = FakeRequest(
            GET,
            controllers.rentalsandrentaroom.income.routes.RentalsAndRentARoomIncomeCheckYourAnswersController
              .onPageLoad(taxYear)
              .url
          )

          val result = route(application, request).value

          val list = SummaryListViewModel(Seq.empty)

          val view = application.injector.instanceOf[RentalsAndRentARoomIncomeCheckYourAnswersView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(list, taxYear)(request, messages(application)).toString

        }
      }

      "must call the right services for a POST" in {
        val mockAuditService = mock[AuditService]
        val mockPropertySubmissionService = mock[PropertySubmissionService]
        val mockBusinessService = mock[BusinessService]
        val userAnswers =
          (for {
            ua1 <- UserAnswers(userAnswersId).set(IsNonUKLandlordPage(RentalsRentARoom), true)
            ua2 <- ua1.set(PropertyRentalIncomePage(RentalsRentARoom), BigDecimal(1.01))
            ua3 <- ua2.set(OtherIncomeFromPropertyPage(RentalsRentARoom), BigDecimal(1.01))
            ua4 <- ua3.set(DeductingTaxPage(RentalsRentARoom), DeductingTax(true, Some(BigDecimal(1.01))))
            ua5 <- ua4.set(
                     ReversePremiumsReceivedPage(RentalsRentARoom),
                     ReversePremiumsReceived(true, Some(BigDecimal(1.01)))
                   )
          } yield ua5).success.value

        when(mockPropertySubmissionService.saveJourneyAnswers(any(), any())(any(), any())) thenReturn Future.successful(
          Right(())
        )
        when(mockBusinessService.getUkPropertyDetails(any(), any())(any())) thenReturn Future
          .successful[Either[ApiError, Option[PropertyDetails]]](
            Right(
              Some(PropertyDetails(Some("incomeSourceTyoe"), Some(LocalDate.now()), Some(true), "incomeSourceId"))
            )
          )
        doNothing().when(mockAuditService).sendAuditEvent(any())(any(), any())

        val application = applicationBuilder(userAnswers = Some(userAnswers), false)
          .overrides(
            bind[AuditService].toInstance(mockAuditService),
            bind[PropertySubmissionService].toInstance(mockPropertySubmissionService),
            bind[BusinessService].toInstance(mockBusinessService)
          )
          .build()

        running(application) {
          val request = FakeRequest(
            POST,
            controllers.rentalsandrentaroom.income.routes.RentalsAndRentARoomIncomeCheckYourAnswersController
              .onSubmit(taxYear)
              .url
          )

          val result = route(application, request).value

          whenReady(result) { r =>
            verify(mockAuditService, times(1)).sendAuditEvent(any())(any(), any())
            verify(mockBusinessService, times(1)).getUkPropertyDetails(any(), any())(any())
          }

        }
      }
    }
  }
}
