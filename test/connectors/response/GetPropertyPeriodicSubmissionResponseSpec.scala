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

package connectors.response

import audit.PropertyAbout
import connectors.error.{ApiError, SingleErrorBody}
import connectors.response.GetPropertyPeriodicSubmissionResponse.getPropertyPeriodicSubmissionResponseReads
import models.ForeignTotalIncome.LessThanOneThousand
import models._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpResponse

class GetPropertyPeriodicSubmissionResponseSpec extends AnyWordSpec with Matchers {

  private val anyHeaders: Map[String, Seq[String]] = Map.empty
  private val anyMethod: String = "GET"
  private val anyUrl = "/any-url"

  private val underTest = getPropertyPeriodicSubmissionResponseReads

  "GetPropertyPeriodicSubmissionResponse" should {
    "convert JsValue to GetPropertyPeriodicSubmissionResponse" when {
      "status is OK and valid jsValue" in {

        val ukPropertyData =
          FetchedBackendData(
            Some(CapitalAllowancesForACar(capitalAllowancesForACarYesNo = true, Some(3.2))),
            Some(
              PropertyAbout(
                TotalIncome.Between,
                Some(Seq(UKPropertySelect.PropertyRentals)),
                Some(true)
              )
            ),
            None,
            None,
            Some(
              Adjustments(
                BalancingCharge(balancingChargeYesNo = true, Some(4.2)),
                PrivateUseAdjustment(4.5),
                45,
                RenovationAllowanceBalancingCharge(renovationAllowanceBalancingChargeYesNo = true, Some(4.2)),
                4.2,
                4.2
              )
            ),
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            List(),
            foreignPropertySelectCountry = Some(
              ForeignPropertySelectCountry(
                totalIncome = LessThanOneThousand,
                reportPropertyIncome = Some(false),
                incomeCountries = None,
                addAnotherCountry = None,
                claimPropertyIncomeAllowance = None
              )
            )
          )

        val foreignPropertyData = FetchedForeignPropertyData(
          foreignPropertyTax = Some(Map("ESP" -> ForeignPropertyTax(Some(ForeignIncomeTax(foreignIncomeTaxYesNo = true, Some(BigDecimal(456.00)))), Some(false)))),
          foreignPropertyIncome = Some(Map("ESP" -> ForeignIncomeAnswers(Some(123.45), premiumsGrantLeaseReceived = false,
            reversePremiumsReceived = Some(ReversePremiumsReceived(reversePremiumsReceived = true, reversePremiums = Some(123.5))),
            otherPropertyIncome = Some(456.7),
            calculatedPremiumLeaseTaxable = Some(PremiumCalculated(calculatedPremiumLeaseTaxable = false, None)),
            receivedGrantLeaseAmount = None, twelveMonthPeriodsInLease = None, premiumsOfLeaseGrantAgreed = None))
          ),
          foreignPropertyExpenses = Some(Map("ESP" -> ForeignExpensesAnswers(
            Some(ConsolidatedOrIndividualExpenses(consolidatedOrIndividualExpensesYesNo = true, Some(456))), premisesRunningCosts = Some(11),
            repairsAndMaintenance = Some(22.30), financialCosts = Some(44), professionalFees = Some(56),
            costOfServices = Some(78), other = Some(90)))
          ),
          foreignJourneyStatuses = Some(Map("ESP" -> List(JourneyWithStatus("foreign-property-tax", "completed")))),
        )
        val propertyPeriodicSubmissionResponse = FetchedPropertyData(ukPropertyData, foreignPropertyData)

        val jsValue: JsValue = Json.toJson(propertyPeriodicSubmissionResponse)

        val httpResponse: HttpResponse = HttpResponse.apply(OK, jsValue, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe GetPropertyPeriodicSubmissionResponse(
          httpResponse,
          Right(propertyPeriodicSubmissionResponse)
        )
      }

      "status is OK and invalid jsValue" in {
        val jsValue: JsValue = Json.parse("""
                                            |{"capitalAllowancesForACar": "invalid"}
                                            |""".stripMargin)

        val httpResponse: HttpResponse = HttpResponse.apply(OK, jsValue, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe GetPropertyPeriodicSubmissionResponse(
          httpResponse,
          Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError))
        )
      }

      "status is NOT_FOUND and any jsValue" in {
        val jsValue: JsValue = Json.parse("""
                                            |{}
                                            |""".stripMargin)

        val httpResponse: HttpResponse = HttpResponse.apply(NOT_FOUND, jsValue, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe GetPropertyPeriodicSubmissionResponse(
          httpResponse,
          Left(ApiError(NOT_FOUND, SingleErrorBody.parsingError))
        )
      }

      "status is INTERNAL_SERVER_ERROR and jsValue for error" in {
        val jsValue: JsValue = Json.toJson(SingleErrorBody("some-code", "some-reason"))

        val httpResponse: HttpResponse = HttpResponse.apply(INTERNAL_SERVER_ERROR, jsValue, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe GetPropertyPeriodicSubmissionResponse(
          httpResponse,
          Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("some-code", "some-reason")))
        )
      }

      "status is SERVICE_UNAVAILABLE and jsValue for error" in {
        val jsValue: JsValue = Json.toJson(SingleErrorBody("some-code", "some-reason"))

        val httpResponse: HttpResponse = HttpResponse.apply(SERVICE_UNAVAILABLE, jsValue, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe GetPropertyPeriodicSubmissionResponse(
          httpResponse,
          Left(ApiError(SERVICE_UNAVAILABLE, SingleErrorBody("some-code", "some-reason")))
        )
      }

      "status is BAD_REQUEST and jsValue for error" in {
        val jsValue: JsValue = Json.toJson(SingleErrorBody("some-code", "some-reason"))

        val httpResponse: HttpResponse = HttpResponse.apply(BAD_REQUEST, jsValue, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe GetPropertyPeriodicSubmissionResponse(
          httpResponse,
          Left(ApiError(BAD_REQUEST, SingleErrorBody("some-code", "some-reason")))
        )
      }

      "status is OTHER and jsValue for error" in {
        val jsValue: JsValue = Json.toJson(SingleErrorBody("some-code", "some-reason"))

        val httpResponse: HttpResponse = HttpResponse.apply(FAILED_DEPENDENCY, jsValue, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe GetPropertyPeriodicSubmissionResponse(
          httpResponse,
          Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("some-code", "some-reason")))
        )
      }
    }
  }
}
