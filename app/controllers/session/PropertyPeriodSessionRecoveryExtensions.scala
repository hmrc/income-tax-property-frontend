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

package controllers.session

import audit.{PropertyRentalsExpense, PropertyRentalsIncome}
import models.{Adjustments, Allowances, EsbasWithSupportingQuestions, FetchedBackendData, Sba, SbasWithSupportingQuestions, UserAnswers}
import pages._
import pages.adjustments._
import pages.allowances._
import pages.enhancedstructuresbuildingallowance._
import pages.premiumlease.{CalculatedFigureYourselfPage, PremiumsGrantLeasePage, ReceivedGrantLeaseAmountPage, YearLeaseAmountPage}
import pages.propertyrentals.expenses._
import pages.propertyrentals.income._
import pages.structurebuildingallowance._
import play.api.libs.json.Writes
import queries.Settable

import scala.util.{Success, Try}

object PropertyPeriodSessionRecoveryExtensions {

  implicit class UserAnswersExtension(userAnswersArg: UserAnswers) {
    def updatePart[T](userAnswers: UserAnswers, page: Settable[T], value: Option[T])(implicit
      writes: Writes[T]
    ): Try[UserAnswers] =
      value.fold[Try[UserAnswers]](Success(userAnswers))(v => userAnswers.set(page, v))

    def update(fetchedData: FetchedBackendData): UserAnswers = {
      for {
        ua1 <- updatePart(userAnswersArg, CapitalAllowancesForACarPage, fetchedData.capitalAllowancesForACar)
        ua2 <- updatePart(ua1, UKPropertyPage, fetchedData.propertyAbout.map(_.ukProperty.toSet))
        ua3 <- updatePart(ua2, TotalIncomePage, fetchedData.propertyAbout.map(_.totalIncome))
        ua5 <- updateAdjustmentsPages(ua3, fetchedData.adjustments)
        ua6 <- updateAllowancesPages(ua5, fetchedData.allowances)
        ua7 <-
          updateStructureBuildingPages(ua6, fetchedData.sbasWithSupportingQuestions)

        ua8 <-
          updateEnhancedStructureBuildingPages(
            ua7,
            fetchedData.esbasWithSupportingQuestions
          )

        ua9  <- updatePropertyRentalsIncomePages(ua8, fetchedData.propertyRentalsIncome)
        ua10 <- updatePropertyRentalsExpensesPages(ua9, fetchedData.propertyRentalsExpenses)
      } yield ua10
    }.getOrElse(userAnswersArg)

    def updateAdjustmentsPages(userAnswers: UserAnswers, maybeAdjustments: Option[Adjustments]): Try[UserAnswers] =
      maybeAdjustments match {
        case None => Success(userAnswers)
        case Some(adjustments) =>
          for {
            ua1 <- userAnswers.set(BalancingChargePage, adjustments.balancingCharge)
            ua2 <- ua1.set(PrivateUseAdjustmentPage, adjustments.privateUseAdjustment)
            ua3 <- ua2.set(PropertyIncomeAllowancePage, adjustments.propertyIncomeAllowance)
            ua4 <- ua3.set(RenovationAllowanceBalancingChargePage, adjustments.renovationAllowanceBalancingCharge)
            ua5 <- ua4.set(ResidentialFinanceCostPage, adjustments.residentialFinanceCost)
            ua6 <- ua5.set(UnusedResidentialFinanceCostPage, adjustments.unusedResidentialFinanceCost)
          } yield ua6
      }

    def updateAllowancesPages(userAnswers: UserAnswers, maybeAllowances: Option[Allowances]): Try[UserAnswers] =
      maybeAllowances match {
        case None => Success(userAnswers)
        case Some(allowances) =>
          for {
            ua1 <- userAnswers.set(AnnualInvestmentAllowancePage, allowances.annualInvestmentAllowance)
            ua2 <- ua1.set(BusinessPremisesRenovationPage, allowances.businessPremisesRenovationAllowance)
            ua3 <- ua2.set(ElectricChargePointAllowancePage, allowances.electricChargePointAllowance)
            ua4 <- ua3.set(OtherCapitalAllowancePage, allowances.otherCapitalAllowance)
            ua5 <- ua4.set(ReplacementOfDomesticGoodsPage, allowances.replacementOfDomesticGoodsAllowance)
            ua6 <- ua5.set(ZeroEmissionCarAllowancePage, allowances.zeroEmissionCarAllowance)
            ua7 <- ua6.set(ZeroEmissionGoodsVehicleAllowancePage, allowances.zeroEmissionGoodsVehicleAllowance)
          } yield ua7
      }

    def updatePropertyRentalsIncomePages(
      userAnswers: UserAnswers,
      maybePropertyRentalsIncome: Option[PropertyRentalsIncome]
    ): Try[UserAnswers] =
      maybePropertyRentalsIncome match {
        case None => Success(userAnswers)
        case Some(propertyRentalsIncome) =>
          for {
            ua1 <-
              userAnswers.set(IncomeFromPropertyRentalsPage, propertyRentalsIncome.incomeFromPropertyRentals)

            ua2 <-
              ua1.set(IsNonUKLandlordPage, propertyRentalsIncome.isNonUKLandlord)

            ua3 <-
              ua2.set(OtherIncomeFromPropertyPage, propertyRentalsIncome.otherIncomeFromProperty)

            ua4 <-
              propertyRentalsIncome.deductingTax.fold(Try(ua3))(dt => ua3.set(DeductingTaxPage, dt))

            ua5 <-
              propertyRentalsIncome.receivedGrantLeaseAmount.fold(Try(ua4))(rgla =>
                ua4.set(ReceivedGrantLeaseAmountPage, rgla)
              )

            ua6 <- propertyRentalsIncome.premiumsGrantLease.fold(Try(ua5))(pgl => ua5.set(PremiumsGrantLeasePage, pgl))
            ua7 <- propertyRentalsIncome.yearLeaseAmount.fold(Try(ua6))(yla =>
                     ua6.set(YearLeaseAmountPage, yla.toInt)
                   ) // Recheck
            ua8 <- propertyRentalsIncome.calculatedFigureYourself.fold(Try(ua7))(cfy =>
                     ua7.set(CalculatedFigureYourselfPage, cfy)
                   )
            ua9 <- propertyRentalsIncome.reversePremiumsReceived.fold(Try(ua8))(rpr =>
                     ua8.set(ReversePremiumsReceivedPage, rpr)
                   )
          } yield ua9
      }

    def updatePropertyRentalsExpensesPages(
      userAnswers: UserAnswers,
      maybePropertyRentalsExpenses: Option[PropertyRentalsExpense]
    ): Try[UserAnswers] =
      maybePropertyRentalsExpenses match {
        case None => Success(userAnswers)
        case Some(propertyRentalsExpenses) =>
          for {
            ua1 <- propertyRentalsExpenses.loanInterestOrOtherFinancialCost.fold(Try(userAnswers))(r =>
                     userAnswers.set(LoanInterestPage, r)
                   )
            ua2 <-
              propertyRentalsExpenses.consolidatedExpenses.fold(Try(ua1))(r => ua1.set(ConsolidatedExpensesPage, r))
            ua3 <- propertyRentalsExpenses.otherAllowablePropertyExpenses.fold(Try(ua2))(r =>
                     ua2.set(OtherAllowablePropertyExpensesPage, r)
                   )
            ua4 <- propertyRentalsExpenses.propertyBusinessTravelCosts.fold(Try(ua3))(r =>
                     ua3.set(PropertyBusinessTravelCostsPage, r)
                   )
            ua5 <- propertyRentalsExpenses.costsOfServicesProvided.fold(Try(ua4))(r =>
                     ua4.set(CostsOfServicesProvidedPage, r)
                   )
            ua6 <-
              propertyRentalsExpenses.otherProfessionalFees.fold(Try(ua5))(r => ua5.set(OtherProfessionalFeesPage, r))
            ua7 <-
              propertyRentalsExpenses.rentsRatesAndInsurance.fold(Try(ua6))(r => ua6.set(RentsRatesAndInsurancePage, r))
            ua8 <-
              propertyRentalsExpenses.repairsAndMaintenanceCosts.fold(Try(ua7))(r =>
                ua7.set(RepairsAndMaintenanceCostsPage, r)
              )
          } yield ua8
      }

    def updateEnhancedStructureBuildingPages(
      userAnswers: UserAnswers,
      maybeEsbasWithSupportingQuestions: Option[EsbasWithSupportingQuestions]
    ): Try[UserAnswers] =
      maybeEsbasWithSupportingQuestions match {
        case None => Success(userAnswers)
        case Some(esbasWithSupportingQuestions) =>
          for {
            ua1 <- userAnswers.set(ClaimEsbaPage, esbasWithSupportingQuestions.claimEnhancedStructureBuildingAllowance)
            ua2 <- ua1.set(EsbaClaimsPage, esbasWithSupportingQuestions.esbaClaims.getOrElse(false))
            ua3 <- updateAllEsbas(ua2, esbasWithSupportingQuestions.esbas)
          } yield ua3
      }

    def updateEsba(userAnswers: UserAnswers, index: Int, esba: Esba): Try[UserAnswers] =
      for {
        ua1 <- userAnswers.set(EsbaAddressPage(index), esba.esbaAddress)
        ua2 <- ua1.set(EsbaQualifyingDatePage(index), esba.esbaQualifyingDate)
        ua3 <- ua2.set(EsbaQualifyingAmountPage(index), esba.esbaQualifyingAmount)
        ua4 <- ua3.set(EsbaClaimPage(index), esba.esbaClaim)
      } yield ua4

    def updateAllEsbas(userAnswers: UserAnswers, fetchedData: List[Esba]): Try[UserAnswers] =
      fetchedData.zipWithIndex.foldLeft(Try(userAnswers)) { (acc, a) =>
        val (esba, index) = a
        acc.flatMap(ua => updateEsba(ua, index, esba))
      }

    def updateStructureBuildingPages(
      userAnswers: UserAnswers,
      maybeSbasWithSupportingQuestions: Option[SbasWithSupportingQuestions]
    ): Try[UserAnswers] =
      maybeSbasWithSupportingQuestions match {
        case None => Success(userAnswers)
        case Some(sbasWithSupportingQuestions) =>
          for {
            ua1 <- userAnswers.set(
                     ClaimStructureBuildingAllowancePage,
                     sbasWithSupportingQuestions.claimStructureBuildingAllowance
                   )
            //ua2 <- ua1.set(SbaClaimsPage, sbasWithSupportingQuestions.sbaClaims)
            ua3 <- updateAllSbas(ua1, sbasWithSupportingQuestions.structureBuildingFormGroup)
          } yield ua3
      }

    def updateAllSbas(userAnswers: UserAnswers, fetchedData: List[Sba]): Try[UserAnswers] =
      fetchedData.zipWithIndex.foldLeft(Try(userAnswers)) { (acc, a) =>
        val (sba, index) = a
        acc.flatMap(ua => updateSba(ua, index, sba))
      }

    def updateSba(userAnswers: UserAnswers, index: Int, sba: Sba): Try[UserAnswers] =
      for {
        ua1 <- userAnswers.set(StructuredBuildingAllowanceAddressPage(index), sba.structuredBuildingAllowanceAddress)
        ua2 <- ua1.set(StructureBuildingQualifyingDatePage(index), sba.structureBuildingQualifyingDate)
        ua3 <- ua2.set(StructureBuildingQualifyingAmountPage(index), sba.structureBuildingQualifyingAmount)
        ua4 <- ua3.set(StructureBuildingAllowanceClaimPage(index), sba.structureBuildingAllowanceClaim)
      } yield ua4

  }
}
