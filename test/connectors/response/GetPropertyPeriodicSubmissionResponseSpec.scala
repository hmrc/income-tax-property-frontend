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

package connectors.response

import models.PropertyAbout
import connectors.error.{SingleErrorBody, ApiError}
import connectors.response.GetPropertyPeriodicSubmissionResponse.getPropertyPeriodicSubmissionResponseReads
import models.ForeignWhenYouReportedTheLoss.y2018to2019
import models.TotalIncome.Under
import models.WhenYouReportedTheLoss.y2021to2022
import models._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status._
import play.api.libs.json.{Json, JsValue}
import uk.gov.hmrc.http.HttpResponse

import java.time.LocalDate

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
                4.2,
                Some(UnusedLossesBroughtForward(unusedLossesBroughtForwardYesOrNo = true, Some(4.2))),
                Some(y2021to2022)
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
            None,
            List(),
            foreignPropertySelectCountry = Some(
              ForeignPropertySelectCountry(
                totalIncome = Under,
                reportPropertyIncome = Some(false),
                incomeCountries = None,
                addAnotherCountry = None,
                claimPropertyIncomeAllowance = None
              )
            )
          )

        val foreignPropertyData = FetchedForeignPropertyData(
          foreignPropertyTax = Some(
            Map(
              "ESP" -> ForeignPropertyTax(
                Some(ForeignIncomeTax(foreignIncomeTaxYesNo = true, Some(BigDecimal(456.00)))),
                Some(false)
              )
            )
          ),
          foreignPropertyIncome = Some(
            Map(
              "ESP" -> ForeignIncomeAnswers(
                Some(123.45),
                premiumsGrantLeaseReceived = false,
                otherPropertyIncome = Some(456.7),
                calculatedPremiumLeaseTaxable = Some(PremiumCalculated(calculatedPremiumLeaseTaxable = false, None)),
                receivedGrantLeaseAmount = None,
                twelveMonthPeriodsInLease = None,
                premiumsOfLeaseGrantAgreed = None
              )
            )
          ),
          foreignPropertyExpenses = Some(
            Map(
              "ESP" -> ForeignExpensesAnswers(
                Some(ConsolidatedOrIndividualExpenses(consolidatedOrIndividualExpensesYesNo = true, Some(456))),
                premisesRunningCosts = Some(11),
                repairsAndMaintenance = Some(22.30),
                financialCosts = Some(44),
                professionalFees = Some(56),
                costOfServices = Some(78),
                other = Some(90)
              )
            )
          ),
          foreignJourneyStatuses = Some(Map("ESP" -> List(JourneyWithStatus("foreign-property-tax", "completed")))),
          foreignPropertyAllowances = Some(
            Map(
              "ESP" -> ForeignPropertyAllowances(
                annualInvestmentAllowance = Some(15.15),
                costOfReplacingDomesticItems = Some(25.25),
                zeroEmissionsGoodsVehicleAllowance = Some(35.35),
                otherCapitalAllowance = Some(45.45),
                electricChargePointAllowance = Some(55.55),
                structuredBuildingAllowance = Some(
                  Seq(
                    StructuredBuildingAllowance(
                      amount = 65.55,
                      Some(
                        StructuredBuildingAllowanceDate(
                          qualifyingDate = LocalDate.now(),
                          qualifyingAmountExpenditure = 50.00
                        )
                      ),
                      building = StructuredBuildingAllowanceBuilding(
                        name = Some("name"),
                        number = Some("number"),
                        postCode = "AB1 2XY"
                      )
                    )
                  )
                ),
                zeroEmissionsCarAllowance = Some(75.75),
                propertyAllowance = Some(85.85),
                capitalAllowancesForACar = None
              )
            )
          ),
          foreignPropertySba = Some(
            Map(
              "ESP" -> ForeignSbaAnswers(
                claimStructureBuildingAllowance = true,
                allowances = Some(
                  Seq(
                    StructuredBuildingAllowance(
                      amount = 65.55,
                      Some(
                        StructuredBuildingAllowanceDate(
                          qualifyingDate = LocalDate.now(),
                          qualifyingAmountExpenditure = 50.00
                        )
                      ),
                      building = StructuredBuildingAllowanceBuilding(
                        name = Some("name"),
                        number = Some("number"),
                        postCode = "AB1 2XY"
                      )
                    )
                  )
                )
              )
            )
          ),
          foreignPropertyAdjustments = Some(
            Map(
              "ESP" -> ForeignAdjustmentsAnswers(
                privateUseAdjustment = Some(BigDecimal(54.00)),
                balancingCharge = Some(BalancingCharge(balancingChargeYesNo = true, Some(BigDecimal(146.56)))),
                residentialFinanceCost = Some(BigDecimal(99.00)),
                unusedResidentialFinanceCost = Some(
                  ForeignUnusedResidentialFinanceCost(
                    foreignUnusedResidentialFinanceCostYesNo = true,
                    Some(BigDecimal(56.77))
                  )
                ),
                propertyIncomeAllowanceClaim = Some(BigDecimal(15.00)),
                unusedLossesPreviousYears =
                  Some(UnusedLossesPreviousYears(unusedLossesPreviousYearsYesNo = true, Some(BigDecimal(45.00)))),
                whenYouReportedTheLoss = Some(y2018to2019)
              )
            )
          )
        )
        val ukAndForeignPropertyData: FetchedUkAndForeignData = FetchedUkAndForeignData(
          None
        )
        val propertyPeriodicSubmissionResponse =
          FetchedPropertyData(ukPropertyData, foreignPropertyData, ukAndForeignPropertyData)

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
