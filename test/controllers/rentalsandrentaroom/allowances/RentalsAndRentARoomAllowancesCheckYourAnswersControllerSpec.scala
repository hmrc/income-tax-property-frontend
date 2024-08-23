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

package controllers.rentalsandrentaroom.allowances

import audit.AuditService
import base.SpecBase
import models.backend.PropertyDetails
import models.{CapitalAllowancesForACar, RentalsRentARoom, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.{times, verify}
import org.scalatest.prop.TableFor2
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import pages.allowances._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.{BusinessService, PropertySubmissionService}
import viewmodels.govuk.SummaryListFluency
import views.html.rentalsandrentaroom.allowances.RentalsAndRentARoomAllowancesCheckYourAnswersView

import java.time.LocalDate
import scala.concurrent.Future

class RentalsAndRentARoomAllowancesCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {
  private val taxYear = 2024
  val scenarios: TableFor2[Boolean, String] = Table[Boolean, String](
    ("isAgent", "individualOrAgent"),
    (false, "individual"),
    (true, "agent")
  )
  forAll(scenarios) { (isAgent: Boolean, agencyOrIndividual: String) =>
    s"RentalsAndRentARoomAllowancesCheckYourAnswers Controller $agencyOrIndividual" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent).build()

        running(application) {
          val request = FakeRequest(
            GET,
            controllers.rentalsandrentaroom.allowances.routes.RentalsAndRentARoomAllowancesCheckYourAnswersController
              .onPageLoad(taxYear)
              .url
          )

          val result = route(application, request).value

          val list = SummaryListViewModel(Seq.empty)

          val view = application.injector.instanceOf[RentalsAndRentARoomAllowancesCheckYourAnswersView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(list, taxYear)(request, messages(application)).toString
        }
      }

      "must call the right services for a POST" in {
        val mockPropertySubmissionService = mock[PropertySubmissionService]
        val mockAuditService = mock[AuditService]
        val mockBusinessService = mock[BusinessService]

        val userAnswers =
          (for {
            ua1 <-
              UserAnswers(userAnswersId)
                .set(
                  CapitalAllowancesForACarPage(RentalsRentARoom),
                  CapitalAllowancesForACar(capitalAllowancesForACarYesNo = true, Some(1.23))
                )
            ua2 <- ua1.set(AnnualInvestmentAllowancePage(RentalsRentARoom), BigDecimal(1.01))
            ua3 <- ua2.set(ZeroEmissionCarAllowancePage(RentalsRentARoom), BigDecimal(1.01))
            ua4 <- ua3.set(ZeroEmissionGoodsVehicleAllowancePage(RentalsRentARoom), BigDecimal(1.01))
            ua5 <- ua4.set(BusinessPremisesRenovationPage(RentalsRentARoom), BigDecimal(1.01))
            ua6 <- ua5.set(ReplacementOfDomesticGoodsPage(RentalsRentARoom), BigDecimal(1.01))
            ua7 <- ua6.set(OtherCapitalAllowancePage(RentalsRentARoom), BigDecimal(1.01))
          } yield ua7).success.value

        when(mockPropertySubmissionService.saveJourneyAnswers(any(), any())(any(), any())) thenReturn Future.successful(
          Right(())
        )

        when(mockBusinessService.getUkPropertyDetails(any(), any())(any())) thenReturn Future.successful(
          Right(
            Option(
              PropertyDetails(
                incomeSourceType = Some("incomeSourceId"),
                tradingStartDate = Some(LocalDate.now),
                accrualsOrCash = Some(true),
                incomeSourceId = "incomeSourceId"
              )
            )
          )
        )

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false)
          .overrides(
            bind[PropertySubmissionService].toInstance(mockPropertySubmissionService),
            bind[BusinessService].toInstance(mockBusinessService),
            bind[AuditService].toInstance(mockAuditService)
          )
          .build()

        running(application) {
          val request = FakeRequest(
            POST,
            controllers.rentalsandrentaroom.allowances.routes.RentalsAndRentARoomAllowancesCheckYourAnswersController
              .onSubmit(taxYear)
              .url
          )

          val result = route(application, request).value

          whenReady(result) { _ =>
            verify(mockPropertySubmissionService, times(1)).saveJourneyAnswers(any(), any())(any(), any())
            verify(mockAuditService, times(1)).sendAuditEvent(any())(any(), any())
          }

        }
      }
    }
  }
}
