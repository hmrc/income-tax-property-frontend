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

import controllers.about.routes._
import controllers.adjustments.routes._
import controllers.allowances.routes._
import controllers.enhancedstructuresbuildingallowance.routes._
import controllers.premiumlease.routes._
import controllers.propertyrentals.expenses.routes._
import controllers.propertyrentals.income.routes._
import controllers.propertyrentals.routes._
import controllers.rentalsandrentaroom.routes
import controllers.routes._
import controllers.structuresbuildingallowance.routes._
import controllers.ukrentaroom.adjustments.routes._
import controllers.ukrentaroom.allowances.routes._
import controllers.ukrentaroom.expenses.routes._
import controllers.ukrentaroom.routes._
import models.TotalIncome.{Between, Over, Under}
import models._
import pages._
import pages.adjustments._
import pages.allowances._
import pages.enhancedstructuresbuildingallowance._
import pages.premiumlease.{CalculatedFigureYourselfPage, LeasePremiumPaymentPage}
import pages.propertyrentals._
import pages.propertyrentals.expenses._
import pages.propertyrentals.income._
import pages.structurebuildingallowance._
import pages.ukrentaroom._
import pages.ukrentaroom.adjustments.{RaRAdjustmentsCompletePage, RaRBalancingChargePage, RaRUnusedResidentialCostsPage}
import pages.ukrentaroom.allowances._
import pages.ukrentaroom.expenses._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => Int => UserAnswers => UserAnswers => Call = {
    case IncomeSectionFinishedPage     => taxYear => _ => _ => SummaryController.show(taxYear)
    case AllowancesSectionFinishedPage => taxYear => _ => _ => SummaryController.show(taxYear)
    case ExpensesSectionFinishedPage   => taxYear => _ => _ => SummaryController.show(taxYear)

    case RaRCapitalAllowancesForACarPage =>
      taxYear => _ => _ => RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)
    case RaROtherCapitalAllowancesPage =>
      taxYear => _ => _ => RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)
    case RaRReplacementsOfDomesticGoodsPage =>
      taxYear => _ => _ => RaROtherCapitalAllowancesController.onPageLoad(taxYear, NormalMode)
    case RaRElectricChargePointAllowanceForAnEVPage =>
      taxYear => _ => _ => RaRZeroEmissionCarAllowanceController.onPageLoad(taxYear, NormalMode)
    case RaRZeroEmissionCarAllowancePage =>
      taxYear => _ => _ => RaRReplacementsOfDomesticGoodsController.onPageLoad(taxYear, NormalMode)
    case RaRAllowancesCompletePage => taxYear => _ => _ => SummaryController.show(taxYear)

    case ExpensesRRSectionCompletePage => taxYear => _ => _ => SummaryController.show(taxYear)
    case ConsolidatedExpensesRRPage =>
      taxYear =>
        _ =>
          userAnswers =>
            userAnswers.get(ConsolidatedExpensesRRPage) match {
              case Some(ConsolidatedRRExpenses(true, _)) => ExpensesCheckYourAnswersRRController.onPageLoad(taxYear)
              case Some(ConsolidatedRRExpenses(false, None)) =>
                RentsRatesAndInsuranceRRController.onPageLoad(taxYear, NormalMode)
            }
    case ExpensesRRSectionCompletePage => taxYear => _ => _ => SummaryController.show(taxYear)
    case OtherPropertyExpensesRRPage =>
      taxYear => _ => _ => ExpensesCheckYourAnswersRRController.onPageLoad(taxYear)
    case CostOfServicesProvidedRRPage =>
      taxYear => _ => _ => OtherPropertyExpensesRRController.onPageLoad(taxYear, NormalMode)
    case RepairsAndMaintenanceCostsRRPage =>
      taxYear => _ => _ => LegalManagementOtherFeeRRController.onPageLoad(taxYear, NormalMode)
    case RentsRatesAndInsuranceRRPage =>
      taxYear => _ => _ => RepairsAndMaintenanceCostsRRController.onPageLoad(taxYear, NormalMode)
    case LegalManagementOtherFeeRRPage =>
      taxYear => _ => _ => CostOfServicesProvidedRRController.onPageLoad(taxYear, NormalMode)

    case UKPropertyDetailsPage => taxYear => _ => _ => TotalIncomeController.onPageLoad(taxYear, NormalMode)
    case TotalIncomePage       => taxYear => _ => userAnswers => totalIncomeNavigationNormalMode(taxYear, userAnswers)
    case UKPropertySelectPage  => taxYear => _ => _ => SummaryController.show(taxYear)
    case ReportPropertyIncomePage =>
      taxYear => _ => userAnswers => reportIncomeNavigationNormalMode(taxYear, userAnswers)
    case UKPropertyPage => taxYear => _ => _ => controllers.about.routes.CheckYourAnswersController.onPageLoad(taxYear)
    case ExpensesLessThan1000Page =>
      taxYear => _ => _ => ClaimPropertyIncomeAllowanceController.onPageLoad(taxYear, NormalMode, Rentals)
    case ClaimPropertyIncomeAllowancePage(Rentals) =>
      taxYear =>
        _ =>
          _ =>
            PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
        // property income
    case IsNonUKLandlordPage(Rentals) =>
      taxYear => _ => userAnswers => isNonUKLandlordNavigation(taxYear, userAnswers, Rentals)
    case DeductingTaxPage(Rentals) =>
      taxYear => _ => _ => IncomeFromPropertyController.onPageLoad(taxYear, NormalMode, Rentals)
    case IncomeFromPropertyPage(Rentals) =>
      taxYear => _ => _ => LeasePremiumPaymentController.onPageLoad(taxYear, NormalMode, Rentals)
    case premiumlease.LeasePremiumPaymentPage(Rentals) =>
      taxYear => _ => userAnswers => leasePremiumPaymentNavigation(taxYear, userAnswers, Rentals)
    case CalculatedFigureYourselfPage(Rentals) =>
      taxYear => _ => userAnswers => calculatedFigureYourselfNavigation(taxYear, userAnswers, Rentals)
    case premiumlease.ReceivedGrantLeaseAmountPage(Rentals) =>
      taxYear => _ => _ => YearLeaseAmountController.onPageLoad(taxYear, NormalMode)
    case premiumlease.YearLeaseAmountPage(Rentals) =>
      taxYear => _ => _ => PremiumsGrantLeaseController.onPageLoad(taxYear, NormalMode)
    case premiumlease.PremiumsGrantLeasePage(Rentals) =>
      taxYear => _ => _ => ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode)
    case ReversePremiumsReceivedPage(Rentals) =>
      taxYear => _ => _ => OtherIncomeFromPropertyController.onPageLoad(taxYear, NormalMode)
    case OtherIncomeFromPropertyPage(Rentals) =>
      taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case PrivateUseAdjustmentPage => taxYear => _ => _ => BalancingChargeController.onPageLoad(taxYear, NormalMode)
    case BalancingChargePage => taxYear => _ => _ => PropertyIncomeAllowanceController.onPageLoad(taxYear, NormalMode)
    case PropertyIncomeAllowancePage =>
      taxYear => _ => _ => RenovationAllowanceBalancingChargeController.onPageLoad(taxYear, NormalMode)
    case RenovationAllowanceBalancingChargePage =>
      taxYear => _ => _ => ResidentialFinanceCostController.onPageLoad(taxYear, NormalMode)
    case ResidentialFinanceCostPage =>
      taxYear => _ => _ => UnusedResidentialFinanceCostController.onPageLoad(taxYear, NormalMode)
    case UnusedResidentialFinanceCostPage =>
      taxYear =>
        _ =>
          _ =>
            AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
        // expenses
    case ConsolidatedExpensesPage(Rentals) =>
      taxYear => _ => userAnswers => consolidatedExpensesNavigation(taxYear, userAnswers, Rentals)
    case RentsRatesAndInsurancePage(Rentals) =>
      taxYear => _ => _ => RepairsAndMaintenanceCostsController.onPageLoad(taxYear, NormalMode)
    case RepairsAndMaintenanceCostsPage(Rentals) =>
      taxYear => _ => _ => LoanInterestController.onPageLoad(taxYear, NormalMode)
    case LoanInterestPage(Rentals) =>
      taxYear => _ => _ => OtherProfessionalFeesController.onPageLoad(taxYear, NormalMode)
    case OtherProfessionalFeesPage(Rentals) =>
      taxYear => _ => _ => CostsOfServicesProvidedController.onPageLoad(taxYear, NormalMode)
    case CostsOfServicesProvidedPage(Rentals) =>
      taxYear => _ => _ => PropertyBusinessTravelCostsController.onPageLoad(taxYear, NormalMode)
    case PropertyBusinessTravelCostsPage(Rentals) =>
      taxYear => _ => _ => OtherAllowablePropertyExpensesController.onPageLoad(taxYear, NormalMode)
    case AnnualInvestmentAllowancePage =>
      taxYear =>
        _ =>
          _ =>
            ElectricChargePointAllowanceController.onPageLoad(taxYear, NormalMode)

        // allowances
    case CapitalAllowancesForACarPage => taxYear => _ => _ => AllowancesCheckYourAnswersController.onPageLoad(taxYear)
    case AnnualInvestmentAllowancePage =>
      taxYear => _ => _ => ElectricChargePointAllowanceController.onPageLoad(taxYear, NormalMode)
    case ElectricChargePointAllowancePage =>
      taxYear =>
        _ => _ => controllers.allowances.routes.ZeroEmissionCarAllowanceController.onPageLoad(taxYear, NormalMode)
    case ZeroEmissionCarAllowancePage =>
      taxYear => _ => _ => ZeroEmissionGoodsVehicleAllowanceController.onPageLoad(taxYear, NormalMode)
    case ZeroEmissionGoodsVehicleAllowancePage =>
      taxYear => _ => _ => BusinessPremisesRenovationController.onPageLoad(taxYear, NormalMode)
    case BusinessPremisesRenovationPage =>
      taxYear => _ => _ => ReplacementOfDomesticGoodsController.onPageLoad(taxYear, NormalMode)
    case ReplacementOfDomesticGoodsPage =>
      taxYear => _ => _ => OtherCapitalAllowanceController.onPageLoad(taxYear, NormalMode)
    case OtherCapitalAllowancePage => taxYear => _ => _ => AllowancesCheckYourAnswersController.onPageLoad(taxYear)
    case OtherAllowablePropertyExpensesPage(Rentals) =>
      taxYear =>
        _ =>
          _ =>
            ExpensesCheckYourAnswersController.onPageLoad(taxYear)

        // Structured building allowance
    case ClaimStructureBuildingAllowancePage =>
      taxYear => _ => userAnswers => structureBuildingAllowanceNavigationNormalMode(taxYear, userAnswers)
    case StructureBuildingAllowancePage =>
      taxYear => _ => _ => ClaimStructureBuildingAllowanceController.onPageLoad(taxYear, NormalMode)
    case SbaClaimsPage => taxYear => _ => userAnswers => sbaClaimsNavigationNormalMode(taxYear, userAnswers)
    case SbaRemoveConfirmationPage =>
      taxYear => _ => userAnswers => sbaRemoveConfirmationNavigationNormalMode(taxYear, userAnswers)
    case SbaSectionFinishedPage =>
      taxYear =>
        _ =>
          _ =>
            SummaryController.show(taxYear)

        // Enhanced structured building allowance
    case ClaimEsbaPage =>
      taxYear => _ => userAnswers => enhancedStructureBuildingAllowanceNavigationNormalMode(taxYear, userAnswers)
    case EsbaClaimsPage => taxYear => _ => userAnswers => esbaClaimsNavigationNormalMode(taxYear, userAnswers)
    case EsbaRemoveConfirmationPage =>
      taxYear => _ => userAnswers => esbaRemoveConfirmationNavigationNormalMode(taxYear, userAnswers)
    case EsbaSectionFinishedPage => taxYear => _ => _ => SummaryController.show(taxYear)
    case AboutSectionCompletePage =>
      taxYear => _ => _ => AboutSectionCompleteController.onPageLoad(taxYear)
    case TotalIncomeAmountPage(RentARoom) =>
      taxYear => _ => _ => ClaimExpensesOrRRRController.onPageLoad(taxYear, NormalMode, RentARoom)
    case AboutSectionCompletePage =>
      taxYear =>
        _ =>
          _ =>
            AboutSectionCompleteController.onPageLoad(taxYear)

        // Rent a Room
    case RentsRatesAndInsuranceRRPage =>
      taxYear => _ => _ => RepairsAndMaintenanceCostsRRController.onPageLoad(taxYear, NormalMode)

    case RaRCapitalAllowancesForACarPage =>
      taxYear => _ => _ => RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)

    case RaRElectricChargePointAllowanceForAnEVPage =>
      taxYear => _ => _ => RaRZeroEmissionCarAllowanceController.onPageLoad(taxYear, NormalMode)
    case RaRZeroEmissionCarAllowancePage =>
      taxYear => _ => _ => RaRReplacementsOfDomesticGoodsController.onPageLoad(taxYear, NormalMode)
    case RaRReplacementsOfDomesticGoodsPage =>
      taxYear => _ => _ => RaROtherCapitalAllowancesController.onPageLoad(taxYear, NormalMode)
    case RaROtherCapitalAllowancesPage =>
      taxYear => _ => _ => RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)
    case JointlyLetPage(RentARoom) =>
      taxYear => _ => _ => TotalIncomeAmountController.onPageLoad(taxYear, NormalMode, RentARoom)
    case ClaimExpensesOrRRRPage(RentARoom) =>
      taxYear =>
        _ =>
          _ =>
            controllers.ukrentaroom.routes.CheckYourAnswersController.onPageLoad(taxYear)

        // RAR Adjustments
    case RaRBalancingChargePage =>
      taxYear => _ => _ => RaRUnusedResidentialCostsController.onPageLoad(taxYear, NormalMode)
    case RaRUnusedResidentialCostsPage =>
      taxYear => _ => _ => RaRAdjustmentsCYAController.onPageLoad(taxYear)
    case RaRAdjustmentsCompletePage     => taxYear => _ => _ => SummaryController.show(taxYear)
    case RentalsAdjustmentsCompletePage => taxYear => _ => _ => SummaryController.show(taxYear)
    case AboutPropertyCompletePage      => taxYear => _ => _ => SummaryController.show(taxYear)

    case AboutPropertyRentalsSectionFinishedPage =>
      taxYear =>
        _ =>
          _ =>
            SummaryController.show(taxYear)

        // Rentals and Rent a Room About
    case JointlyLetPage(RentalsRentARoom) =>
      taxYear => _ => _ => TotalIncomeAmountController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)

    case TotalIncomeAmountPage(RentalsRentARoom) =>
      taxYear => _ => _ => ClaimExpensesOrRRRController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)

    case ClaimExpensesOrRRRPage(RentalsRentARoom) =>
      taxYear => _ => _ => ClaimPropertyIncomeAllowanceController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)

    case ClaimPropertyIncomeAllowancePage(RentalsRentARoom) =>
      taxYear => _ => _ => routes.RentalsAndRaRCheckYourAnswersController.onPageLoad(taxYear)

    case _ => _ => _ => _ => IndexController.onPageLoad

  }

  private val checkRouteMap: Page => Int => UserAnswers => UserAnswers => Call = {
    case RaRCapitalAllowancesForACarPage =>
      taxYear => _ => _ => RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)
    case RaROtherCapitalAllowancesPage =>
      taxYear => _ => _ => RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)
    case RaRReplacementsOfDomesticGoodsPage =>
      taxYear => _ => _ => RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)
    case RaRZeroEmissionCarAllowancePage =>
      taxYear => _ => _ => RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)
    case RaRElectricChargePointAllowanceForAnEVPage =>
      taxYear => _ => _ => RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)

    case OtherPropertyExpensesRRPage => taxYear => _ => _ => ExpensesCheckYourAnswersRRController.onPageLoad(taxYear)
    case ConsolidatedExpensesRRPage =>
      taxYear =>
        previousUserAnswers =>
          userAnswers => consolidatedExpensesRRNavigationCheckMode(taxYear, previousUserAnswers, userAnswers)
    case LegalManagementOtherFeeRRPage => taxYear => _ => _ => ExpensesCheckYourAnswersRRController.onPageLoad(taxYear)
    case CostOfServicesProvidedRRPage  => taxYear => _ => _ => ExpensesCheckYourAnswersRRController.onPageLoad(taxYear)

    case RepairsAndMaintenanceCostsRRPage =>
      taxYear => _ => _ => ExpensesCheckYourAnswersRRController.onPageLoad(taxYear)
    case RentsRatesAndInsuranceRRPage => taxYear => _ => _ => ExpensesCheckYourAnswersRRController.onPageLoad(taxYear)
    case ExpensesLessThan1000Page =>
      taxYear => _ => _ => PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
    case ClaimPropertyIncomeAllowancePage(Rentals) =>
      taxYear => _ => _ => PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
    case TotalIncomePage =>
      taxYear =>
        previousUserAnswers =>
          userAnswers =>
            totalIncomeNavigationCheckMode(taxYear, previousUserAnswers, userAnswers)
        // property income
    case IsNonUKLandlordPage(Rentals) =>
      taxYear =>
        previousUserAnswers =>
          userAnswers => isNonUKLandlordNavigationCheckMode(taxYear, previousUserAnswers, userAnswers, Rentals)
    case DeductingTaxPage(Rentals) => taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case IncomeFromPropertyPage(Rentals) =>
      taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case premiumlease.LeasePremiumPaymentPage(Rentals) =>
      taxYear =>
        previousUserAnswers =>
          userAnswers => leasePremiumPaymentNavigationCheckMode(taxYear, previousUserAnswers, userAnswers, Rentals)
    case CalculatedFigureYourselfPage(Rentals) =>
      taxYear =>
        previousUserAnswers =>
          userAnswers => calculatedFigureYourselfNavigationCheckMode(taxYear, previousUserAnswers, userAnswers, Rentals)
    case premiumlease.ReceivedGrantLeaseAmountPage(Rentals) =>
      taxYear => _ => _ => YearLeaseAmountController.onPageLoad(taxYear, CheckMode)
    case premiumlease.YearLeaseAmountPage(Rentals) =>
      taxYear => _ => _ => PremiumsGrantLeaseController.onPageLoad(taxYear, CheckMode)
    case premiumlease.PremiumsGrantLeasePage(Rentals) =>
      taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case ReversePremiumsReceivedPage(Rentals) =>
      taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case OtherIncomeFromPropertyPage(Rentals) =>
      taxYear =>
        _ =>
          _ =>
            PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
        // Adjustments
    case PrivateUseAdjustmentPage | PropertyIncomeAllowancePage | RenovationAllowanceBalancingChargePage |
        ResidentialFinanceCostPage | UnusedResidentialFinanceCostPage =>
      taxYear => _ => _ => AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
    case BalancingChargePage =>
      taxYear =>
        previousUserAnswers =>
          userAnswers =>
            balancingChargeNavigationCheckMode(taxYear, previousUserAnswers, userAnswers)
          // expenses
          //    case ConsolidatedExpensesPage => taxYear => _ => userAnswers => ExpensesCheckYourAnswersController.onPageLoad(taxYear)
        // Allowances
    case CapitalAllowancesForACarPage | AnnualInvestmentAllowancePage | ElectricChargePointAllowancePage |
        ZeroEmissionCarAllowancePage | ZeroEmissionGoodsVehicleAllowancePage | BusinessPremisesRenovationPage |
        ReplacementOfDomesticGoodsPage | OtherCapitalAllowancePage =>
      taxYear =>
        _ =>
          _ =>
            AllowancesCheckYourAnswersController.onPageLoad(taxYear)
        // Expenses
    case RentsRatesAndInsurancePage(Rentals) | RepairsAndMaintenanceCostsPage(Rentals) | LoanInterestPage(Rentals) |
        OtherProfessionalFeesPage(Rentals) | CostsOfServicesProvidedPage(Rentals) | PropertyBusinessTravelCostsPage(
          Rentals
        ) | OtherAllowablePropertyExpensesPage(Rentals) =>
      taxYear => _ => userAnswers => ExpensesCheckYourAnswersController.onPageLoad(taxYear)
    case ConsolidatedExpensesPage(Rentals) =>
      taxYear =>
        previousUserAnswers =>
          userAnswers =>
            consolidatedExpensesNavigationCheckMode(taxYear, previousUserAnswers, userAnswers, Rentals)

        // Enhanced structured building allowance
    case EsbaQualifyingDatePage(index) =>
      taxYear =>
        _ =>
          _ =>
            controllers.enhancedstructuresbuildingallowance.routes.EsbaQualifyingAmountController
              .onPageLoad(taxYear, index, CheckMode)
    case EsbaQualifyingAmountPage(index) =>
      taxYear =>
        _ =>
          _ =>
            controllers.enhancedstructuresbuildingallowance.routes.EsbaClaimAmountController
              .onPageLoad(taxYear, CheckMode, index)
    case EsbaClaimAmountPage(index) =>
      taxYear =>
        _ =>
          _ =>
            controllers.enhancedstructuresbuildingallowance.routes.EsbaAddressController
              .onPageLoad(taxYear, CheckMode, index)
    case TotalIncomeAmountPage(RentARoom) =>
      taxYear => _ => _ => controllers.ukrentaroom.routes.CheckYourAnswersController.onPageLoad(taxYear)
    case JointlyLetPage(RentARoom) =>
      taxYear => _ => _ => controllers.ukrentaroom.routes.CheckYourAnswersController.onPageLoad(taxYear)

    case ClaimExpensesOrRRRPage(RentARoom) =>
      taxYear => _ => _ => controllers.ukrentaroom.routes.CheckYourAnswersController.onPageLoad(taxYear)

    case RaRCapitalAllowancesForACarPage =>
      taxYear => _ => _ => RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)

    case RaRElectricChargePointAllowanceForAnEVPage =>
      taxYear => _ => _ => RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)
    case RaRZeroEmissionCarAllowancePage =>
      taxYear => _ => _ => RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)
    case RaRReplacementsOfDomesticGoodsPage =>
      taxYear => _ => _ => RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)
    case RaROtherCapitalAllowancesPage =>
      taxYear => _ => _ => RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)

    case RaRBalancingChargePage =>
      taxYear => _ => _ => RaRAdjustmentsCYAController.onPageLoad(taxYear)

    case RaRUnusedResidentialCostsPage =>
      taxYear =>
        _ =>
          _ =>
            RaRAdjustmentsCYAController.onPageLoad(taxYear)

          // Rentals and Rent a Room

        // About
    case JointlyLetPage(RentalsRentARoom) =>
      taxYear => _ => _ => routes.RentalsAndRaRCheckYourAnswersController.onPageLoad(taxYear)
    case TotalIncomeAmountPage(RentalsRentARoom) =>
      taxYear => _ => _ => routes.RentalsAndRaRCheckYourAnswersController.onPageLoad(taxYear)
    case ClaimExpensesOrRRRPage(RentalsRentARoom) =>
      taxYear => _ => _ => ClaimPropertyIncomeAllowanceController.onPageLoad(taxYear, CheckMode, RentalsRentARoom)
    case ClaimPropertyIncomeAllowancePage(RentalsRentARoom) =>
      taxYear => _ => _ => routes.RentalsAndRaRCheckYourAnswersController.onPageLoad(taxYear)
    case ClaimPropertyIncomeAllowancePage(RentalsRentARoom) =>
      taxYear => _ => _ => routes.RentalsAndRaRCheckYourAnswersController.onPageLoad(taxYear)

    case _ =>
      taxYear => _ => userAnswers => controllers.about.routes.CheckYourAnswersController.onPageLoad(taxYear)

  }

  def nextPage(page: Page, taxYear: Int, mode: Mode, previousUserAnswers: UserAnswers, userAnswers: UserAnswers): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(taxYear)(previousUserAnswers)(userAnswers)
      case CheckMode =>
        checkRouteMap(page)(taxYear)(previousUserAnswers)(userAnswers)
    }

  def nextPage(
    page: Page,
    taxYear: Int,
    mode: Mode,
    index: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers
  ): Call = mode match {
    case NormalMode =>
      structureBuildingNormalRoutes(page, taxYear, index)
    case CheckMode =>
      structureBuildingCheckModeRoutes(page, taxYear, index)
  }

  private def structureBuildingNormalRoutes(
    page: Page,
    taxYear: Int,
    index: Int
  ): Call = page match {
    case StructureBuildingQualifyingDatePage(_) =>
      StructureBuildingQualifyingAmountController.onPageLoad(taxYear, NormalMode, index)
    case StructureBuildingQualifyingAmountPage(_) =>
      StructureBuildingAllowanceClaimController.onPageLoad(taxYear, NormalMode, index)
    case StructureBuildingAllowanceClaimPage(_) =>
      StructuredBuildingAllowanceAddressController.onPageLoad(taxYear, NormalMode, index)
    case StructuredBuildingAllowanceAddressPage(_) => SbaCheckYourAnswersController.onPageLoad(taxYear, index)
    case _                                         => IndexController.onPageLoad
  }

  private def structureBuildingCheckModeRoutes(
    page: Page,
    taxYear: Int,
    index: Int
  ): Call = page match {
    case StructureBuildingQualifyingDatePage(_) | StructureBuildingQualifyingAmountPage(_) |
        StructureBuildingAllowanceClaimPage(_) | StructuredBuildingAllowanceAddressPage(_) =>
      SbaCheckYourAnswersController.onPageLoad(taxYear, index)
    case _ => IndexController.onPageLoad
  }

  def esbaNextPage(
    page: Page,
    taxYear: Int,
    mode: Mode,
    index: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers
  ): Call = mode match {
    case NormalMode =>
      esbaNormalRoutes(page, taxYear, index)
    case CheckMode =>
      esbaCheckModeRoutes(page, taxYear, index)
  }
  private def esbaNormalRoutes(
    page: Page,
    taxYear: Int,
    index: Int
  ): Call = page match {
    case EsbaQualifyingDatePage(_)   => EsbaQualifyingAmountController.onPageLoad(taxYear, index, NormalMode)
    case EsbaQualifyingAmountPage(_) => EsbaClaimAmountController.onPageLoad(taxYear, NormalMode, index)
    case EsbaClaimAmountPage(_)      => EsbaAddressController.onPageLoad(taxYear, NormalMode, index)
    case EsbaAddressPage(_)          => EsbaCheckYourAnswersController.onPageLoad(taxYear, index)
    case _                           => IndexController.onPageLoad
  }

  private def esbaCheckModeRoutes(
    page: Page,
    taxYear: Int,
    index: Int
  ): Call = page match {
    case EsbaQualifyingDatePage(_) | EsbaQualifyingAmountPage(_) | EsbaClaimAmountPage(_) | EsbaAddressPage(_) =>
      EsbaCheckYourAnswersController.onPageLoad(taxYear, index)
    case _ => IndexController.onPageLoad
  }

  private def isNonUKLandlordNavigation(taxYear: Int, userAnswers: UserAnswers, propertyType: PropertyType): Call =
    userAnswers.get(IsNonUKLandlordPage(propertyType)) match {
      case Some(true) => DeductingTaxController.onPageLoad(taxYear, NormalMode, Rentals)
      case _          => IncomeFromPropertyController.onPageLoad(taxYear, NormalMode, Rentals)
    }

  private def isNonUKLandlordNavigationCheckMode(
    taxYear: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers,
    propertyType: PropertyType
  ): Call =
    userAnswers.get(IsNonUKLandlordPage(propertyType)) match {
      case Some(true) if !previousUserAnswers.get(IsNonUKLandlordPage(propertyType)).getOrElse(false) =>
        DeductingTaxController.onPageLoad(taxYear, CheckMode, Rentals)
      case _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    }

  private def leasePremiumPaymentNavigation(taxYear: Int, userAnswers: UserAnswers, propertyType: PropertyType): Call =
    userAnswers.get(LeasePremiumPaymentPage(propertyType)) match {
      case Some(true) => CalculatedFigureYourselfController.onPageLoad(taxYear, NormalMode, propertyType)
      case _          => ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode)
    }

  private def leasePremiumPaymentNavigationCheckMode(
    taxYear: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers,
    propertyType: PropertyType
  ): Call =
    userAnswers.get(LeasePremiumPaymentPage(propertyType)) match {
      case Some(true) if !previousUserAnswers.get(LeasePremiumPaymentPage(propertyType)).getOrElse(false) =>
        CalculatedFigureYourselfController.onPageLoad(taxYear, CheckMode, propertyType)
      case _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    }

  private def calculatedFigureYourselfNavigation(
    taxYear: Int,
    userAnswers: UserAnswers,
    propertyType: PropertyType
  ): Call =
    userAnswers.get(CalculatedFigureYourselfPage(propertyType)) match {
      case Some(CalculatedFigureYourself(true, _)) => ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode)
      case Some(CalculatedFigureYourself(false, _)) =>
        ReceivedGrantLeaseAmountController.onPageLoad(taxYear, NormalMode)
    }

  private def calculatedFigureYourselfNavigationCheckMode(
    taxYear: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers,
    propertyType: PropertyType
  ): Call =
    userAnswers.get(CalculatedFigureYourselfPage(propertyType)) match {
      case Some(CalculatedFigureYourself(false, _))
          if previousUserAnswers
            .get(CalculatedFigureYourselfPage(propertyType))
            .map(_.calculatedFigureYourself)
            .getOrElse(true) =>
        ReceivedGrantLeaseAmountController.onPageLoad(taxYear, CheckMode)
      case _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    }

  private def consolidatedExpensesNavigation(taxYear: Int, userAnswers: UserAnswers, propertyType: PropertyType): Call =
    userAnswers.get(ConsolidatedExpensesPage(propertyType)) match {
      case Some(ConsolidatedExpenses(true, _))  => ExpensesCheckYourAnswersController.onPageLoad(taxYear)
      case Some(ConsolidatedExpenses(false, _)) => RentsRatesAndInsuranceController.onPageLoad(taxYear, NormalMode)
    }

  private def consolidatedExpensesRRNavigationCheckMode(
    taxYear: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(ConsolidatedExpensesRRPage) match {
      case Some(ConsolidatedRRExpenses(false, _))
          if previousUserAnswers.get(ConsolidatedExpensesRRPage).map(_.consolidatedExpensesYesOrNo).getOrElse(true) =>
        RentsRatesAndInsuranceRRController.onPageLoad(taxYear, NormalMode)
      case _ =>
        ExpensesCheckYourAnswersRRController.onPageLoad(taxYear)
    }

  private def consolidatedExpensesNavigationCheckMode(
    taxYear: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers,
    propertyType: PropertyType
  ): Call =
    userAnswers.get(ConsolidatedExpensesPage(propertyType)) match {
      case Some(ConsolidatedExpenses(false, _))
          if previousUserAnswers
            .get(ConsolidatedExpensesPage(propertyType))
            .map(_.consolidatedExpensesYesOrNo)
            .getOrElse(true) =>
        RentsRatesAndInsuranceController.onPageLoad(taxYear, NormalMode)
      case _ =>
        ExpensesCheckYourAnswersController.onPageLoad(taxYear)
    }

  private def balancingChargeNavigationCheckMode(
    taxYear: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers
  ): Call =
    (userAnswers.get(BalancingChargePage), previousUserAnswers.get(BalancingChargePage)) match {
      case (Some(current), Some(previous))
          if current.balancingChargeYesNo == previous.balancingChargeYesNo &&
            current.balancingChargeAmount == previous.balancingChargeAmount =>
        AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
      case _ => PropertyIncomeAllowanceController.onPageLoad(taxYear, CheckMode)
    }

  private def totalIncomeNavigationNormalMode(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(TotalIncomePage) match {
      case Some(Under) => ReportPropertyIncomeController.onPageLoad(taxYear, NormalMode)
      case _           => UKPropertySelectController.onPageLoad(taxYear, NormalMode)
    }

  private def reportIncomeNavigationNormalMode(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(ReportPropertyIncomePage) match {
      case Some(true) => UKPropertySelectController.onPageLoad(taxYear, NormalMode)
      case _          => controllers.about.routes.CheckYourAnswersController.onPageLoad(taxYear)
    }

  private def totalIncomeNavigationCheckMode(
    taxYear: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers
  ): Call =
    (previousUserAnswers.get(TotalIncomePage), userAnswers.get(TotalIncomePage)) match {
      case (Some(Between), Some(Under)) | (Some(Over), Some(Under)) =>
        ReportPropertyIncomeController.onPageLoad(taxYear, NormalMode)
      case (Some(Under), Some(Between)) | (Some(Under), Some(Over)) =>
        UKPropertySelectController.onPageLoad(taxYear, NormalMode)
      case _ => controllers.about.routes.CheckYourAnswersController.onPageLoad(taxYear)
    }

  private def structureBuildingAllowanceNavigationNormalMode(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(ClaimStructureBuildingAllowancePage) match {
      case Some(true) => AddClaimStructureBuildingAllowanceController.onPageLoad(taxYear)
      case _          => SummaryController.show(taxYear)
    }

  private def enhancedStructureBuildingAllowanceNavigationNormalMode(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(ClaimEsbaPage) match {
      case Some(true) => EsbaAddClaimController.onPageLoad(taxYear)
      case _          => SummaryController.show(taxYear)
    }

  private def sbaClaimsNavigationNormalMode(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(SbaClaimsPage) match {
      case Some(true) => AddClaimStructureBuildingAllowanceController.onPageLoad(taxYear)
      case _          => SbaSectionFinishedController.onPageLoad(taxYear)
    }

  private def sbaRemoveConfirmationNavigationNormalMode(taxYear: Int, userAnswers: UserAnswers): Call =
    (userAnswers.get(SbaRemoveConfirmationPage), userAnswers.get(StructureBuildingFormGroup)) match {
      case (Some(true), Some(sbaForm)) if sbaForm.isEmpty =>
        AddClaimStructureBuildingAllowanceController.onPageLoad(taxYear)
      case (_, Some(sbaForm)) if sbaForm.nonEmpty => SbaClaimsController.onPageLoad(taxYear)
    }

  private def esbaClaimsNavigationNormalMode(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(EsbaClaimsPage) match {
      case Some(true) => EsbaAddClaimController.onPageLoad(taxYear)
      case _          => SummaryController.show(taxYear)
    }

  private def esbaRemoveConfirmationNavigationNormalMode(taxYear: Int, userAnswers: UserAnswers): Call =
    (userAnswers.get(EsbaRemoveConfirmationPage), userAnswers.get(EnhancedStructureBuildingFormGroup)) match {
      case (Some(true), Some(esbaForm)) if esbaForm.isEmpty => EsbaAddClaimController.onPageLoad(taxYear)
      case (_, Some(esbaForm)) if esbaForm.nonEmpty         => EsbaClaimsController.onPageLoad(taxYear)
    }

}
