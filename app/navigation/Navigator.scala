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

import controllers.adjustments.routes._
import controllers.allowances.routes._
import controllers.premiumlease.routes._
import controllers.propertyrentals.expenses.routes._
import controllers.propertyrentals.routes._
import controllers.routes._
import models.TotalIncome.{Between, Over, Under}
import models._
import pages._
import pages.adjustments._
import pages.allowances._
import pages.premiumlease.LeasePremiumPaymentPage
import pages.propertyrentals.IsNonUKLandlordPage
import pages.propertyrentals.expenses._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => Int => UserAnswers => UserAnswers => Call = {
    case UKPropertyDetailsPage => taxYear => _ => _ => TotalIncomeController.onPageLoad(taxYear, NormalMode)
    case TotalIncomePage => taxYear => _ => userAnswers => totalIncomeNavigationNormalMode(taxYear, userAnswers)
    case UKPropertySelectPage => taxYear => _ => _ => SummaryController.show(taxYear)
    case ReportPropertyIncomePage => taxYear => _ => userAnswers => reportIncomeNavigationNormalMode(taxYear, userAnswers)
    case UKPropertyPage => taxYear => _ => _ => CheckYourAnswersController.onPageLoad(taxYear)
    case propertyrentals.ExpensesLessThan1000Page => taxYear => _ => _ => ClaimPropertyIncomeAllowanceController.onPageLoad(taxYear, NormalMode)
    case propertyrentals.ClaimPropertyIncomeAllowancePage => taxYear => _ => _ => PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
    // property income
    case IsNonUKLandlordPage => taxYear => _ => userAnswers => isNonUKLandlordNavigation(taxYear, userAnswers)
    case DeductingTaxPage => taxYear => _ => _ => IncomeFromPropertyRentalsController.onPageLoad(taxYear, NormalMode)
    case IncomeFromPropertyRentalsPage => taxYear => _ => _ => LeasePremiumPaymentController.onPageLoad(taxYear, NormalMode)
    case premiumlease.LeasePremiumPaymentPage => taxYear => _ => userAnswers => leasePremiumPaymentNavigation(taxYear, userAnswers)
    case CalculatedFigureYourselfPage => taxYear => _ => userAnswers => calculatedFigureYourselfNavigation(taxYear, userAnswers)
    case premiumlease.RecievedGrantLeaseAmountPage => taxYear => _ => _ => YearLeaseAmountController.onPageLoad(taxYear, NormalMode)
    case premiumlease.YearLeaseAmountPage => taxYear => _ => _ => PremiumsGrantLeaseController.onPageLoad(taxYear, NormalMode)
    case premiumlease.PremiumsGrantLeasePage => taxYear => _ => _ => ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode)
    case ReversePremiumsReceivedPage => taxYear => _ => _ => OtherIncomeFromPropertyController.onPageLoad(taxYear, NormalMode)
    case OtherIncomeFromPropertyPage => taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case PrivateUseAdjustmentPage => taxYear => _ => _ => BalancingChargeController.onPageLoad(taxYear, NormalMode)
    case BalancingChargePage => taxYear => _ => _ => PropertyIncomeAllowanceController.onPageLoad(taxYear, NormalMode)
    case PropertyIncomeAllowancePage => taxYear => _ => _ => RenovationAllowanceBalancingChargeController.onPageLoad(taxYear, NormalMode)
    case RenovationAllowanceBalancingChargePage => taxYear => _ => _ => ResidentialFinanceCostController.onPageLoad(taxYear, NormalMode)
    case ResidentialFinanceCostPage => taxYear => _ => _ => UnusedResidentialFinanceCostController.onPageLoad(taxYear, NormalMode)
    case UnusedResidentialFinanceCostPage => taxYear => _ => _ => AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
    // expenses
    case ConsolidatedExpensesPage => taxYear => _ => userAnswers => consolidatedExpensesNavigation(taxYear, userAnswers)
    case RentsRatesAndInsurancePage => taxYear => _ => _ => RepairsAndMaintenanceCostsController.onPageLoad(taxYear, NormalMode)
    case RepairsAndMaintenanceCostsPage => taxYear => _ => _ => LoanInterestController.onPageLoad(taxYear, NormalMode)
    case LoanInterestPage => taxYear => _ => _ => OtherProfessionalFeesController.onPageLoad(taxYear, NormalMode)
    case OtherProfessionalFeesPage => taxYear => _ => _ => CostsOfServicesProvidedController.onPageLoad(taxYear, NormalMode)
    case CostsOfServicesProvidedPage => taxYear => _ => _ => PropertyBusinessTravelCostsController.onPageLoad(taxYear, NormalMode)
    case PropertyBusinessTravelCostsPage => taxYear => _ => _ => OtherAllowablePropertyExpensesController.onPageLoad(taxYear, NormalMode)
    case AnnualInvestmentAllowancePage => taxYear => _ => _ => ElectricChargePointAllowanceController.onPageLoad(taxYear, NormalMode)

    // allowances
    case CapitalAllowancesForACarPage => taxYear => _ => _ => AllowancesCheckYourAnswersController.onPageLoad(taxYear)
    case AnnualInvestmentAllowancePage => taxYear => _ => _ => ElectricChargePointAllowanceController.onPageLoad(taxYear, NormalMode)
    case ElectricChargePointAllowancePage => taxYear => _ => _ => ZeroEmissionCarAllowanceController.onPageLoad(taxYear, NormalMode)
    case ZeroEmissionCarAllowancePage => taxYear => _ => _ => ZeroEmissionGoodsVehicleAllowanceController.onPageLoad(taxYear, NormalMode)
    case ZeroEmissionGoodsVehicleAllowancePage => taxYear => _ => _ => BusinessPremisesRenovationController.onPageLoad(taxYear, NormalMode)
    case BusinessPremisesRenovationPage => taxYear => _ => _ => ReplacementOfDomesticGoodsController.onPageLoad(taxYear, NormalMode)
    case ReplacementOfDomesticGoodsPage => taxYear => _ => _ => OtherCapitalAllowanceController.onPageLoad(taxYear, NormalMode)
    case OtherCapitalAllowancePage => taxYear => _ => _ => AllowancesCheckYourAnswersController.onPageLoad(taxYear)
    case OtherAllowablePropertyExpensesPage => taxYear => _ => _ => ExpensesCheckYourAnswersController.onPageLoad(taxYear)
    case _ => _ => _ => _ => IndexController.onPageLoad
  }

  private val checkRouteMap: Page => Int => UserAnswers => UserAnswers => Call = {
    case propertyrentals.ExpensesLessThan1000Page => taxYear => _ => _ => PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
    case propertyrentals.ClaimPropertyIncomeAllowancePage => taxYear => _ => _ => PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
    case TotalIncomePage => taxYear => previousUserAnswers => userAnswers => totalIncomeNavigationCheckMode(taxYear, previousUserAnswers, userAnswers)
    // property income
    case IsNonUKLandlordPage => taxYear => previousUserAnswers => userAnswers => isNonUKLandlordNavigationCheckMode(taxYear, previousUserAnswers, userAnswers)
    case DeductingTaxPage => taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case IncomeFromPropertyRentalsPage => taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case premiumlease.LeasePremiumPaymentPage => taxYear =>
      previousUserAnswers =>
        userAnswers =>
          leasePremiumPaymentNavigationCheckMode(taxYear, previousUserAnswers, userAnswers)
    case CalculatedFigureYourselfPage => taxYear =>
      previousUserAnswers =>
        userAnswers =>
          calculatedFigureYourselfNavigationCheckMode(taxYear, previousUserAnswers, userAnswers)
    case premiumlease.RecievedGrantLeaseAmountPage => taxYear => _ => _ => YearLeaseAmountController.onPageLoad(taxYear, CheckMode)
    case premiumlease.YearLeaseAmountPage => taxYear => _ => _ => PremiumsGrantLeaseController.onPageLoad(taxYear, CheckMode)
    case premiumlease.PremiumsGrantLeasePage => taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case ReversePremiumsReceivedPage => taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case OtherIncomeFromPropertyPage => taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    // Adjustments
    case PrivateUseAdjustmentPage | PropertyIncomeAllowancePage | RenovationAllowanceBalancingChargePage |
         ResidentialFinanceCostPage | UnusedResidentialFinanceCostPage => taxYear => _ => _ => AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
    case BalancingChargePage => taxYear =>
      previousUserAnswers =>
        userAnswers =>
          balancingChargeNavigationCheckMode(taxYear, previousUserAnswers, userAnswers)
    // expenses
//    case ConsolidatedExpensesPage => taxYear => _ => userAnswers => ExpensesCheckYourAnswersController.onPageLoad(taxYear)
    // Allowances
    case CapitalAllowancesForACarPage | AnnualInvestmentAllowancePage | ElectricChargePointAllowancePage |
         ZeroEmissionCarAllowancePage | ZeroEmissionGoodsVehicleAllowancePage | BusinessPremisesRenovationPage |
         ReplacementOfDomesticGoodsPage | OtherCapitalAllowancePage => taxYear => _ => _ => AllowancesCheckYourAnswersController.onPageLoad(taxYear)
    // Expenses
    case ConsolidatedExpensesPage | RentsRatesAndInsurancePage | RepairsAndMaintenanceCostsPage |
      LoanInterestPage | OtherProfessionalFeesPage | CostsOfServicesProvidedPage |  PropertyBusinessTravelCostsPage |
         OtherAllowablePropertyExpensesPage => taxYear => _ => userAnswers => ExpensesCheckYourAnswersController.onPageLoad(taxYear)
    case _ => taxYear => _ => userAnswers => CheckYourAnswersController.onPageLoad(taxYear)
    // expenses
    case RentsRatesAndInsurancePage | RepairsAndMaintenanceCostsPage |
         LoanInterestPage | OtherProfessionalFeesPage | CostsOfServicesProvidedPage | PropertyBusinessTravelCostsPage |
         OtherAllowablePropertyExpensesPage => taxYear => _ => userAnswers => ExpensesCheckYourAnswersController.onPageLoad(taxYear)
    case ConsolidatedExpensesPage => taxYear =>
      previousUserAnswers =>
        userAnswers =>
          consolidatedExpensesNavigationCheckMode(taxYear, userAnswers)
  }

  def nextPage(page: Page, taxYear: Int, mode: Mode, previousUserAnswers: UserAnswers, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(taxYear)(previousUserAnswers)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(taxYear)(previousUserAnswers)(userAnswers)
  }

  private def isNonUKLandlordNavigation(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(IsNonUKLandlordPage) match {
      case Some(true) => DeductingTaxController.onPageLoad(taxYear, NormalMode)
      case _ => IncomeFromPropertyRentalsController.onPageLoad(taxYear, NormalMode)
    }

  private def isNonUKLandlordNavigationCheckMode(taxYear: Int, previousUserAnswers: UserAnswers, userAnswers: UserAnswers): Call =
    userAnswers.get(IsNonUKLandlordPage) match {
      case Some(true) if !previousUserAnswers.get(IsNonUKLandlordPage).getOrElse(false) => DeductingTaxController.onPageLoad(taxYear, CheckMode)
      case _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    }

  private def leasePremiumPaymentNavigation(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(LeasePremiumPaymentPage) match {
      case Some(true) => CalculatedFigureYourselfController.onPageLoad(taxYear, NormalMode)
      case _ => ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode)
    }

  private def leasePremiumPaymentNavigationCheckMode(taxYear: Int, previousUserAnswers: UserAnswers, userAnswers: UserAnswers): Call =
    userAnswers.get(LeasePremiumPaymentPage) match {
      case Some(true) if !previousUserAnswers.get(LeasePremiumPaymentPage).getOrElse(false) => CalculatedFigureYourselfController.onPageLoad(taxYear, CheckMode)
      case _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    }

  private def calculatedFigureYourselfNavigation(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(CalculatedFigureYourselfPage) match {
      case Some(CalculatedFigureYourself(true, _)) => ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode)
      case Some(CalculatedFigureYourself(false, _)) => RecievedGrantLeaseAmountController.onPageLoad(taxYear, NormalMode)
    }

  private def calculatedFigureYourselfNavigationCheckMode(taxYear: Int, previousUserAnswers: UserAnswers, userAnswers: UserAnswers): Call =
    userAnswers.get(CalculatedFigureYourselfPage) match {
      case Some(CalculatedFigureYourself(false, _)) if previousUserAnswers.get(CalculatedFigureYourselfPage).forall(_.calculatedFigureYourself) =>
        RecievedGrantLeaseAmountController.onPageLoad(taxYear, CheckMode)
      case _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    }

  private def consolidatedExpensesNavigation(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(ConsolidatedExpensesPage) match {
      case Some(ConsolidatedExpenses(true, _)) => ExpensesCheckYourAnswersController.onPageLoad(taxYear)
      case Some(ConsolidatedExpenses(false, _)) => RentsRatesAndInsuranceController.onPageLoad(taxYear, NormalMode)
    }

  private def consolidatedExpensesNavigationCheckMode(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(ConsolidatedExpensesPage) match {
      case Some(ConsolidatedExpenses(true, _)) => ExpensesCheckYourAnswersController.onPageLoad(taxYear)
      case Some(ConsolidatedExpenses(false, _)) =>
        if (userAnswers.get(OtherAllowablePropertyExpensesPage).isDefined) {
          ExpensesCheckYourAnswersController.onPageLoad(taxYear)
        } else {
          RentsRatesAndInsuranceController.onPageLoad(taxYear, NormalMode)
        }
    }

  private def balancingChargeNavigationCheckMode(taxYear: Int, previousUserAnswers: UserAnswers, userAnswers: UserAnswers): Call =
    (userAnswers.get(BalancingChargePage), previousUserAnswers.get(BalancingChargePage)) match {
      case (Some(current), Some(previous)) if current.balancingChargeYesNo == previous.balancingChargeYesNo &&
        current.balancingChargeAmount == previous.balancingChargeAmount => AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
      case _ => PropertyIncomeAllowanceController.onPageLoad(taxYear, CheckMode)
    }

  private def totalIncomeNavigationNormalMode(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(TotalIncomePage) match {
      case Some(Under) => ReportPropertyIncomeController.onPageLoad(taxYear, NormalMode)
      case _ => UKPropertySelectController.onPageLoad(taxYear, NormalMode)
    }

  private def reportIncomeNavigationNormalMode(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(ReportPropertyIncomePage) match {
      case Some(true) => UKPropertySelectController.onPageLoad(taxYear, NormalMode)
      case _ => CheckYourAnswersController.onPageLoad(taxYear)
    }

  private def totalIncomeNavigationCheckMode(taxYear: Int, previousUserAnswers: UserAnswers, userAnswers: UserAnswers): Call =
    (previousUserAnswers.get(TotalIncomePage), userAnswers.get(TotalIncomePage)) match {
      case (Some(Between), Some(Under)) | (Some(Over), Some(Under)) => ReportPropertyIncomeController.onPageLoad(taxYear, NormalMode)
      case (Some(Under), Some(Between)) | (Some(Under), Some(Over)) => UKPropertySelectController.onPageLoad(taxYear, NormalMode)
      case _ => CheckYourAnswersController.onPageLoad(taxYear)
    }
}
