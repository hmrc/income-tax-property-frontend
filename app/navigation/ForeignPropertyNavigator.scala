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
import controllers.foreign.adjustments.routes._
import controllers.foreign.allowances.routes._
import controllers.foreign.expenses.routes._
import controllers.foreign.income.routes._
import controllers.foreign.routes._
import controllers.foreign.structuresbuildingallowance.routes._
import controllers.routes.{IndexController, SummaryController}
import models.TotalIncome.{Between, Over, Under}
import models._
import pages.Page
import pages.foreign._
import pages.foreign.adjustments._
import pages.foreign.allowances._
import pages.foreign.expenses._
import pages.foreign.income._
import pages.foreign.structurebuildingallowance._
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
      taxYear => _ => _ => TwelveMonthPeriodsInLeaseController.onPageLoad(taxYear, countryCode, NormalMode)
    case TwelveMonthPeriodsInLeasePage(countryCode) =>
      taxYear => _ => _ => ForeignPremiumsGrantLeaseController.onPageLoad(taxYear, countryCode, NormalMode)
    case ForeignPremiumsGrantLeasePage(countryCode) =>
      taxYear => _ => _ => ForeignOtherIncomeFromPropertyController.onPageLoad(taxYear, countryCode, NormalMode)
    case ForeignOtherIncomeFromPropertyPage(countryCode) =>
      taxYear => _ => _ => ForeignIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignIncomeSectionCompletePage(_) =>
      taxYear =>
        _ =>
          _ =>
            SummaryController.show(taxYear)
        // Expenses
    case ForeignRentsRatesAndInsurancePage(countryCode) =>
      taxYear => _ => _ => ForeignPropertyRepairsAndMaintenanceController.onPageLoad(taxYear, countryCode, NormalMode)
    case ForeignPropertyRepairsAndMaintenancePage(countryCode) =>
      taxYear =>
        _ => _ => ForeignNonResidentialPropertyFinanceCostsController.onPageLoad(taxYear, countryCode, NormalMode)
    case ForeignNonResidentialPropertyFinanceCostsPage(countryCode) =>
      taxYear => _ => _ => ForeignProfessionalFeesController.onPageLoad(taxYear, countryCode, NormalMode)
    case ForeignProfessionalFeesPage(countryCode) =>
      taxYear => _ => _ => ForeignCostsOfServicesProvidedController.onPageLoad(taxYear, countryCode, NormalMode)
    case ForeignCostsOfServicesProvidedPage(countryCode) =>
      taxYear => _ => _ => ForeignOtherAllowablePropertyExpensesController.onPageLoad(taxYear, countryCode, NormalMode)
    case ForeignOtherAllowablePropertyExpensesPage(countryCode) =>
      taxYear => _ => _ => ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ConsolidatedOrIndividualExpensesPage(countryCode) =>
      taxYear => _ => userAnswers => consolidatedExpensesNavigation(taxYear, userAnswers, countryCode)
    case ForeignExpensesSectionCompletePage(_) =>
      taxYear =>
        _ =>
          _ =>
            SummaryController.show(taxYear)

        // Allowances
    case ForeignZeroEmissionCarAllowancePage(countryCode) =>
      taxYear => _ => _ => ForeignZeroEmissionGoodsVehiclesController.onPageLoad(taxYear, countryCode, NormalMode)
    case ForeignZeroEmissionGoodsVehiclesPage(countryCode) =>
      taxYear => _ => _ => ForeignReplacementOfDomesticGoodsController.onPageLoad(taxYear, countryCode, NormalMode)
    case ForeignReplacementOfDomesticGoodsPage(countryCode) =>
      taxYear => _ => _ => ForeignOtherCapitalAllowancesController.onPageLoad(taxYear, countryCode, NormalMode)
    case ForeignOtherCapitalAllowancesPage(countryCode) => // TODO route to CYA page once created
      taxYear => _ => _ => ForeignAllowancesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignCapitalAllowancesForACarPage(countryCode) =>
      taxYear => _ => _ => ForeignAllowancesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignAllowancesCompletePage(_) =>
      taxYear =>
        _ =>
          _ =>
            SummaryController.show(taxYear)
        // Allowances // Structure Building Allowance
    case ForeignClaimStructureBuildingAllowancePage(countryCode) =>
      taxYear => _ => userAnswers => foreignSbaNavigation(taxYear, userAnswers, countryCode)
    case ForeignStructureBuildingQualifyingDatePage(countryCode, index) =>
      taxYear =>
        _ => _ => ForeignStructureBuildingQualifyingAmountController.onPageLoad(taxYear, countryCode, index, NormalMode)
    case ForeignStructureBuildingQualifyingAmountPage(countryCode, index) =>
      taxYear =>
        _ => _ => ForeignStructureBuildingAllowanceClaimController.onPageLoad(taxYear, countryCode, index, NormalMode)
    case ForeignStructureBuildingAllowanceClaimPage(countryCode, index) =>
      taxYear =>
        _ =>
          _ => ForeignStructuresBuildingAllowanceAddressController.onPageLoad(taxYear, index, countryCode, NormalMode)
    case ForeignStructuresBuildingAllowanceAddressPage(index, countryCode) =>
      taxYear => _ => _ => ForeignSbaCheckYourAnswersController.onPageLoad(taxYear, countryCode, index)
    case ForeignSbaRemoveConfirmationPage(countryCode) =>
      taxYear => _ => userAnswers => foreignSbaRemoveConfirmationNavigationNormalMode(taxYear, userAnswers, countryCode)
    case ForeignSbaCompletePage(_) =>
      taxYear =>
        _ =>
          _ =>
            SummaryController.show(taxYear)

        // Adjustments
    case ForeignPrivateUseAdjustmentPage(countryCode) =>
      taxYear => _ => _ => ForeignBalancingChargeController.onPageLoad(taxYear, countryCode, NormalMode)
    case ForeignBalancingChargePage(countryCode) =>
      taxYear =>
        _ =>
          userAnswers =>
            if (userAnswers.get(ClaimPropertyIncomeAllowanceOrExpensesPage).getOrElse(false)) {
              PropertyIncomeAllowanceClaimController.onPageLoad(taxYear, countryCode, NormalMode)
            } else {
              ForeignResidentialFinanceCostsController.onPageLoad(taxYear, countryCode, NormalMode)
            }
    case PropertyIncomeAllowanceClaimPage(countryCode) =>
      taxYear => _ => _ => ForeignUnusedLossesPreviousYearsController.onPageLoad(taxYear, countryCode, NormalMode)
    case ForeignResidentialFinanceCostsPage(countryCode) =>
      taxYear => _ => _ => ForeignUnusedResidentialFinanceCostController.onPageLoad(taxYear, countryCode, NormalMode)
    case ForeignUnusedResidentialFinanceCostPage(countryCode) =>
      taxYear => _ => _ => ForeignUnusedLossesPreviousYearsController.onPageLoad(taxYear, countryCode, NormalMode)
    case ForeignUnusedLossesPreviousYearsPage(countryCode) =>
      taxYear => _ => userAnswers => unusedLossesNavigationNormalMode(taxYear, countryCode, userAnswers)
    case ForeignWhenYouReportedTheLossPage(countryCode) =>
      taxYear => _ => _ => ForeignAdjustmentsCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignAdjustmentsCompletePage(_) =>
      taxYear => _ => _ => SummaryController.show(taxYear)

    case _ => _ => _ => _ => controllers.routes.IndexController.onPageLoad
  }

  private def foreignSbaRemoveConfirmationNavigationNormalMode(
    taxYear: Int,
    userAnswers: UserAnswers,
    countryCode: String
  ): Call = (
    userAnswers.get(ForeignSbaRemoveConfirmationPage(countryCode)),
    userAnswers.get(ForeignStructureBuildingAllowanceGroup(countryCode))
  ) match {
    case (Some(true), Some(sbaForm)) if sbaForm.isEmpty =>
      ForeignClaimStructureBuildingAllowanceController.onPageLoad(taxYear, countryCode, NormalMode)
    case (_, Some(sbaForm)) if sbaForm.nonEmpty =>
      ForeignStructureBuildingAllowanceClaimsController.onPageLoad(taxYear, countryCode)
    case (_, _) => SummaryController.show(taxYear)
  }

  private def consolidatedExpensesNavigation(taxYear: Int, userAnswers: UserAnswers, countryCode: String): Call =
    userAnswers.get(ConsolidatedOrIndividualExpensesPage(countryCode)) match {
      case Some(ConsolidatedOrIndividualExpenses(true, _)) =>
        ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
      case Some(ConsolidatedOrIndividualExpenses(false, _)) =>
        ForeignRentsRatesAndInsuranceController.onPageLoad(taxYear, countryCode, NormalMode)
      case _ => IndexController.onPageLoad
    }

  private def consolidatedExpensesNavigationCheckMode(
    taxYear: Int,
    userAnswers: UserAnswers,
    countryCode: String
  ): Call =
    userAnswers.get(ConsolidatedOrIndividualExpensesPage(countryCode)) match {
      case Some(ConsolidatedOrIndividualExpenses(false, _)) =>
        ForeignRentsRatesAndInsuranceController.onPageLoad(taxYear, countryCode, NormalMode)
      case _ => ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
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
    case ForeignPropertyRentalIncomePage(countryCode) =>
      taxYear => _ => _ => ForeignIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case PremiumsGrantLeaseYNPage(countryCode) =>
      taxYear => _ => userAnswers => incomePremiumForGrantOfLeaseNavigationCheckMode(taxYear, countryCode, userAnswers)
    case CalculatedPremiumLeaseTaxablePage(countryCode) =>
      taxYear =>
        _ => userAnswers => incomeCalculatePremiumLeaseTaxableNavigationCheckMode(taxYear, countryCode, userAnswers)
    case ForeignReceivedGrantLeaseAmountPage(countryCode) =>
      taxYear => _ => _ => TwelveMonthPeriodsInLeaseController.onPageLoad(taxYear, countryCode, CheckMode)
    case TwelveMonthPeriodsInLeasePage(countryCode) =>
      taxYear => _ => _ => ForeignPremiumsGrantLeaseController.onPageLoad(taxYear, countryCode, CheckMode)
    case ForeignPremiumsGrantLeasePage(countryCode) =>
      taxYear => _ => _ => ForeignIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignOtherIncomeFromPropertyPage(countryCode) =>
      taxYear => _ => _ => ForeignIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ClaimForeignTaxCreditReliefPage(countryCode) =>
      taxYear => _ => _ => ForeignTaxCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignRentsRatesAndInsurancePage(countryCode) =>
      taxYear => _ => _ => ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignPropertyRepairsAndMaintenancePage(countryCode) =>
      taxYear => _ => _ => ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignNonResidentialPropertyFinanceCostsPage(countryCode) =>
      taxYear => _ => _ => ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignProfessionalFeesPage(countryCode) =>
      taxYear => _ => _ => ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignCostsOfServicesProvidedPage(countryCode) =>
      taxYear => _ => _ => ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignOtherAllowablePropertyExpensesPage(countryCode) =>
      taxYear => _ => _ => ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ConsolidatedOrIndividualExpensesPage(countryCode) =>
      taxYear =>
        _ =>
          userAnswers =>
            consolidatedExpensesNavigationCheckMode(taxYear, userAnswers, countryCode)
          // Allowances
        // TODO route to CYA page once created
    case ForeignZeroEmissionCarAllowancePage(countryCode) =>
      taxYear => _ => _ => ForeignAllowancesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignZeroEmissionGoodsVehiclesPage(countryCode) =>
      taxYear => _ => _ => ForeignAllowancesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignReplacementOfDomesticGoodsPage(countryCode) =>
      taxYear => _ => _ => ForeignAllowancesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignOtherCapitalAllowancesPage(countryCode) =>
      taxYear =>
        _ =>
          _ =>
            ForeignAllowancesCheckYourAnswersController.onPageLoad(taxYear, countryCode)

    case ForeignCapitalAllowancesForACarPage(countryCode) =>
      taxYear => _ => _ => ForeignAllowancesCheckYourAnswersController.onPageLoad(taxYear, countryCode)
        // Allowances // Structure Building Allowance
    case ForeignClaimStructureBuildingAllowancePage(countryCode) =>
      taxYear => _ => userAnswers => foreignSbaNavigation(taxYear, userAnswers, countryCode)
    case ForeignStructureBuildingQualifyingDatePage(countryCode, index) =>
      taxYear => _ => _ => ForeignSbaCheckYourAnswersController.onPageLoad(taxYear, countryCode, index)
    case ForeignStructureBuildingQualifyingAmountPage(countryCode, index) =>
      taxYear => _ => _ => ForeignSbaCheckYourAnswersController.onPageLoad(taxYear, countryCode, index)
    case ForeignStructureBuildingAllowanceClaimPage(countryCode, index) =>
      taxYear => _ => _ => ForeignSbaCheckYourAnswersController.onPageLoad(taxYear, countryCode, index)
    case ForeignStructuresBuildingAllowanceAddressPage(index, countryCode) =>
      taxYear => _ => _ => ForeignSbaCheckYourAnswersController.onPageLoad(taxYear, countryCode, index)
      // Adjustments
    case ForeignPrivateUseAdjustmentPage(countryCode) =>
      taxYear => _ => _ => ForeignAdjustmentsCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignBalancingChargePage(countryCode) =>
      taxYear => _ => _ => ForeignAdjustmentsCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case PropertyIncomeAllowanceClaimPage(countryCode) =>
      taxYear => _ => _ => ForeignAdjustmentsCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignResidentialFinanceCostsPage(countryCode) =>
      taxYear => _ => _ => ForeignAdjustmentsCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignUnusedResidentialFinanceCostPage(countryCode) =>
      taxYear => _ => _ => ForeignAdjustmentsCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignUnusedLossesPreviousYearsPage(countryCode) =>
      taxYear => previousAnswers => userAnswers => unusedLossesNavigationCheckMode(taxYear, countryCode, previousAnswers, userAnswers)
    case ForeignWhenYouReportedTheLossPage(countryCode) =>
      taxYear => _ => _ => ForeignAdjustmentsCheckYourAnswersController.onPageLoad(taxYear, countryCode)

    case _ => _ => _ => _ => controllers.routes.IndexController.onPageLoad
  }

  def nextPage(page: Page, taxYear: Int, mode: Mode, previousUserAnswers: UserAnswers, userAnswers: UserAnswers): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(taxYear)(previousUserAnswers)(userAnswers)
      case CheckMode =>
        checkRouteMap(page)(taxYear)(previousUserAnswers)(userAnswers)
    }

  private def unusedLossesNavigationNormalMode(
    taxYear: Int,
    countryCode: String,
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(ForeignUnusedLossesPreviousYearsPage(countryCode)) match {
      case Some(UnusedLossesPreviousYears(true, _)) =>
        ForeignWhenYouReportedTheLossController.onPageLoad(taxYear, countryCode, NormalMode)
      case Some(UnusedLossesPreviousYears(false, _)) =>
        ForeignAdjustmentsCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    }

  private def foreignTotalIncomeNavigationNormalMode(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(TotalIncomePage) match {
      case Some(Under) => PropertyIncomeReportController.onPageLoad(taxYear, NormalMode)
      case _           => SelectIncomeCountryController.onPageLoad(taxYear, 0, NormalMode)
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
      case _          => ForeignOtherIncomeFromPropertyController.onPageLoad(taxYear, countryCode, NormalMode)
    }

  private def incomePremiumForGrantOfLeaseNavigationCheckMode(
    taxYear: Int,
    countryCode: String,
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(PremiumsGrantLeaseYNPage(countryCode)) match {
      case Some(true) => CalculatedPremiumLeaseTaxableController.onPageLoad(taxYear, countryCode, CheckMode)
      case _          => ForeignIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    }

  private def incomeCalculatePremiumLeaseTaxableNavigationNormalMode(
    taxYear: Int,
    countryCode: String,
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(CalculatedPremiumLeaseTaxablePage(countryCode)) match {
      case Some(PremiumCalculated(true, _)) =>
        ForeignOtherIncomeFromPropertyController.onPageLoad(taxYear, countryCode, NormalMode)
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
        ForeignOtherIncomeFromPropertyController.onPageLoad(taxYear, countryCode, CheckMode)
      case Some(PremiumCalculated(false, _)) =>
        ForeignReceivedGrantLeaseAmountController
          .onPageLoad(taxYear, countryCode, CheckMode)
    }

  private def totalIncomeCheckModeNavigation(
    taxYear: Int,
    previousAnswers: UserAnswers,
    userAnswers: UserAnswers
  ): Call =
    (previousAnswers.get(TotalIncomePage), userAnswers.get(TotalIncomePage)) match {
      case (Some(Under), Some(Between)) | (Some(Under), Some(Over)) | (Some(Between), Some(Over)) |
          (Some(Over), Some(Between)) =>
        SelectIncomeCountryController.onPageLoad(taxYear, 0, NormalMode)
      case (Some(Between), Some(Under)) | (Some(Over), Some(Under)) =>
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

  private def foreignSbaNavigation(
    taxYear: Int,
    userAnswers: UserAnswers,
    countryCode: String
  ): Call =
    userAnswers.get(ForeignClaimStructureBuildingAllowancePage(countryCode)) match {
      case Some(true) => ForeignAddClaimStructureBuildingAllowanceController.onPageLoad(taxYear, countryCode)
      case _          => ForeignClaimSbaCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    }

  private def unusedLossesNavigationCheckMode(
                                                taxYear: Int,
                                                countryCode: String,
                                                previousAnswers: UserAnswers,
                                                userAnswers: UserAnswers
                                              ): Call =
    (
      previousAnswers.get(ForeignUnusedLossesPreviousYearsPage(countryCode)),
      userAnswers.get(ForeignUnusedLossesPreviousYearsPage(countryCode))
    ) match {
      case (Some(UnusedLossesPreviousYears(false, _)), Some(UnusedLossesPreviousYears(true, _))) =>
        ForeignWhenYouReportedTheLossController.onPageLoad(taxYear, countryCode, NormalMode)
      case _ =>
        ForeignAdjustmentsCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    }
}
