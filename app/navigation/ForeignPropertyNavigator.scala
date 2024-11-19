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
import controllers.propertyrentals.income.routes.OtherPropertyRentalIncomeController
import controllers.foreign.routes._
import controllers.routes.{IndexController, SummaryController}
import models.ForeignTotalIncome.{LessThanOneThousand, OneThousandAndMore}
import models.{CheckMode, ForeignIncomeTax, PremiumCalculated, Rentals, Mode, NormalMode, ReversePremiumsReceived, UserAnswers}
import pages.Page
import pages.foreign._
import pages.foreign.income.ForeignReversePremiumsReceivedPage
import pages.premiumlease.PremiumForLeasePage
import play.api.mvc.Call

@Singleton
class ForeignPropertyNavigator {
  private val normalRoutes: Page => Int => UserAnswers => UserAnswers => Call = {
    case TotalIncomePage =>
      taxYear => _ => userAnswers => foreignTotalIncomeNavigationNormalMode(taxYear, userAnswers)
    case SelectIncomeCountryPage(_) =>
      taxYear => _ => _ => CountriesRentedPropertyController.onPageLoad(taxYear, NormalMode)
    case PropertyIncomeReportPage =>
      taxYear => _ => userAnswers => reportIncomeNavigation(taxYear, userAnswers)
    case ForeignReversePremiumsReceivedPage(countryCode) =>
      taxYear => _ => userAnswers => foreignReversePremiumReceivedNavigation(taxYear, countryCode, userAnswers)
    case AddCountriesRentedPage =>
      taxYear => _ => userAnswers => addCountryNavigationNormalMode(taxYear, userAnswers)
    case ClaimPropertyIncomeAllowanceOrExpensesPage =>
      taxYear => _ => _ => ForeignCountriesCheckYourAnswersController.onPageLoad(taxYear)
    case ForeignSelectCountriesCompletePage => taxYear => _ => _ => SummaryController.show(taxYear)
    case ForeignIncomeTaxPage(countryCode) =>
      taxYear => _ => userAnswers => foreignIncomeTaxNavigation(taxYear, countryCode, userAnswers)
    case ClaimForeignTaxCreditReliefPage(countryCode) =>
      taxYear => _ => _ => ForeignTaxCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignTaxSectionCompletePage => taxYear => _ => _ => SummaryController.show(taxYear)
    case CalculatedPremiumLeaseTaxablePage(countryCode) =>
      taxYear =>
        _ =>
          userAnswers =>
            userAnswers.get(CalculatedPremiumLeaseTaxablePage(countryCode)) match {
              case Some(PremiumCalculated(true, _)) =>
                OtherPropertyRentalIncomeController.onPageLoad(taxYear, NormalMode, Rentals)
              case Some(PremiumCalculated(false, _)) =>
                controllers.foreign.routes.ForeignReceivedGrantLeaseAmountController.onPageLoad(taxYear, countryCode, NormalMode)
            }
    case _ => _ => _ => _ => controllers.routes.IndexController.onPageLoad
  }

  private val checkRouteMap: Page => Int => UserAnswers => UserAnswers => Call = {
    case TotalIncomePage =>
      taxYear => previousAnswers => userAnswers => totalIncomeCheckModeNavigation(taxYear, previousAnswers, userAnswers)
    case SelectIncomeCountryPage(_) =>
      taxYear => _ => _ => CountriesRentedPropertyController.onPageLoad(taxYear, NormalMode)
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
    case ClaimForeignTaxCreditReliefPage(countryCode) =>
      taxYear => _ => _ => ForeignTaxCheckYourAnswersController.onPageLoad(taxYear, countryCode)
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

  private def foreignReversePremiumReceivedNavigation(
    taxYear: Int,
    countryCode: String,
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(ForeignReversePremiumsReceivedPage(countryCode)) match {
      // TODO should go to the other income from property page
      case Some(ReversePremiumsReceived(true, _)) =>
        ForeignReversePremiumsReceivedController
          .onPageLoad(taxYear, NormalMode, countryCode)
      // TODO should go to the other income from property page
      case _ =>
        ForeignReversePremiumsReceivedController
          .onPageLoad(taxYear, NormalMode, countryCode)

    }

  private def totalIncomeCheckModeNavigation(taxYear: Int, previousAnswers: UserAnswers, userAnswers: UserAnswers) =
    (previousAnswers.get(TotalIncomePage), userAnswers.get(TotalIncomePage)) match {
      case (Some(LessThanOneThousand), Some(OneThousandAndMore)) |
          (Some(OneThousandAndMore), Some(LessThanOneThousand)) =>
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
        controllers.foreign.routes.ClaimForeignTaxCreditReliefController.onPageLoad(taxYear, countryCode, mode)
      case _ =>
        ForeignTaxCheckYourAnswersController.onSubmit(taxYear, countryCode)
    }
}
