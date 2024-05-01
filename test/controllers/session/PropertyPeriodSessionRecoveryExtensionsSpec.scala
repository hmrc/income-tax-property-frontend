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
import pages.premiumlease.{ReceivedGrantLeaseAmountPage, YearLeaseAmountPage}
import pages.propertyrentals.expenses._
import pages.propertyrentals.income.{IncomeFromPropertyRentalsPage, IsNonUKLandlordPage}
import pages.propertyrentals.{ClaimPropertyIncomeAllowancePage, ExpensesLessThan1000Page}
import pages.structurebuildingallowance._
import pages.{CapitalAllowancesForACarPage, TotalIncomePage, UKPropertyPage}
import play.api.libs.json.{JsObject, JsValue, Json}

import java.time.LocalDate

class PropertyPeriodSessionRecoveryExtensionsSpec extends SpecBase with MockitoSugar {
  val data: String =
    """{
      |       "propertyAbout": {
      |             "totalIncome" : "between",
      |             "ukProperty" : [
      |                "property.rentals"
      |                  ]
      |        },
      |       "propertyRentalsAbout": {
      |              "expensesLessThan1000" : false,
      |              "claimPropertyIncomeAllowance" : false
      |        },
      |        "propertyRentalsIncome": {
      |           "isNonUKLandlord" : true,
      |           "deductingTax" : 9,
      |           "incomeFromPropertyRentals" : 8,
      |           "leasePremiumPayment" : 7,
      |           "receivedGrantLeaseAmount" : 6,
      |           "yearLeaseAmount" : 5,
      |           "premiumsGrantLease" : 4,
      |           "reversePremiumsReceived": 3,
      |           "calculatedFigureYourself" : false
      |        },
      |        "propertyRentalsExpense": {
      |           "consolidatedExpenses" : {
      |            "consolidatedExpensesYesOrNo" : false
      |           },
      |           "rentsRatesAndInsurance" : 8,
      |           "repairsAndMaintenanceCosts" : 7,
      |           "loanInterestOrOtherFinancialCost" : 6,
      |           "otherProfessionalFees" : 5,
      |           "costsOfServicesProvided" : 4,
      |           "propertyBusinessTravelCosts" : 3,
      |           "otherAllowablePropertyExpenses" : 2
      |        },
      |        "capitalAllowancesForACar" : {
      |            "CapitalAllowancesForACarYesNo" : false
      |        },
      |        "adjustments": {
      |            "privateUseAdjustment" : {
      |                "amount" : 4
      |            },
      |            "balancingCharge" : {
      |                "balancingChargeYesNo" : false
      |            },
      |            "propertyIncomeAllowance" : 0,
      |            "renovationAllowanceBalancingCharge" : {
      |                "renovationAllowanceBalancingChargeYesNo" : true,
      |                "renovationAllowanceBalancingChargeAmount" : 6
      |            },
      |            "residentialFinanceCost" : 4,
      |            "unusedResidentialFinanceCost" : 3
      |        },
      |        "claimStructureBuildingAllowance" : true,
      |        "structureBuildingFormGroup" : [
      |            {
      |                "structureBuildingQualifyingDate" : "2022-02-02",
      |                "structureBuildingQualifyingAmount" : 22,
      |                "structureBuildingAllowanceClaim" : 22,
      |                "structuredBuildingAllowanceAddress" : {
      |                    "buildingName" : "12",
      |                    "buildingNumber" : "12",
      |                    "postCode" : "EH1 AB1"
      |                }
      |            },
      |            {
      |                "structureBuildingQualifyingDate" : "2023-02-23",
      |                "structureBuildingQualifyingAmount" : 12,
      |                "structureBuildingAllowanceClaim" : 23,
      |                "structuredBuildingAllowanceAddress" : {
      |                    "buildingName" : "123",
      |                    "buildingNumber" : "231",
      |                    "postCode" : "EH1 AB1"
      |                }
      |            }
      |        ],
      |        "sbaClaims" : true,
      |        "sbaRemoveConfirmation" : true
      |    }""".stripMargin

  "PropertyPeriodSessionRecoveryExtensionsSpec" - {
    "should update the session data correctly" in {
      val fetchedData: FetchedPropertyData =
        FetchedPropertyData(JsObject(Json.parse(data).as[Map[String, JsValue]].toSeq))
      val updated = emptyUserAnswers
        .update(fetchedData)

      updated.get(TotalIncomePage).get mustBe TotalIncome.Between
      updated.get(UKPropertyPage).get mustBe Set(UKPropertySelect.PropertyRentals)
      updated.get(ExpensesLessThan1000Page).get mustBe false
      updated.get(ClaimPropertyIncomeAllowancePage).get mustBe false
      updated.get(IsNonUKLandlordPage).get mustBe true
      updated.get(IncomeFromPropertyRentalsPage).get mustBe 8
      updated.get(ReceivedGrantLeaseAmountPage).get mustBe 6
      updated.get(YearLeaseAmountPage).get mustBe 5
      updated.get(ConsolidatedExpensesPage).get mustBe ConsolidatedExpenses(false, None)
      updated.get(RentsRatesAndInsurancePage).get mustBe 8
      updated.get(RepairsAndMaintenanceCostsPage).get mustBe 7
      updated.get(LoanInterestPage).get mustBe 6
      updated.get(OtherProfessionalFeesPage).get mustBe 5
      updated.get(CostsOfServicesProvidedPage).get mustBe 4
      updated.get(PropertyBusinessTravelCostsPage).get mustBe 3
      updated.get(OtherAllowablePropertyExpensesPage).get mustBe 2
      updated.get(CapitalAllowancesForACarPage).get mustBe CapitalAllowancesForACar(false, None)
      updated.get(PrivateUseAdjustmentPage).get mustBe PrivateUseAdjustment(4)
      updated.get(BalancingChargePage).get mustBe BalancingCharge(false, None)
      updated.get(PropertyIncomeAllowancePage).get mustBe 0
      updated.get(RenovationAllowanceBalancingChargePage).get mustBe RenovationAllowanceBalancingCharge(true, Some(6))
      updated.get(ResidentialFinanceCostPage).get mustBe 4
      updated.get(UnusedResidentialFinanceCostPage).get mustBe 3
      updated.get(ClaimStructureBuildingAllowancePage).get mustBe true
      updated.get(StructuredBuildingAllowanceAddressPage(0)).get mustBe StructuredBuildingAllowanceAddress(
        "12",
        "12",
        "EH1 AB1"
      )
      updated.get(StructureBuildingQualifyingDatePage(0)).get mustBe LocalDate.parse("2022-02-02")
      updated.get(StructureBuildingQualifyingAmountPage(0)).get mustBe 22
      updated.get(StructureBuildingAllowanceClaimPage(0)).get mustBe 22
      updated.get(StructuredBuildingAllowanceAddressPage(1)).get mustBe StructuredBuildingAllowanceAddress(
        "123",
        "231",
        "EH1 AB1"
      )
      updated.get(StructureBuildingQualifyingDatePage(1)).get mustBe LocalDate.parse("2023-02-23")
      updated.get(StructureBuildingQualifyingAmountPage(1)).get mustBe 12
      updated.get(StructureBuildingAllowanceClaimPage(1)).get mustBe 23
      updated.get(SbaClaimsPage).get mustBe true
      updated.get(SbaRemoveConfirmationPage).get mustBe true
    }
  }
}
