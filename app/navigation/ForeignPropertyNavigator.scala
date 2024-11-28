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

package navigation

import com.google.inject.Singleton
import controllers.foreign.income.routes._
import controllers.foreign.routes._
import controllers.routes.{IndexController, SummaryController}
import models.ForeignTotalIncome.{LessThanOneThousand, OneThousandAndMore}
import models._
import pages.foreign._
import pages.foreign.expenses.ForeignExpensesSectionCompletePage
import pages.foreign.income._
import pages.{Page, PremiumsGrantLeaseYNPage}
import play.api.mvc.Call

@Singleton
class ForeignPropertyNavigator {
  private val normalRoutes: Page => Int => UserAnswers => UserAnswers => Call = {
    case TotalIncomePage =>
      taxYear => _ => userAnswers => foreignTotalIncomeNavigationNormalMode(taxYear, userAnswers)
    case SelectIncomeCountryPage(_) =>
      taxYear => _ => _ => CountriesRentedPropertyController.onPageLoad(taxYear, NormalMode)
    case ForeignPropertyRentalIncomePage(countryCode) =>
      taxYear => _ => _ => PremiumsGrantLeaseYNController.onPageLoad(taxYear, countryCode, NormalMode)
    case PropertyIncomeReportPage =>
      taxYear => _ => userAnswers => reportIncomeNavigation(taxYear, userAnswers)
    case ForeignReversePremiumsReceivedPage(countryCode) =>
      taxYear => _ => _ => ForeignOtherIncomeFromPropertyController.onPageLoad(taxYear, countryCode, NormalMode)
    case AddCountriesRentedPage =>
      taxYear => _ => userAnswers => addCountryNavigationNormalMode(taxYear, userAnswers)
    case ClaimPropertyIncomeAllowanceOrExpensesPage =>
      taxYear => _ => _ => ForeignCountriesCheckYourAnswersController.onPageLoad(taxYear)
    case ForeignSelectCountriesCompletePage => taxYear => _ => _ => SummaryController.show(taxYear)
    case ForeignIncomeTaxPage(countryCode) =>
      taxYear => _ => userAnswers => foreignIncomeTaxNavigation(taxYear, countryCode, userAnswers)
    case ClaimForeignTaxCreditReliefPage(countryCode) =>
      taxYear => _ => _ => ForeignTaxCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignTaxSectionCompletePage(_) =>
      taxYear =>
        _ =>
          _ =>
            SummaryController.show(taxYear)
        // TODO route to CYA page once created
    case PremiumsGrantLeaseYNPage(countryCode) =>
      taxYear => _ => userAnswers => incomePremiumForGrantOfLeaseNavigationNormalMode(taxYear, countryCode, userAnswers)
    case CalculatedPremiumLeaseTaxablePage(countryCode) =>
      taxYear =>
        _ => userAnswers => incomeCalculatePremiumLeaseTaxableNavigationNormalMode(taxYear, countryCode, userAnswers)
    case ForeignReceivedGrantLeaseAmountPage(countryCode) =>
      taxYear => _ => _ => ForeignYearLeaseAmountController.onPageLoad(taxYear, countryCode, NormalMode)
    case ForeignYearLeaseAmountPage(countryCode) =>
      taxYear => _ => _ => ForeignPremiumsGrantLeaseController.onPageLoad(taxYear, countryCode, NormalMode)
    case ForeignPremiumsGrantLeasePage(countryCode) =>
      taxYear => _ => _ => ForeignReversePremiumsReceivedController.onPageLoad(taxYear, countryCode, NormalMode)
    case ForeignReversePremiumsReceivedPage(countryCode) =>
      taxYear => _ => _ => ForeignOtherIncomeFromPropertyController.onPageLoad(taxYear, countryCode, NormalMode)
    case ForeignOtherIncomeFromPropertyPage(countryCode) =>
      taxYear => _ => _ => ForeignPropertyIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignIncomeSectionCompletePage(countryCode) =>
      taxYear => _ => _ => ForeignIncomeSectionCompleteController.onPageLoad(taxYear, countryCode)
    case ForeignExpensesSectionCompletePage(countryCode) =>
      taxYear => _ => _ => SummaryController.show(taxYear)
    case _ => _ => _ => _ => controllers.routes.IndexController.onPageLoad
  }

  private val checkRouteMap: Page => Int => UserAnswers => UserAnswers => Call = {
    case TotalIncomePage =>
      taxYear => previousAnswers => userAnswers => totalIncomeCheckModeNavigation(taxYear, previousAnswers, userAnswers)
    case SelectIncomeCountryPage(_) =>
      taxYear => _ => _ => CountriesRentedPropertyController.onPageLoad(taxYear, CheckMode)
    case DoYouWantToRemoveCountryPage =>
      taxYear => _ => userAnswers => removeCountryNavigation(taxYear, userAnswers)
    case PropertyIncomeReportPage =>
      taxYear => _ => userAnswers => reportIncomeNavigation(taxYear, userAnswers)
    case AddCountriesRentedPage =>
      taxYear => _ => userAnswers => addCountryNavigationCheckMode(taxYear, userAnswers)
    case ClaimPropertyIncomeAllowanceOrExpensesPage =>
      taxYear => _ => _ => ForeignCountriesCheckYourAnswersController.onPageLoad(taxYear)
    case ForeignIncomeTaxPage(countryCode) =>
      taxYear => _ => userAnswers => foreignIncomeTaxNavigation(taxYear, countryCode, userAnswers, CheckMode)
    case PremiumsGrantLeaseYNPage(countryCode) =>
      taxYear => _ => userAnswers => incomePremiumForGrantOfLeaseNavigationCheckMode(taxYear, countryCode, userAnswers)
    case CalculatedPremiumLeaseTaxablePage(countryCode) =>
      taxYear =>
        _ => userAnswers => incomeCalculatePremiumLeaseTaxableNavigationCheckMode(taxYear, countryCode, userAnswers)
    case ForeignReceivedGrantLeaseAmountPage(countryCode) =>
      taxYear => _ => _ => ForeignPropertyIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignYearLeaseAmountPage(countryCode) =>
      taxYear => _ => _ => ForeignPropertyIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignPremiumsGrantLeasePage(countryCode) =>
      taxYear => _ => _ => ForeignPropertyIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignReversePremiumsReceivedPage(countryCode) =>
      taxYear => _ => _ => ForeignPropertyIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignOtherIncomeFromPropertyPage(countryCode) =>
      taxYear => _ => _ => ForeignPropertyIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ClaimForeignTaxCreditReliefPage(countryCode) =>
      taxYear => _ => _ => ForeignPropertyIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case _ => _ => _ => _ => controllers.routes.IndexController.onPageLoad
  }

  def nextPage(page: Page, taxYear: Int, mode: Mode, previousUserAnswers: UserAnswers, userAnswers: UserAnswers): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(taxYear)(previousUserAnswers)(userAnswers)
      case CheckMode =>
        checkRouteMap(page)(taxYear)(previousUserAnswers)(userAnswers)
    }

  private def addAnotherCountryNavigation(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(AddCountriesRentedPage) match {
      case Some(true) =>
        val nextIndex = userAnswers.get(IncomeSourceCountries).map(_.length).getOrElse(0)
        SelectIncomeCountryController.onPageLoad(taxYear, nextIndex, NormalMode)
      case _ => CountriesRentedPropertyController.onPageLoad(taxYear, NormalMode)
    }

  private def foreignTotalIncomeNavigationNormalMode(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(TotalIncomePage) match {
      case Some(LessThanOneThousand) => PropertyIncomeReportController.onPageLoad(taxYear, NormalMode)
      case _                         => SelectIncomeCountryController.onPageLoad(taxYear, 0, NormalMode)
    }

  private def reportIncomeNavigation(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(PropertyIncomeReportPage) match {
      case Some(true) => SelectIncomeCountryController.onPageLoad(taxYear, 0, NormalMode)
      case _          => ForeignCountriesCheckYourAnswersController.onPageLoad(taxYear)
    }

  private def removeCountryNavigation(taxYear: Int, userAnswers: UserAnswers): Call = {

    val countries = userAnswers.get(IncomeSourceCountries).toSeq.flatten
    userAnswers.get(DoYouWantToRemoveCountryPage) match {
      case Some(true) if countries.isEmpty => SelectIncomeCountryController.onPageLoad(taxYear, 0, NormalMode)
      case Some(true)                      => CountriesRentedPropertyController.onPageLoad(taxYear, NormalMode)
      case Some(false)                     => CountriesRentedPropertyController.onPageLoad(taxYear, NormalMode)
      case None                            => IndexController.onPageLoad
    }

  }

  private def addCountryNavigationNormalMode(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(AddCountriesRentedPage) match {
      case Some(true) =>
        val nextIndex = userAnswers.get(IncomeSourceCountries).map(_.length).getOrElse(0)
        SelectIncomeCountryController.onPageLoad(taxYear, nextIndex, NormalMode)
      case Some(false) => ClaimPropertyIncomeAllowanceOrExpensesController.onPageLoad(taxYear, NormalMode)
      case _           => IndexController.onPageLoad
    }

  private def addCountryNavigationCheckMode(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(AddCountriesRentedPage) match {
      case Some(true) =>
        val nextIndex = userAnswers.get(IncomeSourceCountries).map(_.length).getOrElse(0)
        SelectIncomeCountryController.onPageLoad(taxYear, nextIndex, CheckMode)
      case Some(false) =>
        ForeignCountriesCheckYourAnswersController.onPageLoad(taxYear)
      case _ => IndexController.onPageLoad
    }

  private def incomePremiumForGrantOfLeaseNavigationNormalMode(
    taxYear: Int,
    countryCode: String,
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(PremiumsGrantLeaseYNPage(countryCode)) match {
      case Some(true) => CalculatedPremiumLeaseTaxableController.onPageLoad(taxYear, countryCode, NormalMode)
      case _          => ForeignReversePremiumsReceivedController.onPageLoad(taxYear, countryCode, NormalMode)
    }

  private def incomePremiumForGrantOfLeaseNavigationCheckMode(
    taxYear: Int,
    countryCode: String,
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(PremiumsGrantLeaseYNPage(countryCode)) match {
      case Some(true) => CalculatedPremiumLeaseTaxableController.onPageLoad(taxYear, countryCode, CheckMode)
      case _          => ForeignPropertyIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    }

  private def incomeCalculatePremiumLeaseTaxableNavigationNormalMode(
    taxYear: Int,
    countryCode: String,
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(CalculatedPremiumLeaseTaxablePage(countryCode)) match {
      case Some(PremiumCalculated(true, _)) =>
        ForeignReversePremiumsReceivedController.onPageLoad(taxYear, countryCode, NormalMode)
      case Some(PremiumCalculated(false, _)) =>
        ForeignReceivedGrantLeaseAmountController
          .onPageLoad(taxYear, countryCode, NormalMode)
    }

  private def incomeCalculatePremiumLeaseTaxableNavigationCheckMode(
    taxYear: Int,
    countryCode: String,
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(CalculatedPremiumLeaseTaxablePage(countryCode)) match {
      case Some(PremiumCalculated(true, _)) =>
        ForeignReversePremiumsReceivedController.onPageLoad(taxYear, countryCode, CheckMode)
      case Some(PremiumCalculated(false, _)) =>
        ForeignPropertyIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    }

  private def foreignReversePremiumReceivedNavigation(
    taxYear: Int,
    countryCode: String,
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(ForeignReversePremiumsReceivedPage(countryCode)) match {
      // TODO should go to the other income from property page
      case Some(ReversePremiumsReceived(true, _)) =>
        ForeignReversePremiumsReceivedController
          .onPageLoad(taxYear, countryCode, NormalMode)
      // TODO should go to the other income from property page
      case _ =>
        ForeignReversePremiumsReceivedController
          .onPageLoad(taxYear, countryCode, NormalMode)

    }

  private def totalIncomeCheckModeNavigation(taxYear: Int, previousAnswers: UserAnswers, userAnswers: UserAnswers) =
    (previousAnswers.get(TotalIncomePage), userAnswers.get(TotalIncomePage)) match {
      case (Some(LessThanOneThousand), Some(OneThousandAndMore)) =>
        SelectIncomeCountryController.onPageLoad(taxYear, 0, NormalMode)
      case (Some(OneThousandAndMore), Some(LessThanOneThousand)) =>
        PropertyIncomeReportController.onPageLoad(taxYear, NormalMode)
      case _ => ForeignCountriesCheckYourAnswersController.onPageLoad(taxYear)
    }

  private def foreignIncomeTaxNavigation(
    taxYear: Int,
    countryCode: String,
    userAnswers: UserAnswers,
    mode: Mode = NormalMode
  ): Call =
    userAnswers.get(ForeignIncomeTaxPage(countryCode)) match {
      case Some(ForeignIncomeTax(true, _)) =>
        ClaimForeignTaxCreditReliefController.onPageLoad(taxYear, countryCode, mode)
      case _ =>
        ForeignTaxCheckYourAnswersController.onSubmit(taxYear, countryCode)
    }
}
