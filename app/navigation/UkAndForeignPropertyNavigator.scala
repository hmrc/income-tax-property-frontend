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

package navigation

import com.google.inject.Singleton
import controllers.ukandforeignproperty.routes
import models.TotalPropertyIncome.{LessThan, Maximum}
import models._
import models.ukAndForeign.{UKPremiumsGrantLease, UkAndForeignPropertyPremiumGrantLeaseTax}
import pages.ukandforeignproperty._
import pages.{Page, UkAndForeignPropertyRentalTypeUkPage}
import play.api.mvc.Call

@Singleton
class UkAndForeignPropertyNavigator {
  private val normalRoutes: Page => Int => UserAnswers => UserAnswers => Call = {
    case TotalPropertyIncomePage =>
      taxYear => _ => userAnswers => totalIncomeNavigation(taxYear, userAnswers, NormalMode)
    case ReportIncomePage =>
      taxYear => _ => userAnswers => reportIncomeNavigation(taxYear, userAnswers, NormalMode)
    case UkAndForeignPropertyRentalTypeUkPage =>
      taxYear => _ => userAnswers => propertyRentalTypeNavigation(taxYear, userAnswers)
    case SelectCountryPage =>
      taxYear => _ => _ => routes.ForeignCountriesRentedController.onPageLoad(taxYear, NormalMode)
    case UkAndForeignPropertyClaimExpensesOrReliefPage =>
      taxYear => _ => userAnswers => claimExpensesOrReliefPageNavigation(taxYear, userAnswers)
    case UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage =>
      taxYear => _ => userAnswers => claimPropertyIncomeAllowanceOrExpensesPageNavigation(taxYear, userAnswers)
      // Long Journey - UK
    case UkNonUkResidentLandlordPage =>
      taxYear => _ => userAnswers => nonResidentLandlordNavigation(taxYear, userAnswers, NormalMode)
    case UkDeductingTaxFromNonUkResidentLandlordPage =>
      taxYear => _ => _ => routes.UkRentalPropertyIncomeController.onPageLoad(taxYear, NormalMode)
    case UkRentalPropertyIncomePage =>
      taxYear => _ => _ => routes.BalancingChargeController.onPageLoad(taxYear, NormalMode)
    case UkBalancingChargePage =>
      taxYear => _ => _ => routes.UkAndForeignPropertyPremiumForLeaseController.onPageLoad(taxYear, NormalMode)
    case UkPremiumForLeasePage =>
      taxYear => _ => userAnswers => ukPremiumForLeaseNavigation(taxYear, userAnswers, NormalMode)
    case UkPremiumGrantLeaseTaxPage =>
      taxYear => _ => userAnswers => calculateUkPremiumForLeaseNavigation(taxYear, userAnswers, NormalMode)
    case UkAmountReceivedForGrantOfLeasePage =>
      taxYear => _ => _ => routes.UkYearLeaseAmountController.onPageLoad(taxYear, NormalMode)
    case UkYearLeaseAmountPage =>
      taxYear => _ => _ => routes.UKPremiumsGrantLeaseController.onPageLoad(taxYear, NormalMode)
    case UKPremiumsGrantLeasePage =>
      taxYear => _ => _ => routes.ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode)
    case UkReversePremiumsReceivedPage =>
      taxYear => _ => _ => routes.OtherUkPropertyIncomeController.onPageLoad(taxYear, NormalMode)
    case UkOtherIncomeFromUkPropertyPage =>
      taxYear => _ => _ => routes.ForeignRentalPropertyIncomeController.onPageLoad(taxYear, NormalMode)
      // Long Journey - Foreign
    case ForeignRentalPropertyIncomePage =>
      taxYear => _ => _ => routes.UkAndForeignBalancingChargeController.onPageLoad(taxYear, NormalMode)
    case ForeignUkandForeignBalancingChargePage =>
      taxYear => _ => _ => routes.UkAndForeignForeignPremiumsForTheGrantOfALeaseController.onPageLoad(taxYear, NormalMode)
    case ForeignPremiumsForTheGrantOfALeasePage =>
      taxYear => _ => userAnswers => foreignPremiumsForLeaseNavigation(taxYear, userAnswers, NormalMode)
    case UkAndForeignCalculatedForeignPremiumGrantLeaseTaxablePage =>
      taxYear => _ => userAnswers => calculateForeignPremiumForLeaseNavigation(taxYear, userAnswers, NormalMode)
    case ForeignLeaseGrantAmountReceivedPage =>
      taxYear => _ => _ => routes.ForeignYearLeaseAmountController.onPageLoad(taxYear, NormalMode)
    case ForeignYearLeaseAmountPage =>
      taxYear => _ => _ => routes.UkAndForeignPropertyForeignPremiumsGrantLeaseController.onPageLoad(taxYear, NormalMode)
    case UkAndForeignPropertyForeignPremiumsGrantLeasePage =>
      taxYear => _ => _ => routes.UkAndForeignPropertyForeignOtherIncomeFromPropertyController.onPageLoad(taxYear, NormalMode)
    case ForeignOtherIncomeFromForeignPropertyPage =>
      taxYear => _ => _ => routes.PropertyIncomeAllowanceClaimController.onPageLoad(taxYear, NormalMode)
    case UkAndForeignPropertyIncomeAllowanceClaimPage =>
      taxYear => _ => _ => routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
    case SectionCompletePage =>
      taxYear => _ => _ => controllers.routes.SummaryController.show(taxYear)

    case _ => _ => _ => _ => controllers.routes.IndexController.onPageLoad
  }

  private val indexableRoutes: Page => Int => UserAnswers => UserAnswers => Int => Call = {
    case ForeignCountriesRentedPage =>
      taxYear => _ => userAnswers => index => foreignCountriesRentedNavigation(taxYear, userAnswers, index)
    case _ =>
      _ => _ => _ => _ => controllers.routes.IndexController.onPageLoad

  }

  private val checkRouteMap: Page => Int => UserAnswers => UserAnswers => Call = {
    case TotalPropertyIncomePage =>
      taxYear => previousAnswers => userAnswers => totalPropertyIncomeCheckModeNavigation(taxYear, previousAnswers, userAnswers)
    case ReportIncomePage =>
      taxYear => _ => userAnswers => reportIncomeCheckNavigation(taxYear, userAnswers)
    case UkAndForeignPropertyRentalTypeUkPage =>
      taxYear => _ => userAnswers => propertyRentalTypeNavigation(taxYear, userAnswers)
    case SelectCountryPage =>
      taxYear => _ => _ => routes.ForeignCountriesRentedController.onPageLoad(taxYear, CheckMode)
    case UkAndForeignPropertyClaimExpensesOrReliefPage =>
      taxYear => _ => userAnswers => claimExpensesOrReliefPageNavigation(taxYear, userAnswers)
    case UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage =>
      taxYear => _ => userAnswers => claimPropertyIncomeAllowanceOrExpensesPageNavigation(taxYear, userAnswers)
    // Long Journey - UK
    case UkNonUkResidentLandlordPage =>
      taxYear => _ => userAnswers => nonResidentLandlordNavigation(taxYear, userAnswers, CheckMode)
    case UkDeductingTaxFromNonUkResidentLandlordPage =>
      taxYear => _ => _ => routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
    case UkRentalPropertyIncomePage =>
      taxYear => _ => _ => routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
    case UkBalancingChargePage =>
      taxYear => _ => _ => routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
    case UkPremiumForLeasePage =>
      taxYear => _ => userAnswers => ukPremiumForLeaseNavigation(taxYear, userAnswers, CheckMode)
    case UkPremiumGrantLeaseTaxPage =>
      taxYear => _ => userAnswers => calculateUkPremiumForLeaseNavigation(taxYear, userAnswers, CheckMode)
    case UkAmountReceivedForGrantOfLeasePage =>
      taxYear => _ => _ => routes.UkYearLeaseAmountController.onPageLoad(taxYear, CheckMode)
    case UkYearLeaseAmountPage =>
      taxYear => _ => _ => routes.UKPremiumsGrantLeaseController.onPageLoad(taxYear, CheckMode)
    case UKPremiumsGrantLeasePage =>
      taxYear => _ => _ => routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
    case UkReversePremiumsReceivedPage =>
      taxYear => _ => _ => routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
    case UkOtherIncomeFromUkPropertyPage =>
      taxYear => _ => _ => routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
    // Long Journey - Foreign
    case ForeignRentalPropertyIncomePage =>
      taxYear => _ => _ => routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
    case ForeignUkandForeignBalancingChargePage =>
      taxYear => _ => _ => routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
    case ForeignPremiumsForTheGrantOfALeasePage =>
      taxYear => _ => userAnswers => foreignPremiumsForLeaseNavigation(taxYear, userAnswers, CheckMode)
    case UkAndForeignCalculatedForeignPremiumGrantLeaseTaxablePage =>
      taxYear => _ => userAnswers => calculateForeignPremiumForLeaseNavigation(taxYear, userAnswers, CheckMode)
    case ForeignLeaseGrantAmountReceivedPage =>
      taxYear => _ => _ => routes.ForeignYearLeaseAmountController.onPageLoad(taxYear, CheckMode)
    case ForeignYearLeaseAmountPage =>
      taxYear => _ => _ => routes.UkAndForeignPropertyForeignPremiumsGrantLeaseController.onPageLoad(taxYear, CheckMode)
    case UkAndForeignPropertyForeignPremiumsGrantLeasePage =>
      taxYear => _ => _ => routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
    case ForeignOtherIncomeFromForeignPropertyPage =>
      taxYear => _ => _ => routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
    case UkAndForeignPropertyIncomeAllowanceClaimPage =>
      taxYear => _ => _ => routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)

    case _ => _ => _ => _ => controllers.routes.IndexController.onPageLoad
  }


  private val indexableCheckRoutes: Page => Int => UserAnswers => UserAnswers => Int => Call = {
    case ForeignCountriesRentedPage =>
      taxYear => _ => userAnswers => index => foreignCountriesRentedNavigationCheckMode(taxYear, userAnswers, index)
    case _ =>
      _ => _ => _ => _ => controllers.routes.IndexController.onPageLoad

  }


  def nextPage(page: Page, taxYear: Int, mode: Mode, previousUserAnswers: UserAnswers, userAnswers: UserAnswers): Call = {
    mode match {
      case NormalMode =>
        normalRoutes(page)(taxYear)(previousUserAnswers)(userAnswers)
      case CheckMode =>
        checkRouteMap(page)(taxYear)(previousUserAnswers)(userAnswers)
    }
  }

  def nextIndex(page: Page, taxYear: Int, mode: Mode, previousUserAnswers: UserAnswers, userAnswers: UserAnswers, index: Int): Call = {
    mode match {
      case NormalMode =>
        indexableRoutes(page)(taxYear)(previousUserAnswers)(userAnswers)(index)
      case CheckMode =>
        indexableCheckRoutes(page)(taxYear)(previousUserAnswers)(userAnswers)(index)
    }
  }

  private def propertyRentalTypeNavigation(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(SelectCountryPage) match {
      case Some(countries) if countries.nonEmpty =>
        routes.ForeignCountriesRentedController.onPageLoad(taxYear, NormalMode)
      case _        =>
        routes.SelectCountryController.onPageLoad(taxYear, Index(1), NormalMode)
    }

  private def totalIncomeNavigation(taxYear: Int, userAnswers: UserAnswers, mode: Mode): Call = {
    userAnswers.get(TotalPropertyIncomePage) match {
      case Some(TotalPropertyIncome.Maximum) => routes.UkAndForeignPropertyRentalTypeUkController.onPageLoad(taxYear, mode)
      case Some(TotalPropertyIncome.LessThan) => routes.ReportIncomeController.onPageLoad(taxYear, mode)
    }
  }

  private def reportIncomeNavigation(taxYear: Int, userAnswers: UserAnswers, mode: Mode): Call = {
    userAnswers.get(ReportIncomePage) match {
      case Some(ReportIncome.WantToReport) => routes.UkAndForeignPropertyRentalTypeUkController.onPageLoad(taxYear, mode)
      case Some(ReportIncome.DoNoWantToReport) => routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
    }
  }

  private def foreignCountriesRentedNavigation(taxYear: Int, userAnswers: UserAnswers, index: Int): Call = {

    ( userAnswers.get(ForeignCountriesRentedPage), userAnswers.get(UkAndForeignPropertyRentalTypeUkPage).map(_.toSeq)) match {
      case (Some(true),_) =>
        routes.SelectCountryController.onPageLoad(taxYear, Index(index + 1), NormalMode)
      case (Some(false), None) =>
        throw new RuntimeException("No rental type selected")  // should never happen
      case (Some(false), Some(Seq(UkAndForeignPropertyRentalTypeUk.PropertyRentals))) =>
        routes.UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesController.onPageLoad(taxYear, NormalMode)
      case _ =>
        routes.UkAndForeignPropertyClaimExpensesOrReliefController.onPageLoad(taxYear, NormalMode)
    }
  }

  private def foreignCountriesRentedNavigationCheckMode(taxYear: Int, userAnswers: UserAnswers, index: Int): Call = {
    userAnswers.get(ForeignCountriesRentedPage)  match {
      case Some(true) =>
        routes.SelectCountryController.onPageLoad(taxYear, Index(index + 1), CheckMode)
      case Some(false) =>
        routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
    }
  }

  private def claimExpensesOrReliefPageNavigation(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(UkAndForeignPropertyClaimExpensesOrReliefPage) match {
      case Some(UkAndForeignPropertyClaimExpensesOrRelief(_)) =>
        routes.UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesController.onPageLoad(taxYear, NormalMode)
    }

  private def claimPropertyIncomeAllowanceOrExpensesPageNavigation(taxYear: Int, userAnswers: UserAnswers): Call = {

    (userAnswers.get(UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage), userAnswers.get(UkAndForeignPropertyRentalTypeUkPage).map(_.toSeq)) match {
      case (Some(UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(true)), Some(Seq(UkAndForeignPropertyRentalTypeUk.RentARoom))) =>
        routes.ForeignRentalPropertyIncomeController.onPageLoad(taxYear, NormalMode)
      case (Some(UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(true)), Some(_)) =>
        routes.NonResidentLandlordUKController.onPageLoad(taxYear, NormalMode)
      case (Some(UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(false)), _) =>
        routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
    }
  }

  private def nonResidentLandlordNavigation(taxYear: Int, userAnswers: UserAnswers, mode: Mode): Call =
    (userAnswers.get(UkNonUkResidentLandlordPage), mode) match {
      case (Some(true), NormalMode) =>
        routes.UkAndForeignPropertyDeductingTaxFromNonUkResidentLandlordController.onPageLoad(taxYear, NormalMode)
      case (Some(false), NormalMode) =>
        routes.UkRentalPropertyIncomeController.onPageLoad(taxYear, NormalMode)
      case (Some(true), CheckMode) =>
        routes.UkAndForeignPropertyDeductingTaxFromNonUkResidentLandlordController.onPageLoad(taxYear, CheckMode)
      case (Some(false), CheckMode) =>
        routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
    }

  private def ukPremiumForLeaseNavigation(taxYear: Int, userAnswers: UserAnswers, mode: Mode): Call =
    (userAnswers.get(UkPremiumForLeasePage), mode) match {
      case (Some(true), NormalMode) =>
        routes.UkAndForeignPropertyPremiumGrantLeaseTaxController.onPageLoad(taxYear, NormalMode)
      case (Some(false), NormalMode) =>
        routes.ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode)
      case (Some(false), CheckMode) =>
        routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
      case (Some(true), CheckMode) =>
        routes.UkAndForeignPropertyPremiumGrantLeaseTaxController.onPageLoad(taxYear, CheckMode)


      case _ => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  private def calculateUkPremiumForLeaseNavigation(taxYear: Int, userAnswers: UserAnswers, mode: Mode): Call =
    (userAnswers.get(UkPremiumGrantLeaseTaxPage), mode) match {
      case (Some(UkAndForeignPropertyPremiumGrantLeaseTax(true, _)), NormalMode) =>
        routes.ReversePremiumsReceivedController.onPageLoad(taxYear, NormalMode)
      case (Some(UkAndForeignPropertyPremiumGrantLeaseTax(false, _)), NormalMode) =>
        routes.UkAndForeignPropertyAmountReceivedForGrantOfLeaseController.onPageLoad(taxYear, NormalMode)
      case (Some(UkAndForeignPropertyPremiumGrantLeaseTax(false, _)), CheckMode) =>
        routes.UkAndForeignPropertyAmountReceivedForGrantOfLeaseController.onPageLoad(taxYear, CheckMode)
      case (Some(UkAndForeignPropertyPremiumGrantLeaseTax(true, _)), CheckMode) =>
        routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
    }

  private def foreignPremiumsForLeaseNavigation(taxYear: Int, userAnswers: UserAnswers, mode: Mode): Call =
      (userAnswers.get(ForeignPremiumsForTheGrantOfALeasePage), mode) match {
        case (Some(true), NormalMode) =>
          routes.UkAndForeignCalculatedForeignPremiumGrantLeaseTaxableController.onPageLoad(taxYear, NormalMode)
        case (Some(false), NormalMode) =>
          routes.UkAndForeignPropertyForeignOtherIncomeFromPropertyController.onPageLoad(taxYear, NormalMode)
        case (Some(true), CheckMode) =>
          routes.UkAndForeignCalculatedForeignPremiumGrantLeaseTaxableController.onPageLoad(taxYear, CheckMode)
        case (Some(false), CheckMode) =>
          routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
      }

  private def calculateForeignPremiumForLeaseNavigation(taxYear: Int, userAnswers: UserAnswers, mode: Mode): Call =
    (userAnswers.get(UkAndForeignCalculatedForeignPremiumGrantLeaseTaxablePage), mode) match {
      case (Some(PremiumCalculated(true, _)), NormalMode) =>
        routes.UkAndForeignPropertyForeignOtherIncomeFromPropertyController.onPageLoad(taxYear, NormalMode)
      case (Some(PremiumCalculated(false, _)), NormalMode) =>
        routes.LeaseGrantAmountReceivedController.onPageLoad(taxYear, NormalMode)
      case (Some(PremiumCalculated(false, _)), CheckMode) =>
        routes.LeaseGrantAmountReceivedController.onPageLoad(taxYear, CheckMode)
      case (Some(PremiumCalculated(true, _)), CheckMode) =>
        routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
    }


  private def totalPropertyIncomeCheckModeNavigation(
                                              taxYear: Int,
                                              previousAnswers: UserAnswers,
                                              userAnswers: UserAnswers
                                            ): Call =
    (previousAnswers.get(TotalPropertyIncomePage), userAnswers.get(TotalPropertyIncomePage)) match {
      case (Some(LessThan), Some(Maximum)) =>
        routes.UkAndForeignPropertyRentalTypeUkController.onPageLoad(taxYear, NormalMode)
      case (Some(Maximum), Some(LessThan)) =>
        routes.ReportIncomeController.onPageLoad(taxYear, CheckMode)
      case _ => routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
    }

  private def reportIncomeCheckNavigation(taxYear: Int, userAnswers: UserAnswers): Call = {
    userAnswers.get(ReportIncomePage) match {
      case Some(ReportIncome.WantToReport) => routes.UkAndForeignPropertyRentalTypeUkController.onPageLoad(taxYear, NormalMode)
      case Some(ReportIncome.DoNoWantToReport) => routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
    }
  }

}
