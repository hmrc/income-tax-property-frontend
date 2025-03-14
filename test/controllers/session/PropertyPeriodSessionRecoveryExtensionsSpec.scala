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

package controllers.session

import base.SpecBase
import controllers.session.PropertyPeriodSessionRecoveryExtensions._
import models._
import org.scalatestplus.mockito.MockitoSugar
import pages.adjustments._
import pages.enhancedstructuresbuildingallowance._
import pages.foreign.expenses._
import pages.foreign._
import pages.foreign.adjustments._
import pages.foreign.allowances._
import pages.foreign.income._
import pages.foreign.structurebuildingallowance._
import pages.premiumlease.{CalculatedFigureYourselfPage, ReceivedGrantLeaseAmountPage}
import pages.propertyrentals.ClaimPropertyIncomeAllowancePage
import pages.propertyrentals.expenses._
import pages.propertyrentals.income.{IsNonUKLandlordPage, PropertyRentalIncomePage, ReversePremiumsReceivedPage}
import pages.structurebuildingallowance._
import pages.ukandforeignproperty.{ReportIncomePage, TotalPropertyIncomePage}
import pages.ukrentaroom.adjustments.{RaRBalancingChargePage, RaRUnusedLossesBroughtForwardPage, RaRUnusedResidentialCostsPage, RarWhenYouReportedTheLossPage}
import pages.ukrentaroom.allowances._
import pages.ukrentaroom.expenses.{CostOfServicesProvidedRRPage, LegalManagementOtherFeeRRPage, OtherPropertyExpensesRRPage, RentsRatesAndInsuranceRRPage, RepairsAndMaintenanceCostsRRPage}
import pages.ukrentaroom.{ClaimExpensesOrReliefPage, JointlyLetPage, TotalIncomeAmountPage}
import pages.{TotalIncomePage, UKPropertyPage}
import play.api.libs.json.Json
import testHelpers.Fixture

import java.time.LocalDate

class PropertyPeriodSessionRecoveryExtensionsSpec extends SpecBase with MockitoSugar with Fixture {
  val countryCode1 = "ESP"
  val countryCode2 = "USA"
  val index = 0
  val data: String =
    s"""{
       |  "ukPropertyData" : {
       |  "propertyAbout": {
       |    "totalIncome" : "between",
       |    "ukProperty" : [
       |      "property.rentals"
       |    ]
       |  },
       |  "esbasWithSupportingQuestions": {
       |    "claimEnhancedStructureBuildingAllowance" : true,
       |    "enhancedStructureBuildingAllowances" : [
       |      {
       |        "enhancedStructureBuildingAllowanceQualifyingDate" : "2022-01-04",
       |        "enhancedStructureBuildingAllowanceQualifyingAmount" : 5,
       |        "enhancedStructureBuildingAllowanceClaim" : 6,
       |        "enhancedStructureBuildingAllowanceAddress" : {
       |          "buildingName" : "12",
       |          "buildingNumber" : "12",
       |          "postCode" : "EH1 AB1"
       |        }
       |      },
       |      {
       |        "enhancedStructureBuildingAllowanceQualifyingDate" : "2022-03-03",
       |        "enhancedStructureBuildingAllowanceQualifyingAmount" : 4,
       |        "enhancedStructureBuildingAllowanceClaim" : 5,
       |        "enhancedStructureBuildingAllowanceAddress" : {
       |          "buildingName" : "2",
       |          "buildingNumber" : "2",
       |          "postCode" : "EH1 AB1"
       |        }
       |      }
       |    ],
       |    "enhancedStructureBuildingAllowanceClaims" : false
       |  },
       |  "propertyRentalsAbout" : {
       |    "claimPropertyIncomeAllowanceYesOrNo" : false
       |  },
       |  "propertyRentalsIncome" : {
       |    "isNonUKLandlord" : false,
       |    "propertyRentalIncome" : 45,
       |    "premiumForLeaseYesOrNo" : true,
       |    "calculatedFigureYourself" : {
       |      "calculatedFigureYourself" : true,
       |      "amount" : 45
       |    },
       |    "receivedGrantLeaseAmount": 6,
       |    "reversePremiumsReceived" : {
       |      "reversePremiumsReceived" : true,
       |      "reversePremiums" : 45
       |    },
       |    "otherIncomeFromProperty" : 45
       |  },
       |  "propertyRentalsExpenses" : {
       |    "consolidatedExpenses" : {
       |      "consolidatedExpensesYesOrNo" : false
       |    },
       |    "rentsRatesAndInsurance" : 55,
       |    "repairsAndMaintenanceCosts" : 7,
       |    "loanInterestOrOtherFinancialCost" : 56,
       |    "otherProfessionalFees" : 4,
       |    "costsOfServicesProvided" : 34,
       |    "propertyBusinessTravelCosts" : 4,
       |    "otherAllowablePropertyExpenses" : 3
       |  },
       |  "allowances" : {
       |    "annualInvestmentAllowance" : 44,
       |    "electricChargePointAllowance" : {
       |      "electricChargePointAllowanceYesOrNo" : true,
       |      "electricChargePointAllowanceAmount" : 45
       |    },
       |    "zeroEmissionCarAllowance" : 4,
       |    "zeroEmissionGoodsVehicleAllowance" : 4,
       |    "businessPremisesRenovationAllowance" : 4,
       |    "replacementOfDomesticGoodsAllowance" : 4,
       |    "otherCapitalAllowance" : 4
       |  },
       |  "sbasWithSupportingQuestions": {
       |    "claimStructureBuildingAllowance" : true,
       |    "structureBuildingFormGroup" : [ {
       |      "structureBuildingQualifyingDate" : "2022-04-03",
       |      "structureBuildingQualifyingAmount" : 3,
       |      "structureBuildingAllowanceClaim" : 4,
       |      "structuredBuildingAllowanceAddress" : {
       |        "buildingName" : "3",
       |        "buildingNumber" : "3",
       |        "postCode" : "EH1 AB2"
       |      }
       |    },
       |      {
       |        "structureBuildingQualifyingDate" : "2022-02-02",
       |        "structureBuildingQualifyingAmount" : 4,
       |        "structureBuildingAllowanceClaim" : 5,
       |        "structuredBuildingAllowanceAddress" : {
       |          "buildingName" : "4",
       |          "buildingNumber" : "4",
       |          "postCode" : "EH1 AB2"
       |        }
       |      }
       |    ]
       |
       |  },
       |  "adjustments" : {
       |    "privateUseAdjustment" : 2,
       |    "balancingCharge" : {
       |      "balancingChargeYesNo" : true,
       |      "balancingChargeAmount" : 3
       |    },
       |    "propertyIncomeAllowance" : 4,
       |    "renovationAllowanceBalancingCharge" : {
       |      "renovationAllowanceBalancingChargeYesNo" : true,
       |      "renovationAllowanceBalancingChargeAmount" : 23
       |    },
       |    "residentialFinanceCost" : 2,
       |    "unusedResidentialFinanceCost" : 3,
       |    "unusedLossesBroughtForward" : {
       |      "unusedLossesBroughtForwardYesOrNo" : true,
       |      "unusedLossesBroughtForwardAmount" : 24
       |    },
       |    "whenYouReportedTheLoss": "y2021to2022"
       |
       |  },
       |  "raRAbout" : {
       |    "jointlyLetYesOrNo" : false,
       |    "totalIncomeAmount" : 30,
       |    "claimExpensesOrRelief" : {
       |      "claimExpensesOrReliefYesNo" : false,
       |      "rentARoomAmount" : 50
       |    }
       |  },
       |  "raRAdjustments": {
       |     "balancingCharge": {
       |         "balancingChargeAmount": 10,
       |         "balancingChargeYesNo": true
       |     },
       |     "unusedLossesBroughtForward": {
       |         "unusedLossesBroughtForwardAmount": 5,
       |         "unusedLossesBroughtForwardYesOrNo": true
       |     },
       |     "whenYouReportedTheLoss": "y2021to2022",
       |     "unusedResidentialPropertyFinanceCostsBroughtFwd": 45
       | },
       | "rarExpenses": {
       |     "costOfServicesProvided": 30,
       |     "legalManagementOtherFee": 20,
       |     "otherPropertyExpenses": 35,
       |     "rentsRatesAndInsurance": 5,
       |     "repairsAndMaintenanceCosts": 10
       | },
       |  "rentARoomAllowances" : {
       |    "capitalAllowancesForACar" : {
       |      "capitalAllowancesForACarYesNo" : true,
       |      "capitalAllowancesForACarAmount" : 20
       |    },
       |    "annualInvestmentAllowance" : 5,
       |    "electricChargePointAllowance" : 30,
       |    "zeroEmissionCarAllowance" : 35,
       |    "zeroEmissionGoodsVehicleAllowance" : 10,
       |    "replacementOfDomesticGoodsAllowance" : 25,
       |    "otherCapitalAllowance" : 20
       |  },
       |  "journeyStatuses": [],
       |  "foreignPropertySelectCountry" : {
       |    "totalIncome": "under",
       |    "reportPropertyIncome": false}
       |  },
       |  "foreignPropertyData": {
       |    "foreignPropertyIncome": {
       |      "$countryCode1": {
       |        "rentIncome": 12345.75,
       |        "premiumsGrantLeaseReceived": true,
       |        "otherPropertyIncome": 345.65,
       |        "calculatedPremiumLeaseTaxable": {
       |          "calculatedPremiumLeaseTaxable": true
       |        },
       |        "receivedGrantLeaseAmount": 555.55,
       |        "twelveMonthPeriodsInLease": 3,
       |        "premiumsOfLeaseGrantAgreed": {
       |          "premiumsOfLeaseGrantAgreed": true,
       |          "premiumsOfLeaseGrant": 234.5
       |        }
       |      }
       |    },
       |    "foreignPropertyExpenses": {
       |      "$countryCode1": {
       |        "premisesRunningCosts": 15.15,
       |        "repairsAndMaintenance": 25.15,
       |        "financialCosts": 35.15,
       |        "professionalFees": 45.15,
       |        "costOfServices": 65.15,
       |        "other": 95.15
       |      }
       |    },
       |      "foreignPropertyTax": {
       |      "$countryCode1": {
       |      "foreignIncomeTax": {
       |        "foreignIncomeTaxYesNo": true,
       |        "foreignTaxPaidOrDeducted": 590.55
       |      },
       |      "foreignTaxCreditRelief": true
       |      }
       |    },
       |    "foreignPropertyAllowances": {
       |          "$countryCode1": {
       |          "costOfReplacingDomesticItems": 35.60,
       |          "zeroEmissionsGoodsVehicleAllowance": 99.67,
       |          "zeroEmissionsCarAllowance": 45.45,
       |          "otherCapitalAllowance": 45.15
       |         }
       |         },
       |    "foreignPropertySba": {
       |    "$countryCode1": {
       |      "claimStructureBuildingAllowance": true,
       |      "allowances": [{
       |        "amount": 500,
       |        "firstYear": {
       |          "qualifyingDate": "2023-05-04",
       |          "qualifyingAmountExpenditure": 100
       |        },
       |        "building": {
       |          "name": "Building",
       |          "number": "1",
       |          "postCode": "AB2 7AA"
       |        }
       |        }
       |      ]
       |    }
       |},
       |  "foreignPropertyAdjustments": {
       |    "$countryCode1": {
       |      "privateUseAdjustment": 50,
       |      "balancingCharge": {
       |        "balancingChargeYesNo": true,
       |        "balancingChargeAmount": 56.60
       |      },
       |      "residentialFinanceCost": 67.90,
       |      "unusedResidentialFinanceCost": {
       |        "foreignUnusedResidentialFinanceCostYesNo": true,
       |        "foreignUnusedResidentialFinanceCostAmount": 50
       |      },
       |      "propertyIncomeAllowanceClaim": 50,
       |      "unusedLossesPreviousYears": {
       |        "unusedLossesPreviousYearsYesNo": true,
       |        "unusedLossesPreviousYearsAmount": 500
       |      },
       |      "whenYouReportedTheLoss": "y2021to2022"
       |    }
       |  },
       |    "foreignJourneyStatuses": {
       |      "$countryCode1": [
       |        {
       |          "journeyName": "foreign-property-income",
       |          "journeyStatus": "completed"
       |        },
       |        {
       |          "journeyName": "foreign-property-tax",
       |          "journeyStatus": "completed"
       |        },
       |         {
       |           "journeyName": "foreign-property-allowances",
       |           "journeyStatus": "completed"
       |       },
       |       {
       |           "journeyName": "foreign-property-sba",
       |           "journeyStatus": "completed"
       |       },
       |       {
       |           "journeyName": "foreign-property-adjustments",
       |           "journeyStatus": "completed"
       |       }
       |      ],
       |      "$countryCode2": [
       |        {
       |          "journeyName": "foreign-property-expenses",
       |          "journeyStatus": "inProgress"
       |        }
       |      ]
       |    }
       |  },
       |  "ukAndForeignPropertyData" : {
       |    "ukAndForeignAbout" : {
       |      "aboutUkAndForeign": {
       |        "totalPropertyIncome" : "maximum",
       |        "reportIncome" : "wantToReport"
       |      }
       |    }
       |  }
       |}""".stripMargin

  "PropertyPeriodSessionRecoveryExtensionsSpec" - {
    "should update the session data correctly" in {
      val fetchedData = Json.parse(data).as[FetchedPropertyData]

      val updated = emptyUserAnswers
        .update(fetchedData)

      updated.get(TotalIncomePage).get mustBe TotalIncome.Between
      updated.get(UKPropertyPage).get mustBe Set(UKPropertySelect.PropertyRentals)
      updated.get(ClaimPropertyIncomeAllowancePage(Rentals)).get mustBe false
      updated.get(IsNonUKLandlordPage(Rentals)).get mustBe false
      updated.get(PropertyRentalIncomePage(Rentals)).get mustBe 45
      updated.get(ReceivedGrantLeaseAmountPage(Rentals)) mustBe None // Lease clean up test
      //Todo: To be uncommented, and added to tests when related tickets implemented. updated.get(YearLeaseAmountPage).get mustBe 5
      //Todo: To be uncommented, and added to tests when related tickets implemented. updated.get(ConsolidatedExpensesPage).get mustBe ConsolidatedExpenses(false, None)
      //Todo: To be uncommented, and added to tests when related tickets implemented. updated.get(RentsRatesAndInsurancePage).get mustBe 55
      // Todo: do we need this? updated.get(PremiumForLeasePage).get mustBe true
      updated.get(CalculatedFigureYourselfPage(Rentals)).get mustBe CalculatedFigureYourself(
        calculatedFigureYourself = true,
        Some(45)
      )
      updated.get(ReversePremiumsReceivedPage(Rentals)).get mustBe ReversePremiumsReceived(
        reversePremiumsReceived = true,
        Some(45)
      )
      updated.get(JointlyLetPage(RentARoom)).get mustBe false
      updated.get(TotalIncomeAmountPage(RentARoom)).get mustBe 30
      updated.get(ClaimExpensesOrReliefPage(RentARoom)).get mustBe ClaimExpensesOrRelief(
        claimExpensesOrReliefYesNo = false,
        Some(50)
      )
      updated.get(RentsRatesAndInsuranceRRPage).get mustBe 5
      updated.get(RepairsAndMaintenanceCostsRRPage).get mustBe 10
      updated.get(LegalManagementOtherFeeRRPage).get mustBe 20
      updated.get(CostOfServicesProvidedRRPage).get mustBe 30
      updated.get(OtherPropertyExpensesRRPage).get mustBe 35

      updated.get(RaRBalancingChargePage).get mustBe BalancingCharge(balancingChargeYesNo = true, Some(BigDecimal(10)))
      updated.get(RaRUnusedResidentialCostsPage).get mustBe BigDecimal(45)
      updated.get(RaRUnusedLossesBroughtForwardPage).get mustBe UnusedLossesBroughtForward(unusedLossesBroughtForwardYesOrNo = true, Some(BigDecimal(5)))
      updated.get(RarWhenYouReportedTheLossPage) mustBe Some(WhenYouReportedTheLoss.y2021to2022)

      updated.get(RepairsAndMaintenanceCostsPage(Rentals)).get mustBe 7
      updated.get(LoanInterestPage(Rentals)).get mustBe 56
      updated.get(OtherProfessionalFeesPage(Rentals)).get mustBe 4
      updated.get(CostsOfServicesProvidedPage(Rentals)).get mustBe 34
      updated.get(PropertyBusinessTravelCostsPage(Rentals)).get mustBe 4
      updated.get(OtherAllowablePropertyExpensesPage(Rentals)).get mustBe 3
      updated.get(BalancingChargePage(Rentals)).get mustBe BalancingCharge(balancingChargeYesNo = true, Some(3))
      updated.get(PrivateUseAdjustmentPage(Rentals)).get mustBe PrivateUseAdjustment(2)
      updated.get(PropertyIncomeAllowancePage(Rentals)).get mustBe 4
      updated.get(ReversePremiumsReceivedPage(Rentals)).get mustBe ReversePremiumsReceived(
        reversePremiumsReceived = true,
        Some(45)
      )
      updated.get(RenovationAllowanceBalancingChargePage(Rentals)).get mustBe RenovationAllowanceBalancingCharge(
        renovationAllowanceBalancingChargeYesNo = true,
        Some(23)
      )
      updated.get(ResidentialFinanceCostPage(Rentals)).get mustBe 2
      updated.get(UnusedResidentialFinanceCostPage(Rentals)).get mustBe 3
      updated.get(UnusedLossesBroughtForwardPage(Rentals)).get mustBe UnusedLossesBroughtForward(
        unusedLossesBroughtForwardYesOrNo = true,
        Some(24)
      )
      updated.get(WhenYouReportedTheLossPage(Rentals)) mustBe Some(WhenYouReportedTheLoss.y2021to2022)
      updated.get(ClaimEsbaPage(Rentals)).get mustBe true
      updated.get(EsbaAddressPage(0, Rentals)).get mustBe EsbaAddress(
        "12",
        "12",
        "EH1 AB1"
      )
      updated.get(EsbaQualifyingDatePage(0, Rentals)).get mustBe LocalDate.parse("2022-01-04")
      updated.get(EsbaQualifyingAmountPage(0, Rentals)).get mustBe 5
      updated.get(EsbaClaimPage(0, Rentals)).get mustBe 6
      updated.get(EsbaAddressPage(1, Rentals)).get mustBe EsbaAddress(
        "2",
        "2",
        "EH1 AB1"
      )
      updated.get(EsbaQualifyingDatePage(1, Rentals)).get mustBe LocalDate.parse("2022-03-03")
      updated.get(EsbaQualifyingAmountPage(1, Rentals)).get mustBe 4
      updated.get(EsbaClaimPage(1, Rentals)).get mustBe 5

      // Todo: To be uncommented, and added to tests when related tickets implemented. updated.get(CapitalAllowancesForACarPage).get mustBe CapitalAllowancesForACar(false, None)
      updated.get(StructureBuildingQualifyingDatePage(0, Rentals)).get mustBe LocalDate.parse("2022-04-03")
      updated.get(StructureBuildingQualifyingAmountPage(0, Rentals)).get mustBe 3
      updated.get(StructureBuildingAllowanceClaimPage(0, Rentals)).get mustBe 4
      updated.get(StructuredBuildingAllowanceAddressPage(0, Rentals)).get mustBe StructuredBuildingAllowanceAddress(
        "3",
        "3",
        "EH1 AB2"
      )
      updated.get(StructureBuildingQualifyingDatePage(1, Rentals)).get mustBe LocalDate.parse("2022-02-02")
      updated.get(StructureBuildingQualifyingAmountPage(1, Rentals)).get mustBe 4
      updated.get(StructureBuildingAllowanceClaimPage(1, Rentals)).get mustBe 5
      updated.get(StructuredBuildingAllowanceAddressPage(1, Rentals)).get mustBe StructuredBuildingAllowanceAddress(
        "4",
        "4",
        "EH1 AB2"
      )
      updated.get(RaRBalancingChargePage).get mustBe BalancingCharge(balancingChargeYesNo = true, Some(10))

      updated.get(RaRCapitalAllowancesForACarPage).get mustBe CapitalAllowancesForACar(
        capitalAllowancesForACarYesNo = true,
        Some(20)
      )
      updated.get(RaRAnnualInvestmentAllowancePage).get mustBe 5
      updated.get(RaRZeroEmissionCarAllowancePage).get mustBe 35
      updated.get(RaRZeroEmissionGoodsVehicleAllowancePage).get mustBe 10
      updated.get(RaRReplacementsOfDomesticGoodsPage).get mustBe 25
      updated.get(RaROtherCapitalAllowancesPage).get mustBe 20
      updated.get(pages.foreign.TotalIncomePage).get mustBe TotalIncome.Under
      updated.get(pages.foreign.PropertyIncomeReportPage).get mustBe false
      updated.get(ForeignPropertyRentalIncomePage(countryCode1)).get mustBe 12345.75
      updated.get(PremiumsGrantLeaseYNPage(countryCode1)).get mustBe true
      updated.get(CalculatedPremiumLeaseTaxablePage(countryCode1)).get mustBe PremiumCalculated(
        calculatedPremiumLeaseTaxable = true,
        None
      )
      updated.get(ForeignReceivedGrantLeaseAmountPage(countryCode1)).get mustBe 555.55
      updated.get(TwelveMonthPeriodsInLeasePage(countryCode1)).get mustBe 3
      updated.get(ForeignPremiumsGrantLeasePage(countryCode1)).get mustBe ForeignPremiumsGrantLease(
        premiumsOfLeaseGrantAgreed = true,
        premiumsOfLeaseGrant = Some(234.5)
      )
      updated.get(ForeignOtherIncomeFromPropertyPage(countryCode1)).get mustBe 345.65
      updated.get(ForeignIncomeSectionCompletePage(countryCode1)).get mustBe true
      updated.get(ForeignTaxSectionCompletePage(countryCode1)).get mustBe true
      updated.get(ForeignExpensesSectionCompletePage(countryCode2)).get mustBe false
      updated.get(ForeignIncomeTaxPage(countryCode1)).get mustBe ForeignIncomeTax(
        foreignIncomeTaxYesNo = true,
        Some(590.55)
      )
      updated.get(ClaimForeignTaxCreditReliefPage(countryCode1)).get mustBe true
      updated.get(ConsolidatedOrIndividualExpensesPage(countryCode1)) mustBe None
      updated.get(ForeignRentsRatesAndInsurancePage(countryCode1)) mustBe Some(15.15)
      updated.get(ForeignPropertyRepairsAndMaintenancePage(countryCode1)) mustBe Some(25.15)
      updated.get(ForeignNonResidentialPropertyFinanceCostsPage(countryCode1)) mustBe Some(35.15)
      updated.get(ForeignProfessionalFeesPage(countryCode1)) mustBe Some(45.15)
      updated.get(ForeignCostsOfServicesProvidedPage(countryCode1)) mustBe Some(65.15)
      updated.get(ForeignOtherAllowablePropertyExpensesPage(countryCode1)) mustBe Some(95.15)
      updated.get(ForeignZeroEmissionCarAllowancePage(countryCode1)) mustBe Some(45.45)
      updated.get(ForeignReplacementOfDomesticGoodsPage(countryCode1)) mustBe Some(35.60)
      updated.get(ForeignZeroEmissionCarAllowancePage(countryCode1)) mustBe Some(45.45)
      updated.get(ForeignOtherCapitalAllowancesPage(countryCode1)) mustBe Some(45.15)
      updated.get(ForeignAllowancesCompletePage(countryCode1)) mustBe Some(true)

      updated.get(TotalPropertyIncomePage).get mustBe TotalPropertyIncome.Maximum
      updated.get(ReportIncomePage).get mustBe ReportIncome.WantToReport

      updated.get(ForeignClaimStructureBuildingAllowancePage(countryCode1)).get mustBe true
      updated.get(ForeignStructureBuildingQualifyingAmountPage(countryCode1, index)).get mustBe 100
      updated.get(ForeignStructureBuildingQualifyingDatePage(countryCode1, index)).get mustBe LocalDate.parse(
        "2023-05-04"
      )
      updated
        .get(ForeignStructuresBuildingAllowanceAddressPage(index, countryCode1))
        .get mustBe ForeignStructuresBuildingAllowanceAddress(name = "Building", number = "1", postCode = "AB2 7AA")
      updated.get(ForeignStructureBuildingAllowanceClaimPage(countryCode1, index)).get mustBe 500
      updated.get(ForeignSbaCompletePage(countryCode1)) mustBe Some(true)

      updated.get(ForeignPrivateUseAdjustmentPage(countryCode1)) mustBe Some(50)
      updated.get(ForeignBalancingChargePage(countryCode1)) mustBe Some(
        BalancingCharge(balancingChargeYesNo = true, Some(BigDecimal(56.60)))
      )
      updated.get(ForeignResidentialFinanceCostsPage(countryCode1)) mustBe Some(67.90)
      updated.get(ForeignUnusedResidentialFinanceCostPage(countryCode1)) mustBe Some(
        ForeignUnusedResidentialFinanceCost(foreignUnusedResidentialFinanceCostYesNo = true, Some(BigDecimal(50)))
      )
      updated.get(PropertyIncomeAllowanceClaimPage(countryCode1)) mustBe Some(50)
      updated.get(ForeignUnusedLossesPreviousYearsPage(countryCode1)) mustBe Some(
        UnusedLossesPreviousYears(unusedLossesPreviousYearsYesNo = true, Some(BigDecimal(500)))
      )
      updated.get(ForeignWhenYouReportedTheLossPage(countryCode1)) mustBe Some(ForeignWhenYouReportedTheLoss.y2021to2022)
      updated.get(ForeignAdjustmentsCompletePage(countryCode1)) mustBe Some(true)
    }
  }
}
