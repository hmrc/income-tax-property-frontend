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

package navigation

import base.SpecBase
import controllers.adjustments.routes._
import controllers.allowances.routes._
import controllers.premiumlease.routes._
import controllers.propertyrentals.expenses.routes._
import controllers.propertyrentals.routes._
import controllers.routes
import models.TotalIncome.Under
import models._
import pages._
import pages.adjustments._
import pages.allowances._
import pages.enhancedstructuresbuildingallowance.{EsbaClaimAmountPage, EsbaQualifyingAmountPage, EsbaQualifyingDatePage}
import pages.furnishedholidaylettings.income.FhlIsNonUKLandlordPage
import pages.furnishedholidaylettings._
import pages.premiumlease._
import pages.propertyrentals.expenses._
import pages.propertyrentals.income._
import pages.propertyrentals.{ClaimPropertyIncomeAllowancePage, ExpensesLessThan1000Page}
import pages.structurebuildingallowance.{StructureBuildingQualifyingAmountPage, StructureBuildingQualifyingDatePage}

import java.time.LocalDate

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator
  private val taxYear = LocalDate.now.getYear
  private val index = 0

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, taxYear, NormalMode, UserAnswers("id"), UserAnswers("id")) mustBe routes.IndexController.onPageLoad
      }

      "must go from UKPropertyDetailsPage to Total Income" in {
        navigator.nextPage(
          UKPropertyDetailsPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.about.routes.TotalIncomeController.onPageLoad(taxYear, NormalMode)
      }

      "must go from TotalIncomePage to the UK property select page" in {
        navigator.nextPage(
          TotalIncomePage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.about.routes.UKPropertySelectController.onPageLoad(taxYear, NormalMode)
      }
      "must go from TotalIncomePage to Report property income page if total income is under" in {
        navigator.nextPage(
          TotalIncomePage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test").set(TotalIncomePage, Under).get
        ) mustBe controllers.about.routes.ReportPropertyIncomeController.onPageLoad(taxYear, NormalMode)
      }

      "most go from UKPropertySelectPage to the summary page" in {
        navigator.nextPage(
          UKPropertySelectPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe routes.SummaryController.show(taxYear)
      }

      "must go from UKPropertyPage to Check Your Answers" in {
        navigator.nextPage(
          UKPropertyPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.about.routes.CheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from ReportPropertyIncomePage to Check Your Answers" in {
        navigator.nextPage(
          ReportPropertyIncomePage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.about.routes.CheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from ReportPropertyIncomePage to Property select page if user want to report income" in {
        navigator.nextPage(
          ReportPropertyIncomePage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test").set(ReportPropertyIncomePage, true).get
        ) mustBe controllers.about.routes.UKPropertySelectController.onPageLoad(taxYear, NormalMode)
      }

      "must go from LeasePremiumPaymentPage to CalculateFigureYourselfPage when user selects yes" in {
        val testUserAnswer = UserAnswers("test").set(LeasePremiumPaymentPage, true).get

        navigator.nextPage(
          LeasePremiumPaymentPage, taxYear, NormalMode, UserAnswers("test"), testUserAnswer
        ) mustBe controllers.premiumlease.routes.CalculatedFigureYourselfController.onPageLoad(taxYear, NormalMode)
      }

      "must go from LeasePremiumPaymentPage to reversePremiumReceivedPage when user selects no" in {
        val testUserAnswer = UserAnswers("test").set(LeasePremiumPaymentPage, false).get

        navigator.nextPage(
          LeasePremiumPaymentPage, taxYear, NormalMode, UserAnswers("test"), testUserAnswer
        ) mustBe controllers.propertyrentals.income.routes.ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode)
      }

      "must go from CalculatedFigureYourselfPage to RecievedGrantLeaseAmountPage when user selects no" in {
        val testUserAnswer = UserAnswers("test").set(CalculatedFigureYourselfPage, CalculatedFigureYourself(false, None)).get

        navigator.nextPage(
          CalculatedFigureYourselfPage, taxYear, NormalMode, UserAnswers("test"), testUserAnswer
        ) mustBe controllers.premiumlease.routes.ReceivedGrantLeaseAmountController.onPageLoad(taxYear, NormalMode)
      }

      "must go from CalculatedFigureYourselfPage to ReversePremiumReceivedPage when user selects yes" in {
        val testUserAnswer = UserAnswers("test").set(CalculatedFigureYourselfPage, CalculatedFigureYourself(true, Some(100))).get

        navigator.nextPage(
          CalculatedFigureYourselfPage, taxYear, NormalMode, UserAnswers("test"), testUserAnswer
        ) mustBe controllers.propertyrentals.income.routes.ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode)
      }

      "must go from RecievedGrantLeaseAmountPage to YearLeaseAmountPage" in {
        navigator.nextPage(
          premiumlease.ReceivedGrantLeaseAmountPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.premiumlease.routes.YearLeaseAmountController.onPageLoad(taxYear, NormalMode)
      }

      "must go from YearLeaseAmountPage to PremiumsGrantLeasePage" in {
        navigator.nextPage(
          premiumlease.YearLeaseAmountPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.premiumlease.routes.PremiumsGrantLeaseController.onPageLoad(taxYear, NormalMode)
      }

      "must go from reverse to OtherIncomeFromPropertyPage" in {
        navigator.nextPage(
          ReversePremiumsReceivedPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.propertyrentals.income.routes.OtherIncomeFromPropertyController.onPageLoad(taxYear, NormalMode)
      }


      "must go from ExpensesLessThan1000Page to ClaimPropertyIncomeAllowancePage" in {
        navigator.nextPage(
          ExpensesLessThan1000Page, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.propertyrentals.routes.ClaimPropertyIncomeAllowanceController.onPageLoad(taxYear, NormalMode)
      }

      "must go from ClaimPropertyIncomeAllowancePage to CheckYourAnswersPage" in {
        navigator.nextPage(
          ClaimPropertyIncomeAllowancePage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.propertyrentals.routes.PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from DeductingTax to IncomeFromPropertyRentalsPage" in {
        navigator.nextPage(
          DeductingTaxPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.propertyrentals.income.routes.IncomeFromPropertyRentalsController.onPageLoad(taxYear, NormalMode)
      }

      "must go from IncomeFromPropertyRentalsPage to LeasePremiumPaymentPage" in {
        navigator.nextPage(
          IncomeFromPropertyRentalsPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.premiumlease.routes.LeasePremiumPaymentController.onPageLoad(taxYear, NormalMode)
      }

      "must go from IsNonUKLandlordPage to DeductingTaxPage when answer is yes" in {
        val userAnswers = UserAnswers("test").set(IsNonUKLandlordPage, true).get
        navigator.nextPage(
          IsNonUKLandlordPage, taxYear, NormalMode, UserAnswers("test"), userAnswers
        ) mustBe controllers.propertyrentals.income.routes.DeductingTaxController.onPageLoad(taxYear, NormalMode)
      }

      "must go from IsNonUKLandlordPage to IncomeFromPropertyRentalsPage when answer is no" in {
        val userAnswers = UserAnswers("test").set(IsNonUKLandlordPage, false).get
        navigator.nextPage(
          IsNonUKLandlordPage, taxYear, NormalMode, UserAnswers("test"), userAnswers
        ) mustBe controllers.propertyrentals.income.routes.IncomeFromPropertyRentalsController.onPageLoad(taxYear, NormalMode)
      }

      "must go from OtherIncomeFromPropertyPage to PropertyIncomeCheckYourAnswersPage" in {
        navigator.nextPage(
          OtherIncomeFromPropertyPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.propertyrentals.income.routes.PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from PrivateUseAdjustmentPage to BalancingChargePage" in {
        navigator.nextPage(
          PrivateUseAdjustmentPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe BalancingChargeController.onPageLoad(taxYear, NormalMode)
      }

      "must go from BalancingChargePage to PropertyIncomeAllowancePage" in {
        navigator.nextPage(
          BalancingChargePage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe PropertyIncomeAllowanceController.onPageLoad(taxYear, NormalMode)
      }

      "must go from PropertyIncomeAllowancePage to RenovationAllowanceBalancingChargePage" in {
        navigator.nextPage(
          PropertyIncomeAllowancePage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe RenovationAllowanceBalancingChargeController.onPageLoad(taxYear, NormalMode)
      }

      "must go from RenovationAllowanceBalancingChargePage to ResidentialFinanceCostPage" in {
        navigator.nextPage(
          RenovationAllowanceBalancingChargePage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe ResidentialFinanceCostController.onPageLoad(taxYear, NormalMode)
      }

      "must go from ResidentialFinanceCostPage to UnusedResidentialFinanceCostPage" in {
        navigator.nextPage(
          ResidentialFinanceCostPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe UnusedResidentialFinanceCostController.onPageLoad(taxYear, NormalMode)
      }

      "must go from UnusedResidentialFinanceCostPage to AdjustmentsCheckYourAnswersPage" in {
        navigator.nextPage(
          UnusedResidentialFinanceCostPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from ConsolidatedExpensesPage to RentsRatesAndInsurancePage when user selects no" in {
        val testUserAnswer = UserAnswers("test").set(ConsolidatedExpensesPage, ConsolidatedExpenses(false, None)).get

        navigator.nextPage(
          ConsolidatedExpensesPage, taxYear, NormalMode, UserAnswers("test"), testUserAnswer
        ) mustBe controllers.propertyrentals.expenses.routes.RentsRatesAndInsuranceController.onPageLoad(taxYear, NormalMode)
      }

      "must go from ConsolidatedExpensesPage to ReversePremiumReceivedPage when user selects yes" in {
        val testUserAnswer = UserAnswers("test").set(ConsolidatedExpensesPage, ConsolidatedExpenses(true, Some(100))).get

        navigator.nextPage(
          ConsolidatedExpensesPage, taxYear, NormalMode, UserAnswers("test"), testUserAnswer
        ) mustBe controllers.propertyrentals.expenses.routes.ExpensesCheckYourAnswersController.onPageLoad(taxYear)
      }


      "must go from RentsRatesAndInsurancePage to RepairsAndMaintenanceCostsPage" in {
        navigator.nextPage(
          RentsRatesAndInsurancePage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe RepairsAndMaintenanceCostsController.onPageLoad(taxYear, NormalMode)
      }


      "must go from RepairsAndMaintenanceCostsPage to LoanInterestPage" in {
        navigator.nextPage(
          RepairsAndMaintenanceCostsPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.propertyrentals.expenses.routes.LoanInterestController.onPageLoad(taxYear, NormalMode)
      }

      "must go from LoanInterestPage to OtherProfessionalFeesPage" in {
        navigator.nextPage(
          LoanInterestPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.propertyrentals.expenses.routes.OtherProfessionalFeesController.onPageLoad(taxYear, NormalMode)
      }
      "must go from OtherProfessionalFeesPage to CostsOfServicesProvidedPage" in {
        navigator.nextPage(
          OtherProfessionalFeesPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.propertyrentals.expenses.routes.CostsOfServicesProvidedController.onPageLoad(taxYear, NormalMode)
      }
      "must go from CostsOfServicesProvidedPage to PropertyBusinessTravelCostsPage" in {
        navigator.nextPage(
          CostsOfServicesProvidedPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.propertyrentals.expenses.routes.PropertyBusinessTravelCostsController.onPageLoad(taxYear, NormalMode)
      }

      "must go from PropertyBusinessTravelCostsPage to OtherAllowablePropertyExpensesPage" in {
        navigator.nextPage(
          PropertyBusinessTravelCostsPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.propertyrentals.expenses.routes.OtherAllowablePropertyExpensesController.onPageLoad(taxYear, NormalMode)
      }

      "must go from AnnualInvestmentAllowancePage to ElectricChargePointAllowancePage" in {
        navigator.nextPage(
          AnnualInvestmentAllowancePage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe ElectricChargePointAllowanceController.onPageLoad(taxYear, NormalMode)
      }
      "must go from ElectricChargePointAllowancePage to ZeroEmissionCarAllowancePage" in {
        navigator.nextPage(
          ElectricChargePointAllowancePage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe ZeroEmissionCarAllowanceController.onPageLoad(taxYear, NormalMode)
      }
      "must go from ZeroEmissionCarAllowancePage to ZeroEmissionGoodsVehicleAllowancePage" in {
        navigator.nextPage(
          ZeroEmissionCarAllowancePage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe ZeroEmissionGoodsVehicleAllowanceController.onPageLoad(taxYear, NormalMode)
      }
      "must go from ZeroEmissionGoodsVehicleAllowancePage to BusinessPremisesRenovationPage" in {
        navigator.nextPage(
          ZeroEmissionGoodsVehicleAllowancePage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe BusinessPremisesRenovationController.onPageLoad(taxYear, NormalMode)
      }
      "must go from BusinessPremisesRenovationPage to ReplacementOfDomesticGoodsPage" in {
        navigator.nextPage(
          BusinessPremisesRenovationPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe ReplacementOfDomesticGoodsController.onPageLoad(taxYear, NormalMode)
      }
      "must go from ReplacementOfDomesticGoodsPage to OtherCapitalAllowancePage" in {
        navigator.nextPage(
          ReplacementOfDomesticGoodsPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe OtherCapitalAllowanceController.onPageLoad(taxYear, NormalMode)
      }
      "must go from OtherCapitalAllowancePage to AllowancesCheckYourAnswersPage" in {
        navigator.nextPage(
          OtherCapitalAllowancePage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe AllowancesCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from StructureBuildingQualifyingDatePage to StructureBuildingQualifyingAmountPage" in {
        navigator.nextPage(
          StructureBuildingQualifyingDatePage(index), taxYear, NormalMode, 0, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.structuresbuildingallowance.routes.StructureBuildingQualifyingAmountController.onPageLoad(taxYear, NormalMode, 0)
      }

      "must go from StructureBuildingQualifyingAmountPage to StructureBuildingAllowanceClaimPage" in {
        navigator.nextPage(
          StructureBuildingQualifyingAmountPage(index), taxYear, NormalMode, 0, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.structuresbuildingallowance.routes.StructureBuildingAllowanceClaimController.onPageLoad(taxYear, NormalMode, 0)
      }

      "must go from EsbaQualifyingDatePage to EsbaQualifyingAmountPage" in {
        navigator.esbaNextPage(
          EsbaQualifyingDatePage(index), taxYear, NormalMode, index, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.enhancedstructuresbuildingallowance.routes.EsbaQualifyingAmountController.onPageLoad(taxYear, index, NormalMode)
      }

      "must go from EsbaQualifyingAmountPage to EsbaClaimAmountPage" in {
        navigator.esbaNextPage(
          EsbaQualifyingAmountPage(index), taxYear, NormalMode, index, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.enhancedstructuresbuildingallowance.routes.EsbaClaimAmountController.onPageLoad(taxYear, NormalMode, index)
      }

      "must go from EsbaClaimAmountPage to EsbaAddressPage" in {
        navigator.esbaNextPage(
          EsbaQualifyingAmountPage(index), taxYear, NormalMode, index, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.enhancedstructuresbuildingallowance.routes.EsbaClaimAmountController.onPageLoad(taxYear, NormalMode, index)
      }

      "must go from FhlMoreThanOnePage to FhlMainHomePage" in {
        navigator.nextPage(
          FhlMoreThanOnePage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.furnishedholidaylettings.routes.FhlMainHomeController.onPageLoad(taxYear, NormalMode)
      }

      "must go from FhlMainHomePage to FhlJointlyLetPage when yes selected" in {
        navigator.nextPage(
          FhlMainHomePage, taxYear, NormalMode, emptyUserAnswers, emptyUserAnswers.set(FhlMainHomePage, true).get
        ) mustBe controllers.furnishedholidaylettings.routes.FhlJointlyLetController.onPageLoad(taxYear, NormalMode)
      }

      "must go from FhlMainHomePage to FhlJointlyLetPage when no selected" in {
        navigator.nextPage(
          FhlMainHomePage, taxYear, NormalMode, emptyUserAnswers, emptyUserAnswers.set(FhlMainHomePage, false).get
        ) mustBe controllers.furnishedholidaylettings.routes.FhlClaimPiaOrExpensesController.onPageLoad(taxYear, NormalMode)
      }

      "must go from FhlJointlyLetPage to FhlReliefOrExpensesPage" in {
        navigator.nextPage(
          FhlJointlyLetPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.furnishedholidaylettings.routes.FhlReliefOrExpensesController.onPageLoad(taxYear, NormalMode)
      }

      "must go from FhlReliefOrExpensesPage to FhlCheckYourAnswersPage" in {
        navigator.nextPage(
          FhlReliefOrExpensesPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.furnishedholidaylettings.routes.FhlCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from FhlClaimPiaOrExpensesPage to FhlCheckYourAnswersPage" in {
        navigator.nextPage(
          FhlClaimPiaOrExpensesPage, taxYear, NormalMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.furnishedholidaylettings.routes.FhlCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from FhlIsNonUKLandlordPage to FhlDeductingTax page if FhlIsNonUKLandlordPage is true" in {
        val testUserAnswer = UserAnswers("test").set(FhlIsNonUKLandlordPage, true).get
        navigator.nextPage(
          FhlIsNonUKLandlordPage, taxYear, NormalMode, UserAnswers("test"), testUserAnswer
        ) mustBe controllers.furnishedholidaylettings.income.routes.FhlDeductingTaxController.onPageLoad(taxYear, NormalMode)
      }

      "must go from FhlIsNonUKLandlordPage to FhlDeductingTax page if FhlIsNonUKLandlordPage is false" in {
        val testUserAnswer = UserAnswers("test").set(FhlIsNonUKLandlordPage, false).get
        navigator.nextPage(
          FhlIsNonUKLandlordPage, taxYear, NormalMode, UserAnswers("test"), testUserAnswer
        ) mustBe controllers.routes.FhlIncomeController.onPageLoad(taxYear, NormalMode)
      }

    }

    "in Check mode" - {

      "must go from TotalIncomePage to CheckYourAnswersPage if no change in user answers" in {
        navigator.nextPage(
          TotalIncomePage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.about.routes.CheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from TotalIncomePage to ReportPropertyIncomePage if income changes from between to under" in {
        val previousAnswers = UserAnswers("test").set(TotalIncomePage, TotalIncome.Between).get
        val userAnswers = UserAnswers("test").set(TotalIncomePage, TotalIncome.Under).get
        navigator.nextPage(
          TotalIncomePage, taxYear, CheckMode, previousAnswers, userAnswers
        ) mustBe controllers.about.routes.ReportPropertyIncomeController.onPageLoad(taxYear, NormalMode)
      }

      "must go from TotalIncomePage to Property Select page if income changes from under to over" in {
        val previousAnswers = UserAnswers("test").set(TotalIncomePage, TotalIncome.Under).get
        val userAnswers = UserAnswers("test").set(TotalIncomePage, TotalIncome.Over).get
        navigator.nextPage(
          TotalIncomePage, taxYear, CheckMode, previousAnswers, userAnswers
        ) mustBe controllers.about.routes.UKPropertySelectController.onPageLoad(taxYear, NormalMode)
      }

      "must go from TotalIncomePage to check your answers page if no change in answers" in {
        val previousAnswers = UserAnswers("test").set(TotalIncomePage, TotalIncome.Under).get
        val userAnswers = UserAnswers("test").set(TotalIncomePage, TotalIncome.Under).get
        navigator.nextPage(
          TotalIncomePage, taxYear, CheckMode, previousAnswers, userAnswers
        ) mustBe controllers.about.routes.CheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from ExpensesLessThan1000Page to CheckYourAnswersPage" in {
        navigator.nextPage(
          ExpensesLessThan1000Page, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.propertyrentals.routes.PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from ClaimPropertyIncomeAllowancePage to CheckYourAnswersPage" in {
        navigator.nextPage(
          ClaimPropertyIncomeAllowancePage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.propertyrentals.routes.PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from IsNonUKLandlordPage to the CheckYourAnswers page when answer is no" in {
        val previousUserAnswers = UserAnswers("test").set(IsNonUKLandlordPage, false).get
        val userAnswers = UserAnswers("test").set(IsNonUKLandlordPage, false).get
        navigator.nextPage(
          IsNonUKLandlordPage, taxYear, CheckMode, previousUserAnswers, userAnswers
        ) mustBe controllers.propertyrentals.income.routes.PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from IsNonUKLandlordPage to the CheckYourAnswers page when answer is yes and the previous answer was yes" in {
        val previousUserAnswers = UserAnswers("test").set(IsNonUKLandlordPage, true).get
        val userAnswers = UserAnswers("test").set(IsNonUKLandlordPage, true).get
        navigator.nextPage(
          IsNonUKLandlordPage, taxYear, CheckMode, previousUserAnswers, userAnswers
        ) mustBe controllers.propertyrentals.income.routes.PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from IsNonUKLandlordPage to the DeductingTax page when answer is yes and the previous answer was no" in {
        val previousUserAnswers = UserAnswers("test").set(IsNonUKLandlordPage, false).get
        val userAnswers = UserAnswers("test").set(IsNonUKLandlordPage, true).get
        navigator.nextPage(
          IsNonUKLandlordPage, taxYear, CheckMode, previousUserAnswers, userAnswers
        ) mustBe controllers.propertyrentals.income.routes.DeductingTaxController.onPageLoad(taxYear, CheckMode)
      }

      "must go from DeductingTax to CheckYourAnswers" in {
        navigator.nextPage(
          DeductingTaxPage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.propertyrentals.income.routes.PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from IncomeFromPropertyRentals to CheckYourAnswers" in {
        navigator.nextPage(
          IncomeFromPropertyRentalsPage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.propertyrentals.income.routes.PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from LeasePremiumPaymentPage to CalculateFigureYourselfPage when user selects yes and the previous answer was no" in {
        val previousUserAnswers = UserAnswers("test").set(LeasePremiumPaymentPage, false).get
        val userAnswers = UserAnswers("test").set(LeasePremiumPaymentPage, true).get
        navigator.nextPage(
          LeasePremiumPaymentPage, taxYear, CheckMode, previousUserAnswers, userAnswers
        ) mustBe CalculatedFigureYourselfController.onPageLoad(taxYear, CheckMode)
      }

      "must go from LeasePremiumPaymentPage to CheckYourAnswers when user selects yes and the previous answer was yes" in {
        val previousUserAnswers = UserAnswers("test").set(LeasePremiumPaymentPage, true).get
        val userAnswers = UserAnswers("test").set(LeasePremiumPaymentPage, true).get
        navigator.nextPage(
          LeasePremiumPaymentPage, taxYear, CheckMode, previousUserAnswers, userAnswers
        ) mustBe controllers.propertyrentals.income.routes.PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from LeasePremiumPaymentPage to CheckYourAnswers when user selects no" in {
        val previousUserAnswers = UserAnswers("test").set(LeasePremiumPaymentPage, true).get
        val userAnswers = UserAnswers("test").set(LeasePremiumPaymentPage, false).get
        navigator.nextPage(
          LeasePremiumPaymentPage, taxYear, CheckMode, previousUserAnswers, userAnswers
        ) mustBe controllers.propertyrentals.income.routes.PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from CalculatedFigureYourselfPage to RecievedGrantLeaseAmount when user selects no and the previous answer was yes" in {
        val previousUserAnswers = UserAnswers("test").set(CalculatedFigureYourselfPage, CalculatedFigureYourself(true, None)).get
        val userAnswers = UserAnswers("test").set(CalculatedFigureYourselfPage, CalculatedFigureYourself(false, None)).get
        navigator.nextPage(
          CalculatedFigureYourselfPage, taxYear, CheckMode, previousUserAnswers, userAnswers
        ) mustBe ReceivedGrantLeaseAmountController.onPageLoad(taxYear, CheckMode)
      }

      "must go from CalculatedFigureYourselfPage to CheckYourAnswers when user selects no and the previous answer was no" in {
        val previousUserAnswers = UserAnswers("test").set(CalculatedFigureYourselfPage, CalculatedFigureYourself(false, None)).get
        val userAnswers = UserAnswers("test").set(CalculatedFigureYourselfPage, CalculatedFigureYourself(false, None)).get
        navigator.nextPage(
          CalculatedFigureYourselfPage, taxYear, CheckMode, previousUserAnswers, userAnswers
        ) mustBe controllers.propertyrentals.income.routes.PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from CalculatedFigureYourselfPage to CheckYourAnswers when user selects" in {
        val previousUserAnswers = UserAnswers("test").set(CalculatedFigureYourselfPage, CalculatedFigureYourself(false, None)).get
        val userAnswers = UserAnswers("test").set(CalculatedFigureYourselfPage, CalculatedFigureYourself(true, Some(100))).get
        navigator.nextPage(
          CalculatedFigureYourselfPage, taxYear, CheckMode, previousUserAnswers, userAnswers
        ) mustBe controllers.propertyrentals.income.routes.PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from RecievedGrantLeaseAmountPage to YearLeaseAmount" in {
        navigator.nextPage(
          ReceivedGrantLeaseAmountPage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe YearLeaseAmountController.onPageLoad(taxYear, CheckMode)
      }

      "must go from YearLeaseAmountPage to PremiumsGrantLease" in {
        navigator.nextPage(
          YearLeaseAmountPage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe PremiumsGrantLeaseController.onPageLoad(taxYear, CheckMode)
      }

      "must go from PremiumsGrantLeasePage to CheckYourAnswers" in {
        navigator.nextPage(
          PremiumsGrantLeasePage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.propertyrentals.income.routes.PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from ReversePremiumsReceivedPage to CheckYourAnswers" in {
        navigator.nextPage(
          ReversePremiumsReceivedPage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.propertyrentals.income.routes.PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from OtherIncomeFromPropertyPage to CheckYourAnswers" in {
        navigator.nextPage(
          OtherIncomeFromPropertyPage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.propertyrentals.income.routes.PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("id")) mustBe
          controllers.about.routes.CheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from PrivateUseAdjustmentPage to AdjustmentsCheckYourAnswersPage" in {
        navigator.nextPage(
          PrivateUseAdjustmentPage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from BalancingChargePage to AdjustmentsCheckYourAnswersPage if no change in user-answers" in {
        val userAnswers = UserAnswers("test").set(BalancingChargePage, BalancingCharge(balancingChargeYesNo = true, Some(BigDecimal(10)))).get
        navigator.nextPage(
          BalancingChargePage, taxYear, CheckMode, userAnswers, userAnswers
        ) mustBe AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from BalancingChargePage to PropertyIncomeAllowancePage if change in user-answers" in {
        val previousUserAnswers = UserAnswers("test").set(BalancingChargePage, BalancingCharge(balancingChargeYesNo = true, Some(BigDecimal(10)))).get
        val userAnswers = UserAnswers("test").set(BalancingChargePage, BalancingCharge(balancingChargeYesNo = false, None)).get
        navigator.nextPage(
          BalancingChargePage, taxYear, CheckMode, previousUserAnswers, userAnswers
        ) mustBe PropertyIncomeAllowanceController.onPageLoad(taxYear, CheckMode)
      }

      "must go from PropertyIncomeAllowancePage to AdjustmentsCheckYourAnswersPage" in {
        navigator.nextPage(
          PropertyIncomeAllowancePage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from RenovationAllowanceBalancingChargePage to AdjustmentsCheckYourAnswersPage" in {
        navigator.nextPage(
          RenovationAllowanceBalancingChargePage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from ResidentialFinanceCostPage to AdjustmentsCheckYourAnswersPage" in {
        navigator.nextPage(
          ResidentialFinanceCostPage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from UnusedResidentialFinanceCostPage to AdjustmentsCheckYourAnswersPage" in {
        navigator.nextPage(
          UnusedResidentialFinanceCostPage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
      }
      "must go from AnnualInvestmentAllowancePage to AllowancesCheckYourAnswersPage" in {
        navigator.nextPage(
          AnnualInvestmentAllowancePage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe AllowancesCheckYourAnswersController.onPageLoad(taxYear)
      }
      "must go from ElectricChargePointAllowancePage to AllowancesCheckYourAnswersPage" in {
        navigator.nextPage(
          ElectricChargePointAllowancePage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe AllowancesCheckYourAnswersController.onPageLoad(taxYear)
      }
      "must go from ZeroEmissionCarAllowancePage to AllowancesCheckYourAnswersPage" in {
        navigator.nextPage(
          ZeroEmissionCarAllowancePage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe AllowancesCheckYourAnswersController.onPageLoad(taxYear)
      }
      "must go from ZeroEmissionGoodsVehicleAllowancePage to AllowancesCheckYourAnswersPage" in {
        navigator.nextPage(
          ZeroEmissionGoodsVehicleAllowancePage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe AllowancesCheckYourAnswersController.onPageLoad(taxYear)
      }
      "must go from BusinessPremisesRenovationPage to AllowancesCheckYourAnswersPage" in {
        navigator.nextPage(
          BusinessPremisesRenovationPage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe AllowancesCheckYourAnswersController.onPageLoad(taxYear)
      }
      "must go from ReplacementOfDomesticGoodsPage to AllowancesCheckYourAnswersPage" in {
        navigator.nextPage(
          ReplacementOfDomesticGoodsPage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe AllowancesCheckYourAnswersController.onPageLoad(taxYear)
      }
      "must go from OtherCapitalAllowancePage to AllowancesCheckYourAnswersPage" in {
        navigator.nextPage(
          OtherCapitalAllowancePage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe AllowancesCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from StructureBuildingQualifyingDatePage to StructureBuildingQualifyingAmountPage" in {
        navigator.nextPage(
          StructureBuildingQualifyingDatePage(index), taxYear, CheckMode, 0, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.structuresbuildingallowance.routes.SbaCheckYourAnswersController.onPageLoad(taxYear,0)
      }

      "must go from StructureBuildingQualifyingAmountPage to StructureBuildingAllowanceClaimPage" in {
        navigator.nextPage(
          StructureBuildingQualifyingAmountPage(index), taxYear, CheckMode, 0, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.structuresbuildingallowance.routes.SbaCheckYourAnswersController.onPageLoad(taxYear, 0)
      }

      "must go from EsbaQualifyingDatePage to EsbaQualifyingAmountPage" in {
        navigator.nextPage(
          EsbaQualifyingDatePage(index), taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.enhancedstructuresbuildingallowance.routes.EsbaQualifyingAmountController.onPageLoad(taxYear, index, CheckMode)
      }

      "must go from EsbaQualifyingAmountPage to EsbaClaimAmountPage" in {
        navigator.nextPage(
          EsbaQualifyingAmountPage(index), taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.enhancedstructuresbuildingallowance.routes.EsbaClaimAmountController.onPageLoad(taxYear, CheckMode, index)
      }

      "must go from EsbaClaimAmountPage to EsbaAddressPage" in {
        navigator.nextPage(
          EsbaClaimAmountPage(index), taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.enhancedstructuresbuildingallowance.routes.EsbaAddressController.onPageLoad(taxYear, CheckMode, index)
      }

      "must go from FhlMoreThanOnePage to FhlMainHomePage" in {
        navigator.nextPage(
          FhlMoreThanOnePage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.furnishedholidaylettings.routes.FhlMainHomeController.onPageLoad(taxYear, CheckMode)
      }

      "must go from FhlMainHomePage to FhlJointlyLetPage when yes selected" in {
        navigator.nextPage(
          FhlMainHomePage, taxYear, CheckMode, emptyUserAnswers, emptyUserAnswers.set(FhlMainHomePage, true).get
        ) mustBe controllers.furnishedholidaylettings.routes.FhlJointlyLetController.onPageLoad(taxYear, CheckMode)
      }

      "must go from FhlMainHomePage to FhlJointlyLetPage when no selected" in {
        navigator.nextPage(
          FhlMainHomePage, taxYear, CheckMode, emptyUserAnswers, emptyUserAnswers.set(FhlMainHomePage, false).get
        ) mustBe controllers.furnishedholidaylettings.routes.FhlClaimPiaOrExpensesController.onPageLoad(taxYear, CheckMode)
      }

      "must go from FhlJointlyLetPage to FhlReliefOrExpensesPage" in {
        navigator.nextPage(
          FhlJointlyLetPage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.furnishedholidaylettings.routes.FhlReliefOrExpensesController.onPageLoad(taxYear, CheckMode)
      }

      "must go from FhlReliefOrExpensesPage to FhlCheckYourAnswersPage" in {
        navigator.nextPage(
          FhlReliefOrExpensesPage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.furnishedholidaylettings.routes.FhlCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from FhlClaimPiaOrExpensesPage to FhlCheckYourAnswersPage" in {
        navigator.nextPage(
          FhlClaimPiaOrExpensesPage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("test")
        ) mustBe controllers.furnishedholidaylettings.routes.FhlCheckYourAnswersController.onPageLoad(taxYear)
      }
    }
  }
}
