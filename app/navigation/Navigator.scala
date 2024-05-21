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
import controllers.furnishedholidaylettings.income.routes._
import controllers.premiumlease.routes._
import controllers.propertyrentals.expenses.routes._
import controllers.propertyrentals.income.routes._
import controllers.propertyrentals.routes._
import controllers.routes._
import controllers.structuresbuildingallowance.routes._
import controllers.ukrentaroom.routes._
import models.TotalIncome.{Between, Over, Under}
import models._
import pages._
import pages.adjustments._
import pages.allowances._
import pages.enhancedstructuresbuildingallowance._
import pages.furnishedholidaylettings._
import pages.furnishedholidaylettings.income.{FhlDeductingTaxPage, FhlIsNonUKLandlordPage}
import pages.premiumlease.{CalculatedFigureYourselfPage, LeasePremiumPaymentPage}
import pages.propertyrentals.expenses._
import pages.propertyrentals.income._
import pages.structurebuildingallowance._
import pages.ukrentaroom.{AboutSectionCompletePage, ClaimExpensesOrRRRPage, TotalIncomeAmountPage, UkRentARoomJointlyLetPage}
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => Int => UserAnswers => UserAnswers => Call = {
    case UKPropertyDetailsPage => taxYear => _ => _ => TotalIncomeController.onPageLoad(taxYear, NormalMode)
    case TotalIncomePage       => taxYear => _ => userAnswers => totalIncomeNavigationNormalMode(taxYear, userAnswers)
    case UKPropertySelectPage  => taxYear => _ => _ => SummaryController.show(taxYear)
    case ReportPropertyIncomePage =>
      taxYear => _ => userAnswers => reportIncomeNavigationNormalMode(taxYear, userAnswers)
    case UKPropertyPage => taxYear => _ => _ => controllers.about.routes.CheckYourAnswersController.onPageLoad(taxYear)
    case propertyrentals.ExpensesLessThan1000Page =>
      taxYear => _ => _ => ClaimPropertyIncomeAllowanceController.onPageLoad(taxYear, NormalMode)
    case propertyrentals.ClaimPropertyIncomeAllowancePage =>
      taxYear =>
        _ =>
          _ =>
            PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
        // property income
    case IsNonUKLandlordPage => taxYear => _ => userAnswers => isNonUKLandlordNavigation(taxYear, userAnswers)
    case DeductingTaxPage    => taxYear => _ => _ => IncomeFromPropertyRentalsController.onPageLoad(taxYear, NormalMode)
    case IncomeFromPropertyRentalsPage =>
      taxYear => _ => _ => LeasePremiumPaymentController.onPageLoad(taxYear, NormalMode)
    case premiumlease.LeasePremiumPaymentPage =>
      taxYear => _ => userAnswers => leasePremiumPaymentNavigation(taxYear, userAnswers)
    case CalculatedFigureYourselfPage =>
      taxYear => _ => userAnswers => calculatedFigureYourselfNavigation(taxYear, userAnswers)
    case premiumlease.ReceivedGrantLeaseAmountPage =>
      taxYear => _ => _ => YearLeaseAmountController.onPageLoad(taxYear, NormalMode)
    case premiumlease.YearLeaseAmountPage =>
      taxYear => _ => _ => PremiumsGrantLeaseController.onPageLoad(taxYear, NormalMode)
    case premiumlease.PremiumsGrantLeasePage =>
      taxYear => _ => _ => ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode)
    case ReversePremiumsReceivedPage =>
      taxYear => _ => _ => OtherIncomeFromPropertyController.onPageLoad(taxYear, NormalMode)
    case OtherIncomeFromPropertyPage =>
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
    case ConsolidatedExpensesPage(_)  => taxYear => _ => userAnswers => consolidatedExpensesNavigation(taxYear, userAnswers)
    case RentsRatesAndInsurancePage(_) =>
      taxYear => _ => _ => RepairsAndMaintenanceCostsController.onPageLoad(taxYear, NormalMode)
    case RepairsAndMaintenanceCostsPage => taxYear => _ => _ => LoanInterestController.onPageLoad(taxYear, NormalMode)
    case LoanInterestPage => taxYear => _ => _ => OtherProfessionalFeesController.onPageLoad(taxYear, NormalMode)
    case OtherProfessionalFeesPage =>
      taxYear => _ => _ => CostsOfServicesProvidedController.onPageLoad(taxYear, NormalMode)
    case CostsOfServicesProvidedPage =>
      taxYear => _ => _ => PropertyBusinessTravelCostsController.onPageLoad(taxYear, NormalMode)
    case PropertyBusinessTravelCostsPage =>
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
      taxYear => _ => _ => ZeroEmissionCarAllowanceController.onPageLoad(taxYear, NormalMode)
    case ZeroEmissionCarAllowancePage =>
      taxYear => _ => _ => ZeroEmissionGoodsVehicleAllowanceController.onPageLoad(taxYear, NormalMode)
    case ZeroEmissionGoodsVehicleAllowancePage =>
      taxYear => _ => _ => BusinessPremisesRenovationController.onPageLoad(taxYear, NormalMode)
    case BusinessPremisesRenovationPage =>
      taxYear => _ => _ => ReplacementOfDomesticGoodsController.onPageLoad(taxYear, NormalMode)
    case ReplacementOfDomesticGoodsPage =>
      taxYear => _ => _ => OtherCapitalAllowanceController.onPageLoad(taxYear, NormalMode)
    case OtherCapitalAllowancePage => taxYear => _ => _ => AllowancesCheckYourAnswersController.onPageLoad(taxYear)
    case OtherAllowablePropertyExpensesPage =>
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
      taxYear =>
        _ =>
          userAnswers =>
            sbaRemoveConfirmationNavigationNormalMode(taxYear, userAnswers)

        // Enhanced structured building allowance
    case ClaimEsbaPage =>
      taxYear => _ => userAnswers => enhancedStructureBuildingAllowanceNavigationNormalMode(taxYear, userAnswers)
    case EsbaClaimsPage => taxYear => _ => userAnswers => esbaClaimsNavigationNormalMode(taxYear, userAnswers)
    case EsbaRemoveConfirmationPage =>
      taxYear =>
        _ =>
          userAnswers =>
            esbaRemoveConfirmationNavigationNormalMode(taxYear, userAnswers)

        // Furnished Holiday Lettings
    case FhlMoreThanOnePage =>
      taxYear =>
        _ => _ => controllers.furnishedholidaylettings.routes.FhlMainHomeController.onPageLoad(taxYear, NormalMode)
    case FhlMainHomePage => taxYear => _ => userAnswers => flaYourMainHomeNextPage(taxYear, NormalMode, userAnswers)
    case FhlJointlyLetPage =>
      taxYear =>
        _ =>
          _ => controllers.furnishedholidaylettings.routes.FhlReliefOrExpensesController.onPageLoad(taxYear, NormalMode)

    case FhlIsNonUKLandlordPage => taxYear => _ => userAnswers => isFhlNonUKLandlordNavigation(taxYear, userAnswers)
    case FhlDeductingTaxPage    => taxYear => _ => _ => FhlIncomeController.onPageLoad(taxYear, NormalMode)
    case FhlIncomePage          => taxYear => _ => _ => FhlIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case TotalIncomeAmountPage  => taxYear => _ => _ => ClaimExpensesOrRRRController.onPageLoad(taxYear, NormalMode)

    case AboutSectionCompletePage =>
      taxYear => _ => _ => AboutSectionCompleteController.onPageLoad(taxYear)
    case UkRentARoomJointlyLetPage => taxYear => _ => _ => TotalIncomeAmountController.onPageLoad(taxYear, NormalMode)
    case ClaimExpensesOrRRRPage =>
      taxYear => _ => _ => controllers.ukrentaroom.routes.CheckYourAnswersController.onPageLoad(taxYear)

    case _ => _ => _ => _ => IndexController.onPageLoad

  }

  private val checkRouteMap: Page => Int => UserAnswers => UserAnswers => Call = {
    case propertyrentals.ExpensesLessThan1000Page =>
      taxYear => _ => _ => PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
    case propertyrentals.ClaimPropertyIncomeAllowancePage =>
      taxYear => _ => _ => PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
    case TotalIncomePage =>
      taxYear =>
        previousUserAnswers =>
          userAnswers =>
            totalIncomeNavigationCheckMode(taxYear, previousUserAnswers, userAnswers)
        // property income
    case IsNonUKLandlordPage =>
      taxYear =>
        previousUserAnswers =>
          userAnswers => isNonUKLandlordNavigationCheckMode(taxYear, previousUserAnswers, userAnswers)
    case DeductingTaxPage => taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case IncomeFromPropertyRentalsPage =>
      taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case premiumlease.LeasePremiumPaymentPage =>
      taxYear =>
        previousUserAnswers =>
          userAnswers => leasePremiumPaymentNavigationCheckMode(taxYear, previousUserAnswers, userAnswers)
    case CalculatedFigureYourselfPage =>
      taxYear =>
        previousUserAnswers =>
          userAnswers => calculatedFigureYourselfNavigationCheckMode(taxYear, previousUserAnswers, userAnswers)
    case premiumlease.ReceivedGrantLeaseAmountPage =>
      taxYear => _ => _ => YearLeaseAmountController.onPageLoad(taxYear, CheckMode)
    case premiumlease.YearLeaseAmountPage =>
      taxYear => _ => _ => PremiumsGrantLeaseController.onPageLoad(taxYear, CheckMode)
    case premiumlease.PremiumsGrantLeasePage =>
      taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case ReversePremiumsReceivedPage =>
      taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case OtherIncomeFromPropertyPage =>
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
    case RentsRatesAndInsurancePage(_) | RepairsAndMaintenanceCostsPage | LoanInterestPage | OtherProfessionalFeesPage |
        CostsOfServicesProvidedPage | PropertyBusinessTravelCostsPage | OtherAllowablePropertyExpensesPage =>
      taxYear => _ => userAnswers => ExpensesCheckYourAnswersController.onPageLoad(taxYear)
    case ConsolidatedExpensesPage(_)  =>
      taxYear =>
        previousUserAnswers =>
          userAnswers =>
            consolidatedExpensesNavigationCheckMode(taxYear, previousUserAnswers, userAnswers)

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

        // Furnished Holiday Lettings
    case FhlMoreThanOnePage =>
      taxYear =>
        _ => _ => controllers.furnishedholidaylettings.routes.FhlMainHomeController.onPageLoad(taxYear, CheckMode)
    case FhlMainHomePage => taxYear => _ => userAnswers => flaYourMainHomeNextPage(taxYear, CheckMode, userAnswers)
    case FhlJointlyLetPage =>
      taxYear =>
        _ =>
          _ => controllers.furnishedholidaylettings.routes.FhlReliefOrExpensesController.onPageLoad(taxYear, CheckMode)

    case FhlIsNonUKLandlordPage =>
      taxYear =>
        previousUserAnswers =>
          userAnswers => isFhlNonUKLandlordNavigationCheckMode(taxYear, previousUserAnswers, userAnswers)

    case TotalIncomeAmountPage =>
      taxYear => _ => _ => controllers.ukrentaroom.routes.CheckYourAnswersController.onPageLoad(taxYear)
    case UkRentARoomJointlyLetPage =>
      taxYear => _ => _ => controllers.ukrentaroom.routes.CheckYourAnswersController.onPageLoad(taxYear)

    case ClaimExpensesOrRRRPage =>
      taxYear => _ => _ => controllers.ukrentaroom.routes.CheckYourAnswersController.onPageLoad(taxYear)
    case _ => taxYear => _ => userAnswers => controllers.about.routes.CheckYourAnswersController.onPageLoad(taxYear)
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
      structureBuildingNormalRoutes(page, taxYear, mode, index, previousUserAnswers, userAnswers)
    case CheckMode =>
      structureBuildingCheckModeRoutes(page, taxYear, mode, index, previousUserAnswers, userAnswers)
  }

  private def flaYourMainHomeNextPage(taxYear: Int, mode: Mode, userAnswers: UserAnswers): Call =
    userAnswers.get(FhlMainHomePage) match {
      case Some(true) => controllers.furnishedholidaylettings.routes.FhlJointlyLetController.onPageLoad(taxYear, mode)
      case None       => IndexController.onPageLoad
    }

  private def structureBuildingNormalRoutes(
    page: Page,
    taxYear: Int,
    mode: Mode,
    index: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers
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
    mode: Mode,
    index: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers
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
      esbaNormalRoutes(page, taxYear, mode, index, previousUserAnswers, userAnswers)
    case CheckMode =>
      esbaCheckModeRoutes(page, taxYear, mode, index, previousUserAnswers, userAnswers)
  }
  private def esbaNormalRoutes(
    page: Page,
    taxYear: Int,
    mode: Mode,
    index: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers
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
    mode: Mode,
    index: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers
  ): Call = page match {
    case EsbaQualifyingDatePage(_) | EsbaQualifyingAmountPage(_) | EsbaClaimAmountPage(_) | EsbaAddressPage(_) =>
      EsbaCheckYourAnswersController.onPageLoad(taxYear, index)
    case _ => IndexController.onPageLoad
  }

  private def isNonUKLandlordNavigation(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(IsNonUKLandlordPage) match {
      case Some(true) => DeductingTaxController.onPageLoad(taxYear, NormalMode)
      case _          => IncomeFromPropertyRentalsController.onPageLoad(taxYear, NormalMode)
    }

  private def isNonUKLandlordNavigationCheckMode(
    taxYear: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(IsNonUKLandlordPage) match {
      case Some(true) if !previousUserAnswers.get(IsNonUKLandlordPage).getOrElse(false) =>
        DeductingTaxController.onPageLoad(taxYear, CheckMode)
      case _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    }

  private def leasePremiumPaymentNavigation(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(LeasePremiumPaymentPage) match {
      case Some(true) => CalculatedFigureYourselfController.onPageLoad(taxYear, NormalMode)
      case _          => ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode)
    }

  private def leasePremiumPaymentNavigationCheckMode(
    taxYear: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(LeasePremiumPaymentPage) match {
      case Some(true) if !previousUserAnswers.get(LeasePremiumPaymentPage).getOrElse(false) =>
        CalculatedFigureYourselfController.onPageLoad(taxYear, CheckMode)
      case _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    }

  private def calculatedFigureYourselfNavigation(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(CalculatedFigureYourselfPage) match {
      case Some(CalculatedFigureYourself(true, _)) => ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode)
      case Some(CalculatedFigureYourself(false, _)) =>
        ReceivedGrantLeaseAmountController.onPageLoad(taxYear, NormalMode)
    }

  private def calculatedFigureYourselfNavigationCheckMode(
    taxYear: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(CalculatedFigureYourselfPage) match {
      case Some(CalculatedFigureYourself(false, _))
          if previousUserAnswers.get(CalculatedFigureYourselfPage).map(_.calculatedFigureYourself).getOrElse(true) =>
        ReceivedGrantLeaseAmountController.onPageLoad(taxYear, CheckMode)
      case _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    }

  private def consolidatedExpensesNavigation(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(ConsolidatedExpensesPage(PageConstants.propertyRentalsExpense) ) match {
      case Some(ConsolidatedExpenses(true, _))  => ExpensesCheckYourAnswersController.onPageLoad(taxYear)
      case Some(ConsolidatedExpenses(false, _)) => RentsRatesAndInsuranceController.onPageLoad(taxYear, NormalMode, Rentals)
    }

  private def consolidatedExpensesNavigationCheckMode(
    taxYear: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(ConsolidatedExpensesPage(PageConstants.propertyRentalsExpense)) match {
      case Some(ConsolidatedExpenses(false, _))
        if previousUserAnswers.get(ConsolidatedExpensesPage(PageConstants.propertyRentalsExpense)).forall(_.consolidatedExpensesYesOrNo) =>
        RentsRatesAndInsuranceController.onPageLoad(taxYear, NormalMode, Rentals)
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
      case _          => SummaryController.show(taxYear)
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

  private def isFhlNonUKLandlordNavigation(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(FhlIsNonUKLandlordPage) match {
      case Some(true) => FhlDeductingTaxController.onPageLoad(taxYear, NormalMode)
      case _          => FhlIncomeController.onPageLoad(taxYear, NormalMode)
    }

  private def isFhlNonUKLandlordNavigationCheckMode(
    taxYear: Int,
    previousUserAnswers: UserAnswers,
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(FhlIsNonUKLandlordPage) match {
      case Some(true) if !previousUserAnswers.get(FhlIsNonUKLandlordPage).getOrElse(false) =>
        FhlDeductingTaxController.onPageLoad(taxYear, CheckMode)
      case _ => FhlIncomeCheckYourAnswersController.onPageLoad(taxYear)
    }

}
