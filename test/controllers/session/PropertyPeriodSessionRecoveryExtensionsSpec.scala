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

package controllers.session

import base.SpecBase
import controllers.session.PropertyPeriodSessionRecoveryExtensions._
import models._
import org.scalatestplus.mockito.MockitoSugar
import pages.adjustments._
import pages.enhancedstructuresbuildingallowance._
import pages.premiumlease.{CalculatedFigureYourselfPage, ReceivedGrantLeaseAmountPage}
import pages.propertyrentals.expenses._
import pages.propertyrentals.income.{IncomeFromPropertyRentalsPage, IsNonUKLandlordPage, ReversePremiumsReceivedPage}
import pages.propertyrentals.{ClaimPropertyIncomeAllowancePage, ExpensesLessThan1000Page}
import pages.structurebuildingallowance._
import pages.ukrentaroom.adjustments.RaRBalancingChargePage
import pages.ukrentaroom.allowances._
import pages.ukrentaroom.{ClaimExpensesOrRRRPage, JointlyLetPage, TotalIncomeAmountPage}
import pages.{TotalIncomePage, UKPropertyPage}
import play.api.libs.json.Json
import testHelpers.Fixture

import java.time.LocalDate

class PropertyPeriodSessionRecoveryExtensionsSpec extends SpecBase with MockitoSugar with Fixture {
  val data: String =
    """{
      |        "propertyAbout": {
      |            "totalIncome" : "between",
      |            "ukProperty" : [
      |               "property.rentals"
      |             ]
      |        },
      |        "esbasWithSupportingQuestions": {
      |        "claimEnhancedStructureBuildingAllowance" : true,
      |        "esbas" : [
      |            {
      |                "esbaQualifyingDate" : "2022-01-04",
      |                "esbaQualifyingAmount" : 5,
      |                "esbaClaim" : 6,
      |                "esbaAddress" : {
      |                    "buildingName" : "12",
      |                    "buildingNumber" : "12",
      |                    "postCode" : "EH1 AB1"
      |                }
      |            },
      |            {
      |                "esbaQualifyingDate" : "2022-03-03",
      |                "esbaQualifyingAmount" : 4,
      |                "esbaClaim" : 5,
      |                "esbaAddress" : {
      |                    "buildingName" : "2",
      |                    "buildingNumber" : "2",
      |                    "postCode" : "EH1 AB1"
      |                }
      |            }
      |        ],
      |        "esbaClaims" : false
      |        },
      |        "propertyRentalsAbout" : {
      |            "toexpensesLessThan1000" : false,
      |            "claimPropertyIncomeAllowanceYesOrNo" : false
      |        },
      |        "propertyRentalsIncome" : {
      |            "isNonUKLandlord" : false,
      |            "incomeFromPropertyRentals" : 45,
      |            "leasePremiumPaymentYesOrNo" : true,
      |            "calculatedFigureYourself" : {
      |                "calculatedFigureYourself" : true,
      |                "amount" : 45
      |            },
      |            "receivedGrantLeaseAmount": 6,
      |            "reversePremiumsReceived" : {
      |                "reversePremiumsReceived" : true,
      |                "amount" : 45
      |            },
      |            "otherIncomeFromProperty" : 45
      |        },
      |        "propertyRentalsExpenses" : {
      |            "consolidatedExpenses" : {
      |                "consolidatedExpensesYesOrNo" : false
      |            },
      |            "rentsRatesAndInsurance" : 55,
      |            "repairsAndMaintenanceCosts" : 7,
      |            "loanInterestOrOtherFinancialCost" : 56,
      |            "otherProfessionalFees" : 4,
      |            "costsOfServicesProvided" : 34,
      |            "propertyBusinessTravelCosts" : 4,
      |            "otherAllowablePropertyExpenses" : 3
      |        },
      |        "allowances" : {
      |            "annualInvestmentAllowance" : 44,
      |            "electricChargePointAllowance" : {
      |                "electricChargePointAllowanceYesOrNo" : true,
      |                "electricChargePointAllowanceAmount" : 45
      |            },
      |            "zeroEmissionCarAllowance" : 4,
      |            "zeroEmissionGoodsVehicleAllowance" : 4,
      |            "businessPremisesRenovationAllowance" : 4,
      |            "replacementOfDomesticGoodsAllowance" : 4,
      |            "otherCapitalAllowance" : 4
      |        },
      |        "sbasWithSupportingQuestions": {
      |        "claimStructureBuildingAllowance" : true,
      |        "sbaClaims" : true,
      |        "sbas" : [
      |            {
      |                "structureBuildingQualifyingDate" : "2022-04-03",
      |                "structureBuildingQualifyingAmount" : 3,
      |                "structureBuildingAllowanceClaim" : 4,
      |                "structuredBuildingAllowanceAddress" : {
      |                    "buildingName" : "3",
      |                    "buildingNumber" : "3",
      |                    "postCode" : "EH1 AB2"
      |                }
      |            },
      |            {
      |                "structureBuildingQualifyingDate" : "2022-02-02",
      |                "structureBuildingQualifyingAmount" : 4,
      |                "structureBuildingAllowanceClaim" : 5,
      |                "structuredBuildingAllowanceAddress" : {
      |                    "buildingName" : "4",
      |                    "buildingNumber" : "4",
      |                    "postCode" : "EH1 AB2"
      |                }
      |            }
      |        ]
      |        },
      |        "adjustments" : {
      |            "privateUseAdjustment" : 2,
      |            "balancingCharge" : {
      |                "balancingChargeYesNo" : true,
      |                "balancingChargeAmount" : 3
      |            },
      |            "propertyIncomeAllowance" : 4,
      |            "renovationAllowanceBalancingCharge" : {
      |                "renovationAllowanceBalancingChargeYesNo" : true,
      |                "renovationAllowanceBalancingChargeAmount" : 23
      |            },
      |            "residentialFinanceCost" : 2,
      |            "unusedResidentialFinanceCost" : 3
      |
      |    },
      |    "raRAbout" : {
      |            "jointlyLetYesOrNo" : false,
      |            "totalIncomeAmount" : 30,
      |            "claimExpensesOrRRR" : {
      |                "claimRRROrExpenses" : false,
      |                "rentARoomAmount" : 50
      |            }
      |        },
      |    "rentARoomAllowances" : {
      |        "capitalAllowancesForACar" : {
      |            "capitalAllowancesForACarYesNo" : true,
      |            "capitalAllowancesForACarAmount" : 20
      |        },
      |        "annualInvestmentAllowance" : 5,
      |        "electricChargePointAllowance" : 30,
      |        "zeroEmissionCarAllowance" : 35,
      |        "zeroEmissionGoodsVehicleAllowance" : 10,
      |        "replacementOfDomesticGoodsAllowance" : 25,
      |        "otherCapitalAllowance" : 20
      |    },
      |    "raRAdjustments" : {
      |        "balancingCharge" : {
      |            "balancingChargeYesNo" : true,
      |            "balancingChargeAmount" : 10
      |        },
      |        "unusedResidentialPropertyFinanceCostsBroughtFwd": 25
      |    }
      |}""".stripMargin

  "PropertyPeriodSessionRecoveryExtensionsSpec" - {
    "should update the session data correctly" in {
      val fetchedData: FetchedBackendData = Json.parse(data).as[FetchedBackendData] // fetchedPropertyData

      val updated = emptyUserAnswers
        .update(fetchedData)

      updated.get(TotalIncomePage).get mustBe TotalIncome.Between
      updated.get(UKPropertyPage).get mustBe Set(UKPropertySelect.PropertyRentals)
      updated.get(ExpensesLessThan1000Page).get mustBe false
      updated.get(ClaimPropertyIncomeAllowancePage(Rentals)).get mustBe false
      updated.get(IsNonUKLandlordPage).get mustBe false
      updated.get(IncomeFromPropertyRentalsPage).get mustBe 45
      updated.get(ReceivedGrantLeaseAmountPage) mustBe None // Lease clean up test
//Todo: To be uncommented, and added to tests when related tickets implemented. updated.get(YearLeaseAmountPage).get mustBe 5
//Todo: To be uncommented, and added to tests when related tickets implemented. updated.get(ConsolidatedExpensesPage).get mustBe ConsolidatedExpenses(false, None)
//Todo: To be uncommented, and added to tests when related tickets implemented. updated.get(RentsRatesAndInsurancePage).get mustBe 55
      // Todo: do we need this? updated.get(LeasePremiumPaymentPage).get mustBe true
      updated.get(CalculatedFigureYourselfPage).get mustBe CalculatedFigureYourself(true, Some(45))
      updated.get(ReversePremiumsReceivedPage).get mustBe ReversePremiumsReceived(true, Some(45))
      updated.get(JointlyLetPage(RentARoom)).get mustBe false
      updated.get(TotalIncomeAmountPage(RentARoom)).get mustBe 30
      updated.get(ClaimExpensesOrRRRPage(RentARoom)).get mustBe ClaimExpensesOrRRR(false, Some(50))
      updated.get(RepairsAndMaintenanceCostsPage).get mustBe 7
      updated.get(LoanInterestPage).get mustBe 56
      updated.get(OtherProfessionalFeesPage).get mustBe 4
      updated.get(CostsOfServicesProvidedPage).get mustBe 34
      updated.get(PropertyBusinessTravelCostsPage).get mustBe 4
      updated.get(OtherAllowablePropertyExpensesPage).get mustBe 3
      updated.get(PrivateUseAdjustmentPage).get mustBe PrivateUseAdjustment(2)
      updated.get(BalancingChargePage).get mustBe BalancingCharge(true, Some(3))
      updated.get(PropertyIncomeAllowancePage).get mustBe 4
      updated.get(ReversePremiumsReceivedPage).get mustBe ReversePremiumsReceived(true, Some(45))
      updated.get(RenovationAllowanceBalancingChargePage).get mustBe RenovationAllowanceBalancingCharge(true, Some(23))
      updated.get(ResidentialFinanceCostPage).get mustBe 2
      updated.get(UnusedResidentialFinanceCostPage).get mustBe 3
      updated.get(ClaimEsbaPage).get mustBe true
      updated.get(EsbaAddressPage(0)).get mustBe EsbaAddress(
        "12",
        "12",
        "EH1 AB1"
      )
      updated.get(EsbaQualifyingDatePage(0)).get mustBe LocalDate.parse("2022-01-04")
      updated.get(EsbaQualifyingAmountPage(0)).get mustBe 5
      updated.get(EsbaClaimPage(0)).get mustBe 6
      updated.get(EsbaAddressPage(1)).get mustBe EsbaAddress(
        "2",
        "2",
        "EH1 AB1"
      )
      updated.get(EsbaQualifyingDatePage(1)).get mustBe LocalDate.parse("2022-03-03")
      updated.get(EsbaQualifyingAmountPage(1)).get mustBe 4
      updated.get(EsbaClaimPage(1)).get mustBe 5

// Todo: To be uncommented, and added to tests when related tickets implemented. updated.get(CapitalAllowancesForACarPage).get mustBe CapitalAllowancesForACar(false, None)
      updated.get(StructureBuildingQualifyingDatePage(0)).get mustBe LocalDate.parse("2022-04-03")
      updated.get(StructureBuildingQualifyingAmountPage(0)).get mustBe 3
      updated.get(StructureBuildingAllowanceClaimPage(0)).get mustBe 4
      updated.get(StructuredBuildingAllowanceAddressPage(0)).get mustBe StructuredBuildingAllowanceAddress(
        "3",
        "3",
        "EH1 AB2"
      )
      updated.get(StructureBuildingQualifyingDatePage(1)).get mustBe LocalDate.parse("2022-02-02")
      updated.get(StructureBuildingQualifyingAmountPage(1)).get mustBe 4
      updated.get(StructureBuildingAllowanceClaimPage(1)).get mustBe 5
      updated.get(StructuredBuildingAllowanceAddressPage(1)).get mustBe StructuredBuildingAllowanceAddress(
        "4",
        "4",
        "EH1 AB2"
      )
      updated.get(RaRBalancingChargePage).get mustBe BalancingCharge(true, Some(10))

      updated.get(RaRCapitalAllowancesForACarPage).get mustBe CapitalAllowancesForACar(true, Some(20))
      updated.get(RaRAnnualInvestmentAllowancePage).get mustBe 5
      updated.get(RaRElectricChargePointAllowanceForAnEVPage).get mustBe 30
      updated.get(RaRZeroEmissionCarAllowancePage).get mustBe 35
      updated.get(RaRZeroEmissionGoodsVehicleAllowancePage).get mustBe 10
      updated.get(RaRReplacementsOfDomesticGoodsPage).get mustBe 25
      updated.get(RaROtherCapitalAllowancesPage).get mustBe 20

    }
  }
}
