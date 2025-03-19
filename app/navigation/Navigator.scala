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
import controllers.rentalsandrentaroom.adjustments.routes._
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
import pages.premiumlease.{CalculatedFigureYourselfPage, PremiumForLeasePage}
import pages.propertyrentals._
import pages.propertyrentals.expenses._
import pages.propertyrentals.income._
import pages.rentalsandrentaroom.adjustments.BusinessPremisesRenovationAllowanceBalancingChargePage
import pages.structurebuildingallowance._
import pages.ukrentaroom._
import pages.ukrentaroom.adjustments._
import pages.ukrentaroom.allowances._
import pages.ukrentaroom.expenses._
import play.api.mvc.Call
import service.CYADiversionService

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() (diversionService: CYADiversionService) {

  private val normalRoutes: Page => Int => UserAnswers => UserAnswers => Call = {
    case IncomeSectionFinishedPage     => taxYear => _ => _ => SummaryController.show(taxYear)
    case AllowancesSectionFinishedPage => taxYear => _ => _ => SummaryController.show(taxYear)
    case ExpensesSectionFinishedPage   => taxYear => _ => _ => SummaryController.show(taxYear)

    case RaRCapitalAllowancesForACarPage =>
      taxYear => _ => _ => RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)
    case RaROtherCapitalAllowancesPage =>
      taxYear => _ => _ => RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)
    case RaRReplacementsOfDomesticGoodsPage =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", RentARoom) {
              RaROtherCapitalAllowancesController.onPageLoad(taxYear, NormalMode)
            }
    case RaRElectricChargePointAllowanceForAnEVPage =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", RentARoom) {
              RaRZeroEmissionCarAllowanceController.onPageLoad(taxYear, NormalMode)
            }
    case RaRZeroEmissionCarAllowancePage =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", RentARoom) {
              RaRReplacementsOfDomesticGoodsController.onPageLoad(taxYear, NormalMode)
            }
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
    case ExpensesRRSectionCompletePage =>
      taxYear => _ => _ => SummaryController.show(taxYear)
    case OtherPropertyExpensesRRPage =>
      taxYear => _ => _ => ExpensesCheckYourAnswersRRController.onPageLoad(taxYear)
    case CostOfServicesProvidedRRPage =>
      taxYear =>
        _ =>
          userAnswers =>
              OtherPropertyExpensesRRController.onPageLoad(taxYear, NormalMode)
    case RepairsAndMaintenanceCostsRRPage =>
      taxYear =>
        _ =>
          userAnswers =>
              LegalManagementOtherFeeRRController.onPageLoad(taxYear, NormalMode)
    case RentsRatesAndInsuranceRRPage =>
      taxYear =>
        _ =>
          userAnswers =>
              RepairsAndMaintenanceCostsRRController.onPageLoad(taxYear, NormalMode)
    case LegalManagementOtherFeeRRPage =>
      taxYear =>
        _ =>
          userAnswers =>
              CostOfServicesProvidedRRController.onPageLoad(taxYear, NormalMode)

    case UKPropertyDetailsPage => taxYear => _ => _ => TotalIncomeController.onPageLoad(taxYear, NormalMode)
    case TotalIncomePage       => taxYear => _ => userAnswers => totalIncomeNavigationNormalMode(taxYear, userAnswers)
    case UKPropertySelectPage  => taxYear => _ => _ => SummaryController.show(taxYear)
    case ReportPropertyIncomePage =>
      taxYear => _ => userAnswers => reportIncomeNavigationNormalMode(taxYear, userAnswers)
    case UKPropertyPage => taxYear => _ => _ => controllers.about.routes.CheckYourAnswersController.onPageLoad(taxYear)

    case ClaimPropertyIncomeAllowancePage(Rentals) =>
      taxYear =>
        _ =>
          _ =>
            PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
        // Rentals-Income
    case IsNonUKLandlordPage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "income", Rentals) {
              isNonUKLandlordNavigation(taxYear, userAnswers, Rentals)
            }
    case DeductingTaxPage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "income", Rentals)(
              PropertyRentalIncomeController.onPageLoad(taxYear, NormalMode, Rentals)
            )
    case PropertyRentalIncomePage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "income", Rentals)(
              PremiumForLeaseController.onPageLoad(taxYear, NormalMode, Rentals)
            )
    case premiumlease.PremiumForLeasePage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "income", Rentals)(
              premiumForLeaseNavigation(taxYear, userAnswers, Rentals)
            )
    case CalculatedFigureYourselfPage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "income", Rentals)(
              calculatedFigureYourselfNavigation(taxYear, userAnswers, Rentals)
            )
    case premiumlease.ReceivedGrantLeaseAmountPage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "income", Rentals)(
              YearLeaseAmountController.onPageLoad(taxYear, NormalMode, Rentals)
            )
    case premiumlease.YearLeaseAmountPage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "income", Rentals)(
              PremiumsGrantLeaseController.onPageLoad(taxYear, NormalMode, Rentals)
            )
    case premiumlease.PremiumsGrantLeasePage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "income", Rentals)(
              ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode, Rentals)
            )
    case ReversePremiumsReceivedPage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "income", Rentals)(
              OtherPropertyRentalIncomeController.onPageLoad(taxYear, NormalMode, Rentals)
            )
    case OtherIncomeFromPropertyPage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "income", Rentals)(
              PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
            )
        // Rentals Rent a Room-Income
    case IsNonUKLandlordPage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "income", RentalsRentARoom) {
              isNonUKLandlordNavigation(taxYear, userAnswers, RentalsRentARoom)
            }
    case DeductingTaxPage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "income", RentalsRentARoom) {
              PremiumForLeaseController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
            }
    case premiumlease.PremiumForLeasePage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "income", RentalsRentARoom) {
              premiumForLeaseNavigation(taxYear, userAnswers, RentalsRentARoom)
            }
    case CalculatedFigureYourselfPage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "income", RentalsRentARoom) {
              calculatedFigureYourselfNavigation(taxYear, userAnswers, RentalsRentARoom)
            }
    case premiumlease.ReceivedGrantLeaseAmountPage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "income", RentalsRentARoom) {
              YearLeaseAmountController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
            }
    case premiumlease.YearLeaseAmountPage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "income", RentalsRentARoom) {
              PremiumsGrantLeaseController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
            }
    case premiumlease.PremiumsGrantLeasePage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "income", RentalsRentARoom) {
              ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
            }
    case ReversePremiumsReceivedPage(RentalsRentARoom) =>
      taxYear =>
        userAnswers =>
          _ =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "income", RentalsRentARoom) {
              OtherPropertyRentalIncomeController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
            }
    case OtherIncomeFromPropertyPage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          _ =>
            controllers.rentalsandrentaroom.income.routes.RentalsAndRentARoomIncomeCheckYourAnswersController
              .onPageLoad(taxYear)

        // Rentals Rent A Room-Adjustments
    case PrivateUseAdjustmentPage(propertyType) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "adjustments", Rentals) {
              BalancingChargeController.onPageLoad(taxYear, NormalMode, propertyType)
            }
    case BalancingChargePage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "adjustments", Rentals) {
              if (userAnswers.get(ClaimPropertyIncomeAllowancePage(Rentals)).getOrElse(false)) {
                PropertyIncomeAllowanceController.onPageLoad(taxYear, NormalMode, Rentals)
              } else {
                RenovationAllowanceBalancingChargeController.onPageLoad(taxYear, NormalMode, Rentals)
              }

            }
    case BalancingChargePage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
            if (userAnswers.get(ClaimPropertyIncomeAllowancePage(RentalsRentARoom)).getOrElse(false)) {
              PropertyIncomeAllowanceController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
            } else {
              BusinessPremisesRenovationBalancingChargeController.onPageLoad(taxYear, NormalMode)
            }
    case PropertyIncomeAllowancePage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "adjustments", Rentals) {
              RenovationAllowanceBalancingChargeController.onPageLoad(taxYear, NormalMode, Rentals)
            }
    case PropertyIncomeAllowancePage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "adjustments", Rentals) {
              BusinessPremisesRenovationBalancingChargeController.onPageLoad(taxYear, NormalMode)
            }
    case RenovationAllowanceBalancingChargePage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "adjustments", Rentals) {
              ResidentialFinanceCostController.onPageLoad(taxYear, NormalMode, Rentals)
            }
    case ResidentialFinanceCostPage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "adjustments", Rentals) {
              UnusedResidentialFinanceCostController.onPageLoad(taxYear, NormalMode, Rentals)
            }
    case UnusedResidentialFinanceCostPage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "adjustments", Rentals) {
              UnusedLossesBroughtForwardController.onPageLoad(taxYear, NormalMode, Rentals)
            }
    case ResidentialFinanceCostPage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
            if (userAnswers.get(ClaimPropertyIncomeAllowancePage(RentalsRentARoom)).getOrElse(false)) {
              RentalsAndRentARoomAdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
            } else {
              diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "adjustments", Rentals) {
                UnusedResidentialFinanceCostController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
              }
            }
    case UnusedResidentialFinanceCostPage(Rentals) =>
      taxYear => _ => _ => UnusedLossesBroughtForwardController.onPageLoad(taxYear, NormalMode, Rentals)
    case UnusedResidentialFinanceCostPage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          _ =>
            UnusedLossesBroughtForwardController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
    case UnusedLossesBroughtForwardPage(propertyType) => taxYear => _ => userAnswers =>
      userAnswers.get(UnusedLossesBroughtForwardPage(propertyType)) match {
        case Some(UnusedLossesBroughtForward(true, _))  => WhenYouReportedTheLossController.onPageLoad(taxYear, NormalMode, propertyType)
        case Some(UnusedLossesBroughtForward(false, _)) =>
          propertyType match {
            case Rentals          => AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
            case RentalsRentARoom => RentalsAndRentARoomAdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
          }
      }
    case WhenYouReportedTheLossPage(propertyType) => taxYear => _ => _ =>
      propertyType match {
        case Rentals          => AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
        case RentalsRentARoom => RentalsAndRentARoomAdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
      }
    // Rentals-Expenses
    case ConsolidatedExpensesPage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
              consolidatedExpensesNavigation(taxYear, userAnswers, Rentals)
    case RentsRatesAndInsurancePage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
              RepairsAndMaintenanceCostsController.onPageLoad(taxYear, NormalMode, Rentals)
    case RepairsAndMaintenanceCostsPage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
              LoanInterestController.onPageLoad(taxYear, NormalMode, Rentals)
    case LoanInterestPage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
              OtherProfessionalFeesController.onPageLoad(taxYear, NormalMode, Rentals)
    case OtherProfessionalFeesPage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
              CostsOfServicesProvidedController.onPageLoad(taxYear, NormalMode, Rentals)

    case CostsOfServicesProvidedPage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
              PropertyBusinessTravelCostsController.onPageLoad(taxYear, NormalMode, Rentals)

    case PropertyBusinessTravelCostsPage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
              OtherAllowablePropertyExpensesController.onPageLoad(taxYear, NormalMode, Rentals)

        // Rentals-Allowances
    case CapitalAllowancesForACarPage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", Rentals) {
              AllowancesCheckYourAnswersController.onPageLoad(taxYear)
            }
    case AnnualInvestmentAllowancePage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", Rentals) {
              ZeroEmissionCarAllowanceController.onPageLoad(taxYear, NormalMode, Rentals)
            }
    case ZeroEmissionCarAllowancePage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", Rentals) {
              ZeroEmissionGoodsVehicleAllowanceController.onPageLoad(taxYear, NormalMode, Rentals)
            }
    case ZeroEmissionGoodsVehicleAllowancePage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", Rentals) {
              BusinessPremisesRenovationController.onPageLoad(taxYear, NormalMode, Rentals)
            }
    case BusinessPremisesRenovationPage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", RentalsRentARoom) {
              ReplacementOfDomesticGoodsController.onPageLoad(taxYear, NormalMode, Rentals)
            }
    case BusinessPremisesRenovationPage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", RentalsRentARoom) {
              ReplacementOfDomesticGoodsController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
            }

    case BusinessPremisesRenovationAllowanceBalancingChargePage =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", Rentals) {
              ResidentialFinanceCostController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
            }
    case ReplacementOfDomesticGoodsPage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", Rentals) {
              OtherCapitalAllowanceController.onPageLoad(taxYear, NormalMode, Rentals)
            }
    case OtherCapitalAllowancePage(Rentals) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", Rentals) {
              AllowancesCheckYourAnswersController.onPageLoad(taxYear)
            }
    case OtherAllowablePropertyExpensesPage(Rentals) =>
      taxYear =>
        _ =>
          _ => ExpensesCheckYourAnswersController.onPageLoad(taxYear)

        // Rentals and Rent a Room-Allowances
    case CapitalAllowancesForACarPage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", RentalsRentARoom) {
              controllers.rentalsandrentaroom.allowances.routes.RentalsAndRentARoomAllowancesCheckYourAnswersController
                .onPageLoad(taxYear)
            }
    case AnnualInvestmentAllowancePage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", RentalsRentARoom) {
              controllers.allowances.routes.ZeroEmissionCarAllowanceController
                .onPageLoad(taxYear, NormalMode, RentalsRentARoom)
            }
    case ZeroEmissionCarAllowancePage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", RentalsRentARoom) {
              (
                userAnswers.get(ClaimExpensesOrReliefPage(RentalsRentARoom)),
                userAnswers.get(ClaimPropertyIncomeAllowancePage(RentalsRentARoom))
              ) match {
                case (Some(ClaimExpensesOrRelief(false, None)), Some(true)) =>
                  ReplacementOfDomesticGoodsController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
                case _ =>
                  ZeroEmissionGoodsVehicleAllowanceController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
              }
            }
    case ZeroEmissionGoodsVehicleAllowancePage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", RentalsRentARoom) {
              BusinessPremisesRenovationController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
            }
    case ReplacementOfDomesticGoodsPage(RentalsRentARoom) =>
      taxYear =>
        userAnswers =>
          _ =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", RentalsRentARoom) {
              OtherCapitalAllowanceController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
            }
    case OtherCapitalAllowancePage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          _ =>
            controllers.rentalsandrentaroom.allowances.routes.RentalsAndRentARoomAllowancesCheckYourAnswersController
              .onPageLoad(taxYear)

        // Structured building allowance
    case ClaimStructureBuildingAllowancePage(propertyType) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "sba", Rentals) {
              structureBuildingAllowanceNavigation(taxYear, userAnswers, propertyType)
            }
    case StructureBuildingAllowancePage =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "sba", Rentals) {
              ClaimStructureBuildingAllowanceController.onPageLoad(taxYear, NormalMode, Rentals)
            }
    case SbaRemoveConfirmationPage(propertyType) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "sba", Rentals) {
              sbaRemoveConfirmationNavigationNormalMode(taxYear, userAnswers, propertyType)
            }

        // Enhanced structured building allowance
    case ClaimEsbaPage(propertyType) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "esba", Rentals) {
              enhancedStructureBuildingAllowanceNavigation(taxYear, userAnswers, propertyType)
            }
    case EsbaClaimsPage(propertyType) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "esba", Rentals) {
              esbaClaimsNavigationNormalMode(taxYear, userAnswers, propertyType)
            }
    case EsbaRemoveConfirmationPage(propertyType) =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "esba", Rentals) {
              esbaRemoveConfirmationNavigationNormalMode(taxYear, userAnswers, propertyType)
            }
    case EsbaSectionFinishedPage(propertyType) => taxYear => _ => _ => SummaryController.show(taxYear)
    case AboutSectionCompletePage =>
      taxYear => _ => _ => AboutSectionCompleteController.onPageLoad(taxYear)
    case TotalIncomeAmountPage(RentARoom) =>
      taxYear => _ => _ => ClaimExpensesOrReliefController.onPageLoad(taxYear, NormalMode, RentARoom)
    case AboutSectionCompletePage =>
      taxYear =>
        _ =>
          _ =>
            AboutSectionCompleteController.onPageLoad(taxYear)

        // Rent a Room
    case RentsRatesAndInsuranceRRPage =>
      taxYear =>
        _ =>
          userAnswers =>
              RepairsAndMaintenanceCostsRRController.onPageLoad(taxYear, NormalMode)

    case RaRCapitalAllowancesForACarPage =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", RentARoom) {
              RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)
            }

    case RaRElectricChargePointAllowanceForAnEVPage =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", RentARoom) {
              RaRZeroEmissionCarAllowanceController.onPageLoad(taxYear, NormalMode)
            }
    case RaRZeroEmissionCarAllowancePage =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", RentARoom) {
              RaRReplacementsOfDomesticGoodsController.onPageLoad(taxYear, NormalMode)
            }
    case RaRReplacementsOfDomesticGoodsPage =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", RentARoom) {
              RaROtherCapitalAllowancesController.onPageLoad(taxYear, NormalMode)
            }
    case RaROtherCapitalAllowancesPage =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "allowances", RentARoom) {
              RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)
            }
    case JointlyLetPage(RentARoom) =>
      taxYear =>
        userAnswers =>
          _ =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "about", RentARoom) {
              TotalIncomeAmountController.onPageLoad(taxYear, NormalMode, RentARoom)
            }
    case ClaimExpensesOrReliefPage(RentARoom) =>
      taxYear =>
        _ =>
          _ =>
            controllers.ukrentaroom.routes.CheckYourAnswersController.onPageLoad(taxYear)

        // RAR Adjustments
    case RaRBalancingChargePage =>
      taxYear =>
        _ =>
          userAnswers =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "adjustments", RentARoom) {
              RaRUnusedResidentialCostsController.onPageLoad(taxYear, NormalMode)
            }
    case RaRUnusedResidentialCostsPage =>
      taxYear => _ => _ => RaRUnusedLossesBroughtForwardController.onPageLoad(taxYear, NormalMode)
    case RaRUnusedLossesBroughtForwardPage =>
      taxYear => _ => userAnswers =>
        userAnswers.get(RaRUnusedLossesBroughtForwardPage) match {
          case Some(UnusedLossesBroughtForward(true, _)) => RarWhenYouReportedTheLossController.onPageLoad(taxYear, NormalMode)
          case _ => RaRAdjustmentsCYAController.onPageLoad(taxYear)
        }

    case RarWhenYouReportedTheLossPage =>
      taxYear => _ => _ => RaRAdjustmentsCYAController.onPageLoad(taxYear)
    case RaRAdjustmentsCompletePage     => taxYear => _ => _ => SummaryController.show(taxYear)
    case RentalsAdjustmentsCompletePage => taxYear => _ => _ => SummaryController.show(taxYear)
    case AboutPropertyCompletePage      => taxYear => _ => _ => SummaryController.show(taxYear)

    case AboutPropertyRentalsSectionFinishedPage =>
      taxYear =>
        _ =>
          _ =>
            SummaryController.show(taxYear)

        // Rentals and Rent a Room-About
    case JointlyLetPage(RentalsRentARoom) =>
      taxYear =>
        userAnswers =>
          _ =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "about", RentalsRentARoom) {
              TotalIncomeAmountController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
            }
    case TotalIncomeAmountPage(RentalsRentARoom) =>
      taxYear =>
        userAnswers =>
          _ =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "about", RentalsRentARoom) {
              ClaimExpensesOrReliefController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
            }
    case ClaimExpensesOrReliefPage(RentalsRentARoom) =>
      taxYear =>
        userAnswers =>
          _ =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "about", RentalsRentARoom) {
              PropertyRentalIncomeController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
            }
    case PropertyRentalIncomePage(RentalsRentARoom) =>
      taxYear =>
        userAnswers =>
          _ =>
            diversionService.redirectCallToCYAIfFinished(taxYear, userAnswers, "about", RentalsRentARoom) {
              ClaimPropertyIncomeAllowanceController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
            }
    case ClaimPropertyIncomeAllowancePage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          _ =>
            routes.RentalsAndRaRCheckYourAnswersController.onPageLoad(taxYear)

        // Rentals and Rent A Room-Expenses
    case ConsolidatedExpensesPage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
              consolidatedExpensesNavigation(taxYear, userAnswers, RentalsRentARoom)
    case RentsRatesAndInsurancePage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
              RepairsAndMaintenanceCostsController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
    case RepairsAndMaintenanceCostsPage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
              if (userAnswers.get(ClaimPropertyIncomeAllowancePage(RentalsRentARoom)).getOrElse(false)) {
                OtherProfessionalFeesController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
              } else {
                LoanInterestController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
              }
    case LoanInterestPage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
              OtherProfessionalFeesController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
    case OtherProfessionalFeesPage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
              CostsOfServicesProvidedController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
    case CostsOfServicesProvidedPage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
              if (userAnswers.get(ClaimPropertyIncomeAllowancePage(RentalsRentARoom)).getOrElse(false)) {
                OtherAllowablePropertyExpensesController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
              } else {
                PropertyBusinessTravelCostsController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
              }
    case PropertyBusinessTravelCostsPage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
              OtherAllowablePropertyExpensesController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
    case OtherAllowablePropertyExpensesPage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          userAnswers =>
              controllers.rentalsandrentaroom.expenses.routes.RentalsAndRaRExpensesCheckYourAnswersController
                .onPageLoad(taxYear)
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
      taxYear => _=>
          userAnswers => consolidatedExpensesRRNavigationCheckMode(taxYear, userAnswers)
    case LegalManagementOtherFeeRRPage => taxYear => _ => _ => ExpensesCheckYourAnswersRRController.onPageLoad(taxYear)
    case CostOfServicesProvidedRRPage  => taxYear => _ => _ => ExpensesCheckYourAnswersRRController.onPageLoad(taxYear)

    case RepairsAndMaintenanceCostsRRPage =>
      taxYear => _ => _ => ExpensesCheckYourAnswersRRController.onPageLoad(taxYear)
    case RentsRatesAndInsuranceRRPage => taxYear => _ => _ => ExpensesCheckYourAnswersRRController.onPageLoad(taxYear)

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
    case PropertyRentalIncomePage(Rentals) =>
      taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case premiumlease.PremiumForLeasePage(Rentals) =>
      taxYear =>
        previousUserAnswers =>
          userAnswers => premiumForLeaseNavigationCheckMode(taxYear, previousUserAnswers, userAnswers, Rentals)
    case CalculatedFigureYourselfPage(Rentals) =>
      taxYear =>
        previousUserAnswers =>
          userAnswers => calculatedFigureYourselfNavigationCheckMode(taxYear, previousUserAnswers, userAnswers, Rentals)
    case premiumlease.ReceivedGrantLeaseAmountPage(Rentals) =>
      taxYear => _ => _ => YearLeaseAmountController.onPageLoad(taxYear, CheckMode, Rentals)
    case premiumlease.YearLeaseAmountPage(Rentals) =>
      taxYear => _ => _ => PremiumsGrantLeaseController.onPageLoad(taxYear, CheckMode, Rentals)
    case premiumlease.PremiumsGrantLeasePage(Rentals) =>
      taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case ReversePremiumsReceivedPage(Rentals) =>
      taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case OtherIncomeFromPropertyPage(Rentals) =>
      taxYear =>
        _ =>
          _ =>
            PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)

        // Rentals and Rent A Room Income
    case IsNonUKLandlordPage(RentalsRentARoom) =>
      taxYear =>
        previousUserAnswers =>
          userAnswers => isNonUKLandlordNavigationCheckMode(taxYear, previousUserAnswers, userAnswers, RentalsRentARoom)
    case DeductingTaxPage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          _ =>
            controllers.rentalsandrentaroom.income.routes.RentalsAndRentARoomIncomeCheckYourAnswersController
              .onPageLoad(taxYear)
    case premiumlease.PremiumForLeasePage(RentalsRentARoom) =>
      taxYear =>
        previousUserAnswers =>
          userAnswers => premiumForLeaseNavigationCheckMode(taxYear, previousUserAnswers, userAnswers, RentalsRentARoom)
    case CalculatedFigureYourselfPage(RentalsRentARoom) =>
      taxYear =>
        previousUserAnswers =>
          userAnswers =>
            calculatedFigureYourselfNavigationCheckMode(taxYear, previousUserAnswers, userAnswers, RentalsRentARoom)
    case premiumlease.ReceivedGrantLeaseAmountPage(RentalsRentARoom) =>
      taxYear => _ => _ => YearLeaseAmountController.onPageLoad(taxYear, CheckMode, RentalsRentARoom)
    case premiumlease.YearLeaseAmountPage(RentalsRentARoom) =>
      taxYear => _ => _ => PremiumsGrantLeaseController.onPageLoad(taxYear, CheckMode, RentalsRentARoom)
    case premiumlease.PremiumsGrantLeasePage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          _ =>
            controllers.rentalsandrentaroom.income.routes.RentalsAndRentARoomIncomeCheckYourAnswersController
              .onPageLoad(taxYear)
    case ReversePremiumsReceivedPage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          _ =>
            controllers.rentalsandrentaroom.income.routes.RentalsAndRentARoomIncomeCheckYourAnswersController
              .onPageLoad(taxYear)
    case OtherIncomeFromPropertyPage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          _ =>
            controllers.rentalsandrentaroom.income.routes.RentalsAndRentARoomIncomeCheckYourAnswersController
              .onPageLoad(taxYear)
        // Rentals and Rent a Room Expenses
    case ConsolidatedExpensesPage(RentalsRentARoom) =>
      taxYear =>
        previousUserAnswers =>
          userAnswers =>
            consolidatedExpensesNavigationCheckMode(taxYear, previousUserAnswers, userAnswers, RentalsRentARoom)
    case RentsRatesAndInsurancePage(RentalsRentARoom) =>
      taxYear => _ => _ => RepairsAndMaintenanceCostsController.onPageLoad(taxYear, CheckMode, RentalsRentARoom)
    case RepairsAndMaintenanceCostsPage(RentalsRentARoom) =>
      taxYear => _ => _ => LoanInterestController.onPageLoad(taxYear, CheckMode, RentalsRentARoom)
    case LoanInterestPage(RentalsRentARoom) =>
      taxYear => _ => _ => OtherProfessionalFeesController.onPageLoad(taxYear, CheckMode, RentalsRentARoom)
    case OtherProfessionalFeesPage(RentalsRentARoom) =>
      taxYear => _ => _ => CostsOfServicesProvidedController.onPageLoad(taxYear, CheckMode, RentalsRentARoom)
    case CostsOfServicesProvidedPage(RentalsRentARoom) =>
      taxYear => _ => _ => PropertyBusinessTravelCostsController.onPageLoad(taxYear, CheckMode, RentalsRentARoom)
    case PropertyBusinessTravelCostsPage(RentalsRentARoom) =>
      taxYear => _ => _ => OtherAllowablePropertyExpensesController.onPageLoad(taxYear, CheckMode, RentalsRentARoom)
    case OtherAllowablePropertyExpensesPage(RentalsRentARoom) =>
      taxYear =>
        _ =>
          _ =>
            controllers.rentalsandrentaroom.expenses.routes.RentalsAndRaRExpensesCheckYourAnswersController
              .onPageLoad(taxYear)
        // Adjustments
    case UnusedLossesBroughtForwardPage(propertyType) =>
      taxYear => previousAnswers => userAnswers => UnusedLossesBroughtForwardPNavigationCheckMode(taxYear, propertyType, previousAnswers, userAnswers)
    case PrivateUseAdjustmentPage(Rentals) | PropertyIncomeAllowancePage(Rentals) |
        RenovationAllowanceBalancingChargePage(Rentals) | ResidentialFinanceCostPage(Rentals) |
        UnusedResidentialFinanceCostPage(Rentals) | WhenYouReportedTheLossPage(Rentals) =>
      taxYear =>
        _ =>
          _ =>
            AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
        // TODO add the correct property type here i.e. RentalsRentARoom
      case PrivateUseAdjustmentPage(RentalsRentARoom) | PropertyIncomeAllowancePage(RentalsRentARoom) |
        BusinessPremisesRenovationAllowanceBalancingChargePage | BalancingChargePage(RentalsRentARoom) |
        RenovationAllowanceBalancingChargePage(RentalsRentARoom) | ResidentialFinanceCostPage(RentalsRentARoom) |
        UnusedResidentialFinanceCostPage(RentalsRentARoom) | WhenYouReportedTheLossPage(RentalsRentARoom) =>
      taxYear => _ => _ => RentalsAndRentARoomAdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
    case BalancingChargePage(Rentals) =>
      taxYear =>
        previousUserAnswers =>
          userAnswers =>
            balancingChargeNavigationCheckMode(taxYear, previousUserAnswers, userAnswers, Rentals)
          // expenses
          //    case ConsolidatedExpensesPage => taxYear => _ => userAnswers => ExpensesCheckYourAnswersController.onPageLoad(taxYear)
        // Allowances
    case CapitalAllowancesForACarPage(Rentals) | AnnualInvestmentAllowancePage(Rentals) | ZeroEmissionCarAllowancePage(
          Rentals
        ) | ZeroEmissionGoodsVehicleAllowancePage(
          Rentals
        ) | BusinessPremisesRenovationPage(Rentals) | ReplacementOfDomesticGoodsPage(Rentals) |
        OtherCapitalAllowancePage(
          Rentals
        ) =>
      taxYear =>
        _ =>
          _ =>
            AllowancesCheckYourAnswersController.onPageLoad(taxYear)
        // Rentals and Rent A Room Allowances
    case CapitalAllowancesForACarPage(RentalsRentARoom) | AnnualInvestmentAllowancePage(RentalsRentARoom) |
        ZeroEmissionCarAllowancePage(RentalsRentARoom) | ZeroEmissionGoodsVehicleAllowancePage(
          RentalsRentARoom
        ) | BusinessPremisesRenovationPage(RentalsRentARoom) | ReplacementOfDomesticGoodsPage(RentalsRentARoom) |
        OtherCapitalAllowancePage(
          RentalsRentARoom
        ) =>
      taxYear =>
        _ =>
          _ =>
            controllers.rentalsandrentaroom.allowances.routes.RentalsAndRentARoomAllowancesCheckYourAnswersController
              .onPageLoad(taxYear)

        // Expenses
    case RentsRatesAndInsurancePage(Rentals) | RepairsAndMaintenanceCostsPage(Rentals) | LoanInterestPage(Rentals) |
        OtherProfessionalFeesPage(Rentals) | CostsOfServicesProvidedPage(Rentals) | PropertyBusinessTravelCostsPage(
          Rentals
        ) | OtherAllowablePropertyExpensesPage(Rentals) =>
      taxYear => _ => _ => ExpensesCheckYourAnswersController.onPageLoad(taxYear)
    case ConsolidatedExpensesPage(Rentals) =>
      taxYear =>
        previousUserAnswers =>
          userAnswers =>
            consolidatedExpensesNavigationCheckMode(taxYear, previousUserAnswers, userAnswers, Rentals)

        // Enhanced structured building allowance
    case EsbaQualifyingDatePage(index, propertyType) =>
      taxYear =>
        _ =>
          _ =>
            controllers.enhancedstructuresbuildingallowance.routes.EsbaQualifyingAmountController
              .onPageLoad(taxYear, index, CheckMode, propertyType)
    case EsbaQualifyingAmountPage(index, propertyType) =>
      taxYear =>
        _ =>
          _ =>
            controllers.enhancedstructuresbuildingallowance.routes.EsbaClaimController
              .onPageLoad(taxYear, CheckMode, index, propertyType)
    case EsbaClaimPage(index, propertyType) =>
      taxYear =>
        _ =>
          _ =>
            controllers.enhancedstructuresbuildingallowance.routes.EsbaAddressController
              .onPageLoad(taxYear, CheckMode, index, propertyType)
    case ClaimEsbaPage(propertyType) =>
      taxYear => _ => userAnswers => enhancedStructureBuildingAllowanceNavigation(taxYear, userAnswers, propertyType)
    case ClaimStructureBuildingAllowancePage(propertyType) =>
      taxYear => _ => userAnswers => structureBuildingAllowanceNavigation(taxYear, userAnswers, propertyType)

    case TotalIncomeAmountPage(RentARoom) =>
      taxYear =>
        previousUserAnswers =>
          userAnswers => totalIncomeForRaRNavigationCheckMode(taxYear, previousUserAnswers, userAnswers)
    case JointlyLetPage(RentARoom) =>
      taxYear =>
        previousUserAnswers => userAnswers => jointlyLetCheckModeNavigation(taxYear, previousUserAnswers, userAnswers)

    case ClaimExpensesOrReliefPage(RentARoom) =>
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
      taxYear => _ => _ => RaRAdjustmentsCYAController.onPageLoad(taxYear)
    case RaRUnusedLossesBroughtForwardPage =>
      taxYear => previousUserAnswers => userAnswers => UnusedLossesBroughtForwardPNavigationCheckMode(taxYear, RentARoom, previousUserAnswers, userAnswers)
    case RarWhenYouReportedTheLossPage =>
      taxYear => _ => _ => RaRAdjustmentsCYAController.onPageLoad(taxYear)
        // Rentals and Rent a Room
    case JointlyLetPage(RentalsRentARoom) =>
      taxYear => _ => _ => routes.RentalsAndRaRCheckYourAnswersController.onPageLoad(taxYear)
    case TotalIncomeAmountPage(RentalsRentARoom) =>
      taxYear => _ => _ => routes.RentalsAndRaRCheckYourAnswersController.onPageLoad(taxYear)
    case ClaimExpensesOrReliefPage(RentalsRentARoom) =>
      taxYear => _ => _ => ClaimPropertyIncomeAllowanceController.onPageLoad(taxYear, CheckMode, RentalsRentARoom)
    case PropertyRentalIncomePage(RentalsRentARoom) =>
      taxYear =>_ => _ => routes.RentalsAndRaRCheckYourAnswersController.onPageLoad(taxYear)
    case ClaimPropertyIncomeAllowancePage(RentalsRentARoom) =>
      taxYear => _ => _ => routes.RentalsAndRaRCheckYourAnswersController.onPageLoad(taxYear)

    case _ =>
      taxYear => _ => _ => controllers.about.routes.CheckYourAnswersController.onPageLoad(taxYear)

  }

  def nextPage(page: Page, taxYear: Int, mode: Mode, previousUserAnswers: UserAnswers, userAnswers: UserAnswers): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(taxYear)(previousUserAnswers)(userAnswers)
      case CheckMode =>
        checkRouteMap(page)(taxYear)(previousUserAnswers)(userAnswers)
    }

  def sbaNextPage(
    page: Page,
    taxYear: Int,
    mode: Mode,
    index: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers
  ): Call = mode match {
    case NormalMode =>
      sbaNormalRoutes(page, taxYear, index)
    case CheckMode =>
      sbaCheckModeRoutes(page, taxYear, index)
  }

  private def sbaNormalRoutes(
    page: Page,
    taxYear: Int,
    index: Int
  ): Call = page match {
    case StructureBuildingQualifyingDatePage(_, propertyType) =>
      StructureBuildingQualifyingAmountController.onPageLoad(taxYear, NormalMode, index, propertyType)
    case StructureBuildingQualifyingAmountPage(_, propertyType) =>
      StructureBuildingAllowanceClaimController.onPageLoad(taxYear, NormalMode, index, propertyType)
    case StructureBuildingAllowanceClaimPage(_, propertyType) =>
      StructuredBuildingAllowanceAddressController.onPageLoad(taxYear, NormalMode, index, propertyType)
    case StructuredBuildingAllowanceAddressPage(_, propertyType) =>
      SbaCheckYourAnswersController.onPageLoad(taxYear, index, propertyType)
    case _ => IndexController.onPageLoad
  }

  private def sbaCheckModeRoutes(
    page: Page,
    taxYear: Int,
    index: Int
  ): Call = page match {
    case StructureBuildingQualifyingDatePage(_, propertyType) =>
      SbaCheckYourAnswersController.onPageLoad(taxYear, index, propertyType)
    case StructureBuildingQualifyingAmountPage(_, propertyType) =>
      SbaCheckYourAnswersController.onPageLoad(taxYear, index, propertyType)
    case StructureBuildingAllowanceClaimPage(_, propertyType) =>
      SbaCheckYourAnswersController.onPageLoad(taxYear, index, propertyType)
    case StructuredBuildingAllowanceAddressPage(_, propertyType) =>
      SbaCheckYourAnswersController.onPageLoad(taxYear, index, propertyType)
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
    case EsbaQualifyingDatePage(_, propertyType) =>
      EsbaQualifyingAmountController.onPageLoad(taxYear, index, NormalMode, propertyType)
    case EsbaQualifyingAmountPage(index, propertyType) =>
      EsbaClaimController.onPageLoad(taxYear, NormalMode, index, propertyType)
    case EsbaClaimPage(_, propertyType)   => EsbaAddressController.onPageLoad(taxYear, NormalMode, index, propertyType)
    case EsbaAddressPage(_, propertyType) => EsbaCheckYourAnswersController.onPageLoad(taxYear, index, propertyType)
    case _                                => IndexController.onPageLoad
  }

  private def esbaCheckModeRoutes(
    page: Page,
    taxYear: Int,
    index: Int
  ): Call = page match {
    case EsbaQualifyingDatePage(_, propertyType) =>
      EsbaCheckYourAnswersController.onPageLoad(taxYear, index, propertyType)
    case EsbaQualifyingAmountPage(_, propertyType) =>
      EsbaCheckYourAnswersController.onPageLoad(taxYear, index, propertyType)
    case EsbaClaimPage(_, propertyType) =>
      EsbaCheckYourAnswersController.onPageLoad(taxYear, index, propertyType)
    case EsbaAddressPage(_, propertyType) =>
      EsbaCheckYourAnswersController.onPageLoad(taxYear, index, propertyType)
    case _ => IndexController.onPageLoad
  }

  private def isNonUKLandlordNavigation(taxYear: Int, userAnswers: UserAnswers, propertyType: PropertyType): Call =
    userAnswers.get(IsNonUKLandlordPage(propertyType)) match {
      case Some(true)                   => DeductingTaxController.onPageLoad(taxYear, NormalMode, propertyType)
      case _ if propertyType == Rentals => PropertyRentalIncomeController.onPageLoad(taxYear, NormalMode, propertyType)
      case _ if propertyType == RentalsRentARoom =>
        PremiumForLeaseController.onPageLoad(taxYear, NormalMode, propertyType)
    }

  private def isNonUKLandlordNavigationCheckMode(
    taxYear: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers,
    propertyType: PropertyType
  ): Call =
    userAnswers.get(IsNonUKLandlordPage(propertyType)) match {
      case Some(true) if !previousUserAnswers.get(IsNonUKLandlordPage(propertyType)).getOrElse(false) =>
        DeductingTaxController.onPageLoad(taxYear, CheckMode, propertyType)
      case _ if propertyType == Rentals => toIncomeCYA(propertyType, taxYear)
      case _ if propertyType == RentalsRentARoom =>
        PremiumForLeaseController.onPageLoad(taxYear, CheckMode, propertyType)
    }

  private def premiumForLeaseNavigation(taxYear: Int, userAnswers: UserAnswers, propertyType: PropertyType): Call =
    userAnswers.get(PremiumForLeasePage(propertyType)) match {
      case Some(true) => CalculatedFigureYourselfController.onPageLoad(taxYear, NormalMode, propertyType)
      case _          => ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode, propertyType)
    }

  private def premiumForLeaseNavigationCheckMode(
    taxYear: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers,
    propertyType: PropertyType
  ): Call =
    userAnswers.get(PremiumForLeasePage(propertyType)) match {
      case Some(true) if !previousUserAnswers.get(PremiumForLeasePage(propertyType)).getOrElse(false) =>
        CalculatedFigureYourselfController.onPageLoad(taxYear, CheckMode, propertyType)
      case _ =>
        toIncomeCYA(propertyType, taxYear)

    }

  private def calculatedFigureYourselfNavigation(
    taxYear: Int,
    userAnswers: UserAnswers,
    propertyType: PropertyType
  ): Call =
    userAnswers.get(CalculatedFigureYourselfPage(propertyType)) match {
      case Some(CalculatedFigureYourself(true, _)) =>
        ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode, propertyType)
      case Some(CalculatedFigureYourself(false, _)) =>
        ReceivedGrantLeaseAmountController.onPageLoad(taxYear, NormalMode, propertyType)
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
        ReceivedGrantLeaseAmountController.onPageLoad(taxYear, CheckMode, propertyType)
      case _ =>
        toIncomeCYA(propertyType, taxYear)
    }
  private def toIncomeCYA(propertyType: PropertyType, taxYear: Int) =
    propertyType match {
      case Rentals => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
      case RentalsRentARoom =>
        controllers.rentalsandrentaroom.income.routes.RentalsAndRentARoomIncomeCheckYourAnswersController
          .onPageLoad(taxYear)
    }
  private def consolidatedExpensesNavigation(taxYear: Int, userAnswers: UserAnswers, propertyType: PropertyType): Call =
    userAnswers.get(ConsolidatedExpensesPage(propertyType)) match {
      case Some(ConsolidatedExpenses(true, _)) =>
        propertyType match {
          case Rentals => ExpensesCheckYourAnswersController.onPageLoad(taxYear)
          case RentalsRentARoom =>
            controllers.rentalsandrentaroom.expenses.routes.RentalsAndRaRExpensesCheckYourAnswersController
              .onPageLoad(taxYear)
        }
      case Some(ConsolidatedExpenses(false, _)) =>
        RentsRatesAndInsuranceController.onPageLoad(taxYear, NormalMode, propertyType)
    }

  private def consolidatedExpensesRRNavigationCheckMode(
    taxYear: Int,
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(ConsolidatedExpensesRRPage) match {
      case Some(ConsolidatedRRExpenses(false, _)) =>
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
        RentsRatesAndInsuranceController.onPageLoad(taxYear, NormalMode, propertyType)
      case _ =>
        propertyType match {
          case Rentals => ExpensesCheckYourAnswersController.onPageLoad(taxYear)
          case RentalsRentARoom =>
            controllers.rentalsandrentaroom.expenses.routes.RentalsAndRaRExpensesCheckYourAnswersController
              .onPageLoad(taxYear)
        }
    }

  private def balancingChargeNavigationCheckMode(
    taxYear: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers,
    propertyType: PropertyType
  ): Call =
    (
      userAnswers.get(BalancingChargePage(propertyType)),
      previousUserAnswers.get(BalancingChargePage(propertyType)),
      userAnswers.get(ClaimPropertyIncomeAllowancePage(Rentals))
    ) match {
      case (Some(current), Some(previous),Some(true))
        if current.balancingChargeYesNo != previous.balancingChargeYesNo &&
          current.balancingChargeAmount != previous.balancingChargeAmount =>
        PropertyIncomeAllowanceController.onPageLoad(taxYear, CheckMode, propertyType)

      case _ => AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
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

  private def structureBuildingAllowanceNavigation(
    taxYear: Int,
    userAnswers: UserAnswers,
    propertyType: PropertyType
  ): Call =
    userAnswers.get(ClaimStructureBuildingAllowancePage(propertyType)) match {
      case Some(true)  => AddClaimStructureBuildingAllowanceController.onPageLoad(taxYear, propertyType)
      case Some(false) => ClaimSbaCheckYourAnswersController.onPageLoad(taxYear, propertyType)
      case _           => SummaryController.show(taxYear)
    }

  private def enhancedStructureBuildingAllowanceNavigation(
    taxYear: Int,
    userAnswers: UserAnswers,
    propertyType: PropertyType
  ): Call =
    userAnswers.get(ClaimEsbaPage(propertyType)) match {
      case Some(true)  => EsbaAddClaimController.onPageLoad(taxYear, propertyType)
      case Some(false) => ClaimEsbaCheckYourAnswersController.onPageLoad(taxYear, propertyType)
      case _           => SummaryController.show(taxYear)
    }

  private def sbaRemoveConfirmationNavigationNormalMode(
    taxYear: Int,
    userAnswers: UserAnswers,
    propertyType: PropertyType
  ): Call =
    (
      userAnswers.get(SbaRemoveConfirmationPage(propertyType)),
      userAnswers.get(StructureBuildingAllowanceGroup(propertyType))
    ) match {
      case (Some(true), Some(sbaForm)) if sbaForm.isEmpty =>
        AddClaimStructureBuildingAllowanceController.onPageLoad(taxYear, propertyType)
      case (_, Some(sbaForm)) if sbaForm.nonEmpty => SbaClaimsController.onPageLoad(taxYear, propertyType)
      case (_, _)                                 => SummaryController.show(taxYear)
    }

  private def esbaClaimsNavigationNormalMode(taxYear: Int, userAnswers: UserAnswers, propertyType: PropertyType): Call =
    userAnswers.get(EsbaClaimsPage(propertyType)) match {
      case Some(true)  => EsbaAddClaimController.onPageLoad(taxYear, propertyType)
      case Some(false) => EsbaSectionFinishedController.onPageLoad(taxYear, propertyType)
      case _           => SummaryController.show(taxYear)
    }

  private def esbaRemoveConfirmationNavigationNormalMode(
    taxYear: Int,
    userAnswers: UserAnswers,
    propertyType: PropertyType
  ): Call =
    (
      userAnswers.get(EsbaRemoveConfirmationPage(propertyType)),
      userAnswers.get(EnhancedStructureBuildingAllowanceGroup(propertyType))
    ) match {
      case (Some(true), Some(esbaForm)) if esbaForm.isEmpty => EsbaAddClaimController.onPageLoad(taxYear, propertyType)
      case (_, Some(esbaForm)) if esbaForm.nonEmpty         => EsbaClaimsController.onPageLoad(taxYear, propertyType)
    }

  private def jointlyLetCheckModeNavigation(
    taxYear: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers
  ): Call =
    (previousUserAnswers.get(JointlyLetPage(RentARoom)), userAnswers.get(JointlyLetPage(RentARoom))) match {
      case (Some(true), Some(false)) | (Some(false), Some(true)) =>
        TotalIncomeAmountController.onPageLoad(taxYear, NormalMode, RentARoom)
      case _ => controllers.ukrentaroom.routes.CheckYourAnswersController.onPageLoad(taxYear)
    }

  private def totalIncomeForRaRNavigationCheckMode(
    taxYear: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers
  ): Call =
    (
      previousUserAnswers.get(TotalIncomeAmountPage(RentARoom)),
      userAnswers.get(TotalIncomeAmountPage(RentARoom))
    ) match {
      case (Some(pre), Some(cur)) if pre != cur =>
        ClaimExpensesOrReliefController.onPageLoad(taxYear, NormalMode, RentARoom)
      case _ => controllers.ukrentaroom.routes.CheckYourAnswersController.onPageLoad(taxYear)
    }

  private def UnusedLossesBroughtForwardPNavigationCheckMode(
       taxYear: Int,
       propertyType: PropertyType,
       previousAnswers: UserAnswers,
       userAnswers: UserAnswers
     ): Call =
    (
      previousAnswers.get(UnusedLossesBroughtForwardPage(propertyType)),
      userAnswers.get(UnusedLossesBroughtForwardPage(propertyType))
    ) match {
      case (Some(UnusedLossesBroughtForward(false, _)), Some(UnusedLossesBroughtForward(true, _))) =>
        WhenYouReportedTheLossController.onPageLoad(taxYear, NormalMode, propertyType)
      case _ => propertyType match {
        case Rentals => AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
        case RentARoom => RaRAdjustmentsCYAController.onPageLoad(taxYear)
        case RentalsRentARoom => RentalsAndRentARoomAdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
      }
    }
}

