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

import controllers.premiumlease.routes._
import controllers.propertyrentals.routes._
import controllers.adjustments.routes._
import controllers.routes
import controllers.routes._
import models._
import pages._
import pages.premiumlease.LeasePremiumPaymentPage
import pages.propertyrentals.IsNonUKLandlordPage
import play.api.mvc.Call
import pages.adjustments.{PrivateUseAdjustmentPage,BalancingChargePage,PropertyIncomeAllowancePage,
  ResidentialFinanceCostPage, RenovationAllowanceBalancingChargePage, UnusedResidentialFinanceCostPage}

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => Int => UserAnswers => UserAnswers => Call = {
    case UKPropertyDetailsPage => taxYear => _ => _ => routes.TotalIncomeController.onPageLoad(taxYear, NormalMode)
    case TotalIncomePage => taxYear => _ => _ => routes.UKPropertySelectController.onPageLoad(taxYear, NormalMode)
    case UKPropertySelectPage => taxYear => _ => _ => routes.SummaryController.show(taxYear)
    case UKPropertyPage => _ => _ => _ => routes.CheckYourAnswersController.onPageLoad
    case propertyrentals.ExpensesLessThan1000Page => taxYear => _ => _ => ClaimPropertyIncomeAllowanceController.onPageLoad(taxYear, NormalMode)
    case propertyrentals.ClaimPropertyIncomeAllowancePage => taxYear => _ => _ => PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
    // property income
    case IsNonUKLandlordPage => taxYear => _ => userAnswers => isNonUKLandlordNavigation(taxYear, userAnswers)
    case DeductingTaxPage => taxYear => _ => _ => routes.IncomeFromPropertyRentalsController.onPageLoad(taxYear, NormalMode)
    case IncomeFromPropertyRentalsPage => taxYear => _ => _ => LeasePremiumPaymentController.onPageLoad(taxYear, NormalMode)
    case premiumlease.LeasePremiumPaymentPage => taxYear => _ => userAnswers => leasePremiumPaymentNavigation(taxYear, userAnswers)
    case CalculatedFigureYourselfPage => taxYear => _ => userAnswers => calculatedFigureYourselfNavigation(taxYear, userAnswers)
    case premiumlease.RecievedGrantLeaseAmountPage => taxYear => _ => _ => YearLeaseAmountController.onPageLoad(taxYear, NormalMode)
    case premiumlease.YearLeaseAmountPage => taxYear => _ => _ => PremiumsGrantLeaseController.onPageLoad(taxYear, NormalMode)
    case premiumlease.PremiumsGrantLeasePage => taxYear => _ => _ => routes.ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode)
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
    case LoanInterestPage => taxYear => _ => _ => routes.OtherProfessionalFeesController.onPageLoad(taxYear, NormalMode)
    case OtherProfessionalFeesPage => taxYear => _ => _ => routes.CostsOfServicesProvidedController.onPageLoad(taxYear, NormalMode)
    case CostsOfServicesProvidedPage => taxYear => _ => _ => routes.PropertyBusinessTravelCostsController.onPageLoad(taxYear, NormalMode)
    case _ => _ => _ => _ => routes.IndexController.onPageLoad
  }

  private val checkRouteMap: Page => Int => UserAnswers => UserAnswers => Call = {
    case propertyrentals.ExpensesLessThan1000Page => taxYear => _ => _ => PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
    case propertyrentals.ClaimPropertyIncomeAllowancePage => taxYear => _ => _ => PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
    // property income
    case IsNonUKLandlordPage => taxYear => previousUserAnswers => userAnswers => isNonUKLandlordNavigationCheckMode(taxYear, previousUserAnswers, userAnswers)
    case DeductingTaxPage => taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case IncomeFromPropertyRentalsPage => taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case premiumlease.LeasePremiumPaymentPage => taxYear => previousUserAnswers => userAnswers =>
      leasePremiumPaymentNavigationCheckMode(taxYear, previousUserAnswers, userAnswers)
    case CalculatedFigureYourselfPage => taxYear => previousUserAnswers => userAnswers =>
      calculatedFigureYourselfNavigationCheckMode(taxYear, previousUserAnswers, userAnswers)
    case premiumlease.RecievedGrantLeaseAmountPage => taxYear => _ => _ => YearLeaseAmountController.onPageLoad(taxYear, CheckMode)
    case premiumlease.YearLeaseAmountPage => taxYear => _ => _ => PremiumsGrantLeaseController.onPageLoad(taxYear, CheckMode)
    case premiumlease.PremiumsGrantLeasePage => taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case ReversePremiumsReceivedPage => taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    case OtherIncomeFromPropertyPage => taxYear => _ => _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
   // Adjustments
    case PrivateUseAdjustmentPage | PropertyIncomeAllowancePage | RenovationAllowanceBalancingChargePage |
         ResidentialFinanceCostPage | UnusedResidentialFinanceCostPage => taxYear => _ => _ => AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
    case BalancingChargePage => taxYear => previousUserAnswers => userAnswers =>
          balancingChargeNavigationCheckMode(taxYear, previousUserAnswers, userAnswers)
    // expenses
    case ConsolidatedExpensesPage => taxYear => _ => userAnswers => ExpensesCheckYourAnswersController.onPageLoad(taxYear)
    case _ => _ => _ => _ => routes.CheckYourAnswersController.onPageLoad
  }

  def nextPage(page: Page, taxYear: Int, mode: Mode, previousUserAnswers: UserAnswers, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(taxYear)(previousUserAnswers)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(taxYear)(previousUserAnswers)(userAnswers)
  }

  private def isNonUKLandlordNavigation(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(IsNonUKLandlordPage) match {
      case Some(true) => routes.DeductingTaxController.onPageLoad(taxYear, NormalMode)
      case _ => routes.IncomeFromPropertyRentalsController.onPageLoad(taxYear, NormalMode)
    }

  private def isNonUKLandlordNavigationCheckMode(taxYear: Int, previousUserAnswers: UserAnswers, userAnswers: UserAnswers): Call =
    userAnswers.get(IsNonUKLandlordPage) match {
      case Some(true) if !previousUserAnswers.get(IsNonUKLandlordPage).getOrElse(false) => routes.DeductingTaxController.onPageLoad(taxYear, CheckMode)
      case _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    }

  private def leasePremiumPaymentNavigation(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(LeasePremiumPaymentPage) match {
      case Some(true) => CalculatedFigureYourselfController.onPageLoad(taxYear, NormalMode)
      case _ => routes.ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode)
    }

  private def leasePremiumPaymentNavigationCheckMode(taxYear: Int, previousUserAnswers: UserAnswers, userAnswers: UserAnswers): Call =
    userAnswers.get(LeasePremiumPaymentPage) match {
      case Some(true) if !previousUserAnswers.get(LeasePremiumPaymentPage).getOrElse(false) => CalculatedFigureYourselfController.onPageLoad(taxYear, CheckMode)
      case _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    }

  private def calculatedFigureYourselfNavigation(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(CalculatedFigureYourselfPage) match {
      case Some(CalculatedFigureYourself(true, _)) => routes.ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode)
      case Some(CalculatedFigureYourself(false, _)) => RecievedGrantLeaseAmountController.onPageLoad(taxYear, NormalMode)
  }

  private def calculatedFigureYourselfNavigationCheckMode(taxYear: Int, previousUserAnswers: UserAnswers, userAnswers: UserAnswers): Call =
    userAnswers.get(CalculatedFigureYourselfPage) match {
      case Some(CalculatedFigureYourself(false, _)) if previousUserAnswers.get(CalculatedFigureYourselfPage).map(_.calculatedFigureYourself).getOrElse(true) =>
        RecievedGrantLeaseAmountController.onPageLoad(taxYear, CheckMode)
      case _ => PropertyIncomeCheckYourAnswersController.onPageLoad(taxYear)
    }

  private def consolidatedExpensesNavigation(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(ConsolidatedExpensesPage) match {
      case Some(ConsolidatedExpenses(true, _)) => routes.ExpensesCheckYourAnswersController.onPageLoad(taxYear)
      case Some(ConsolidatedExpenses(false, _)) => ConsolidatedExpensesController.onPageLoad(taxYear, NormalMode)
    }

  private def balancingChargeNavigationCheckMode(taxYear: Int, previousUserAnswers: UserAnswers, userAnswers: UserAnswers): Call =
    (userAnswers.get(BalancingChargePage), previousUserAnswers.get(BalancingChargePage)) match {
      case (Some(current), Some(previous)) if current.balancingChargeYesNo == previous.balancingChargeYesNo &&
        current.balancingChargeAmount == previous.balancingChargeAmount => AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
      case _ => PropertyIncomeAllowanceController.onPageLoad(taxYear, CheckMode)
    }
}
