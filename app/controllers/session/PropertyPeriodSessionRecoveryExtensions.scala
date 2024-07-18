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

import audit._
import models._
import pages._
import pages.adjustments._
import pages.allowances._
import pages.enhancedstructuresbuildingallowance._
import pages.premiumlease._
import pages.propertyrentals.expenses._
import pages.propertyrentals.income._
import pages.propertyrentals.{ClaimPropertyIncomeAllowancePage, ExpensesLessThan1000Page}
import pages.structurebuildingallowance._
import pages.ukrentaroom.adjustments.RaRBalancingChargePage
import pages.ukrentaroom.allowances._
import pages.ukrentaroom.{ClaimExpensesOrRRRPage, JointlyLetPage, TotalIncomeAmountPage}
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
        ua2 <- updatePropertyAboutPages(ua1, fetchedData.propertyAbout)
        ua3 <- updatePropertyRentalsAboutPages(ua2, fetchedData.propertyRentalsAbout)
        ua4 <- updateAdjustmentsPages(ua3, fetchedData.adjustments)
        ua5 <- updateAllowancesPages(ua4, fetchedData.allowances)
        ua6 <-
          updateStructureBuildingPages(ua5, fetchedData.sbasWithSupportingQuestions)

        ua7 <-
          updateEnhancedStructureBuildingPages(
            ua6,
            fetchedData.esbasWithSupportingQuestions
          )

        ua8  <- updatePropertyRentalsIncomePages(ua7, fetchedData.propertyRentalsIncome)
        ua9  <- updatePropertyRentalsExpensesPages(ua8, fetchedData.propertyRentalsExpenses)
        ua10 <- updateRentARoomAbout(ua9, fetchedData.raRAbout)
        ua11 <- updateRentARoomAllowance(ua10, fetchedData.rentARoomAllowances)
        ua12 <- updateRentARoomAdjustments(ua11, fetchedData.raRAdjustments)
      } yield ua12
    }.getOrElse(userAnswersArg)

    def updatePropertyAboutPages(
      userAnswers: UserAnswers,
      maybePropertyAbout: Option[PropertyAbout]
    ): Try[UserAnswers] =
      maybePropertyAbout match {
        case None => Success(userAnswers)
        case Some(propertyAbout) =>
          for {
            ua1 <- userAnswers.set(UKPropertyPage, propertyAbout.ukProperty.toSet)
            ua2 <- ua1.set(TotalIncomePage, propertyAbout.totalIncome)
            ua3 <- updatePart(ua2, ReportPropertyIncomePage, propertyAbout.reportPropertyIncome)
          } yield ua3
      }

    def updatePropertyRentalsAboutPages(
      userAnswers: UserAnswers,
      maybePropertyRentalsAbout: Option[RentalsAbout]
    ): Try[UserAnswers] =
      maybePropertyRentalsAbout match {
        case None => Success(userAnswers)
        case Some(propertyRentalsAbout) =>
          for {
            ua1 <- userAnswers.set(ExpensesLessThan1000Page, propertyRentalsAbout.toexpensesLessThan1000)
            ua2 <- ua1.set(
                     ClaimPropertyIncomeAllowancePage(Rentals),
                     propertyRentalsAbout.claimPropertyIncomeAllowanceYesOrNo
                   )
          } yield ua2
      }

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
      maybePropertyRentalsIncome: Option[RentalsIncome]
    ): Try[UserAnswers] =
      maybePropertyRentalsIncome match {
        case None => Success(userAnswers)
        case Some(propertyRentalsIncome) =>
          for {
            ua1 <-
              userAnswers.set(IncomeFromPropertyPage(Rentals), propertyRentalsIncome.incomeFromPropertyRentals)

            ua2 <-
              ua1.set(IsNonUKLandlordPage(Rentals), propertyRentalsIncome.isNonUKLandlord)

            ua3 <-
              ua2.set(OtherIncomeFromPropertyPage(Rentals), propertyRentalsIncome.otherIncomeFromProperty)

            ua4 <-
              propertyRentalsIncome.deductingTax.fold(Try(ua3))(dt => ua3.set(DeductingTaxPage(Rentals), dt))

            ua5 <-
              propertyRentalsIncome.receivedGrantLeaseAmount.fold(Try(ua4))(rgla =>
                ua4.set(ReceivedGrantLeaseAmountPage(Rentals), rgla)
              )

            ua6 <- propertyRentalsIncome.premiumsGrantLease.fold(Try(ua5))(pgl =>
                     ua5.set(PremiumsGrantLeasePage(Rentals), pgl)
                   )
            ua7 <- propertyRentalsIncome.yearLeaseAmount.fold(Try(ua6))(yla =>
                     ua6.set(YearLeaseAmountPage(Rentals), yla.toInt)
                   ) // Recheck
            ua8 <- propertyRentalsIncome.calculatedFigureYourself.fold(Try(ua7))(cfy =>
                     ua7.set(CalculatedFigureYourselfPage(Rentals), cfy)
                   )
            ua9 <- propertyRentalsIncome.reversePremiumsReceived.fold(Try(ua8))(rpr =>
                     ua8.set(ReversePremiumsReceivedPage(Rentals), rpr)
                   )
          } yield ua9
      }

    def updatePropertyRentalsExpensesPages(
      userAnswers: UserAnswers,
      maybePropertyRentalsExpenses: Option[RentalsExpense]
    ): Try[UserAnswers] =
      maybePropertyRentalsExpenses match {
        case None => Success(userAnswers)
        case Some(propertyRentalsExpenses) =>
          for {
            ua1 <- propertyRentalsExpenses.loanInterestOrOtherFinancialCost.fold(Try(userAnswers))(r =>
                     userAnswers.set(LoanInterestPage(Rentals), r)
                   )
            ua2 <-
              propertyRentalsExpenses.consolidatedExpenses.fold(Try(ua1))(r =>
                ua1.set(ConsolidatedExpensesPage(Rentals), r)
              )
            ua3 <- propertyRentalsExpenses.otherAllowablePropertyExpenses.fold(Try(ua2))(r =>
                     ua2.set(OtherAllowablePropertyExpensesPage(Rentals), r)
                   )
            ua4 <- propertyRentalsExpenses.propertyBusinessTravelCosts.fold(Try(ua3))(r =>
                     ua3.set(PropertyBusinessTravelCostsPage(Rentals), r)
                   )
            ua5 <- propertyRentalsExpenses.costsOfServicesProvided.fold(Try(ua4))(r =>
                     ua4.set(CostsOfServicesProvidedPage(Rentals), r)
                   )
            ua6 <-
              propertyRentalsExpenses.otherProfessionalFees.fold(Try(ua5))(r =>
                ua5.set(OtherProfessionalFeesPage(Rentals), r)
              )
            ua7 <-
              propertyRentalsExpenses.rentsRatesAndInsurance.fold(Try(ua6))(r =>
                ua6.set(RentsRatesAndInsurancePage(Rentals), r)
              )
            ua8 <-
              propertyRentalsExpenses.repairsAndMaintenanceCosts.fold(Try(ua7))(r =>
                ua7.set(RepairsAndMaintenanceCostsPage(Rentals), r)
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
            ua3 <- updateAllSbas(ua1, sbasWithSupportingQuestions.sbas)
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

    def updateRentARoomAbout(userAnswers: UserAnswers, maybeRentARoomAbout: Option[RaRAbout]): Try[UserAnswers] =
      maybeRentARoomAbout match {
        case None => Success(userAnswers)
        case Some(raRAbout) =>
          for {
            ua1 <- userAnswers.set(JointlyLetPage(RentARoom), raRAbout.jointlyLetYesOrNo)
            ua2 <- ua1.set(TotalIncomeAmountPage(RentARoom), raRAbout.totalIncomeAmount)
            ua3 <- ua2.set(ClaimExpensesOrRRRPage(RentARoom), raRAbout.claimExpensesOrRRR)
          } yield ua3
      }

    def updateRentARoomAllowance(
      userAnswers: UserAnswers,
      maybeRentARoomAllowance: Option[RentARoomAllowance]
    ): Try[UserAnswers] =
      maybeRentARoomAllowance match {
        case None => Success(userAnswers)
        case Some(rentARoomAllowance) =>
          for {
            ua1 <- rentARoomAllowance.capitalAllowancesForACar.fold[Try[UserAnswers]](Success(userAnswers))(cafac =>
                     userAnswers.set(RaRCapitalAllowancesForACarPage, cafac)
                   )
            ua2 <- rentARoomAllowance.annualInvestmentAllowance.fold[Try[UserAnswers]](Success(ua1))(aia =>
                     ua1.set(RaRAnnualInvestmentAllowancePage, aia)
                   )
            ua3 <- rentARoomAllowance.electricChargePointAllowance.fold[Try[UserAnswers]](Success(ua2))(ecpa =>
                     ua2.set(RaRElectricChargePointAllowanceForAnEVPage, ecpa)
                   )
            ua4 <- rentARoomAllowance.zeroEmissionCarAllowance.fold[Try[UserAnswers]](Success(ua3))(zeca =>
                     ua3.set(RaRZeroEmissionCarAllowancePage, zeca)
                   )
            ua5 <- rentARoomAllowance.zeroEmissionGoodsVehicleAllowance.fold[Try[UserAnswers]](Success(ua4))(zegva =>
                     ua4.set(RaRZeroEmissionGoodsVehicleAllowancePage, zegva)
                   )
            ua6 <- rentARoomAllowance.replacementOfDomesticGoodsAllowance.fold[Try[UserAnswers]](Success(ua5))(rodga =>
                     ua5.set(RaRReplacementsOfDomesticGoodsPage, rodga)
                   )
            ua7 <- rentARoomAllowance.otherCapitalAllowance.fold[Try[UserAnswers]](Success(ua6))(oca =>
                     ua6.set(RaROtherCapitalAllowancesPage, oca)
                   )
          } yield ua7
      }

    def updateRentARoomAdjustments(
      userAnswers: UserAnswers,
      maybeRentARoomAdjustments: Option[RentARoomAdjustments]
    ): Try[UserAnswers] =
      maybeRentARoomAdjustments match {
        case None                       => Success(userAnswers)
        case Some(rentARoomAdjustments) => userAnswers.set(RaRBalancingChargePage, rentARoomAdjustments.balancingCharge)
      }
  }
}
