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
import pages.propertyrentals.ClaimPropertyIncomeAllowancePage
import pages.propertyrentals.expenses._
import pages.propertyrentals.income._
import pages.structurebuildingallowance._
import pages.ukrentaroom.adjustments.RaRBalancingChargePage
import pages.ukrentaroom.allowances._
import pages.ukrentaroom.{ClaimExpensesOrReliefPage, JointlyLetPage, TotalIncomeAmountPage}
import play.api.libs.json.Writes
import queries.Settable

import scala.util.{Success, Try}

object PropertyPeriodSessionRecoveryExtensions {

  implicit class UserAnswersExtension(userAnswersArg: UserAnswers) {

    def update(fetchedData: FetchedBackendData): UserAnswers = {
      for {
        ua1 <- updatePart(userAnswersArg, CapitalAllowancesForACarPage(Rentals), fetchedData.capitalAllowancesForACar)
        ua2 <- updatePropertyAboutPages(ua1, fetchedData.propertyAbout)
        ua3 <- updatePropertyRentalsAboutPages(ua2, fetchedData.propertyRentalsAbout)
        ua4 <- updateAdjustmentsPages(ua3, fetchedData.adjustments, Rentals)
        ua5 <- updateAdjustmentsPages(ua4, fetchedData.adjustments, RentalsRentARoom)
        ua6 <- updateAllowancesPages(ua5, fetchedData.allowances, Rentals)
        ua7 <- updateAllowancesPages(ua6, fetchedData.allowances, RentalsRentARoom)
        ua8 <-
          updateStructureBuildingPages(ua7, fetchedData.rentalsSBA, Rentals)
        ua9 <-
          updateStructureBuildingPages(ua8, fetchedData.rentalsSBA, RentalsRentARoom)

        ua10 <-
          updateEnhancedStructureBuildingPages(
            ua9,
            fetchedData.esbasWithSupportingQuestions,
            Rentals
          )
        ua11 <-
          updateEnhancedStructureBuildingPages(
            ua10,
            fetchedData.esbasWithSupportingQuestions,
            RentalsRentARoom
          )
        ua12 <- updatePropertyRentalsIncomePages(ua11, fetchedData.propertyRentalsIncome)
        ua13 <- updateRentalsAndRaRIncomePages(ua12, fetchedData.rentalsAndRaRIncome)
        ua14 <- updatePropertyRentalsExpensesPages(ua13, fetchedData.propertyRentalsExpenses, Rentals)
        ua15 <- updatePropertyRentalsExpensesPages(ua14, fetchedData.propertyRentalsExpenses, RentalsRentARoom)
        ua16 <- updateRentARoomAbout(ua15, fetchedData.raRAbout)
        ua17 <- updateRentARoomAllowance(ua16, fetchedData.rentARoomAllowances)
        ua18 <- updateRentARoomAdjustments(ua17, fetchedData.raRAdjustments)
        ua19 <- updateRentalsAndRaRAbout(ua18, fetchedData.rentalsAndRaRAbout)
      } yield ua19
    }.getOrElse(userAnswersArg)

    private def updatePropertyAboutPages(
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

    private def updatePart[T](userAnswers: UserAnswers, page: Settable[T], value: Option[T])(implicit
      writes: Writes[T]
    ): Try[UserAnswers] =
      value.fold[Try[UserAnswers]](Success(userAnswers))(v => userAnswers.set(page, v))

    private def updatePropertyRentalsAboutPages(
      userAnswers: UserAnswers,
      maybePropertyRentalsAbout: Option[RentalsAbout]
    ): Try[UserAnswers] =
      maybePropertyRentalsAbout match {
        case None => Success(userAnswers)
        case Some(propertyRentalsAbout) =>
          for {
            ua2 <- userAnswers.set(
                     ClaimPropertyIncomeAllowancePage(Rentals),
                     propertyRentalsAbout.claimPropertyIncomeAllowanceYesOrNo
                   )
          } yield ua2
      }

    private def updateAdjustmentsPages(
      userAnswers: UserAnswers,
      maybeAdjustments: Option[Adjustments],
      propertyType: PropertyType
    ): Try[UserAnswers] =
      maybeAdjustments match {
        case None                           => Success(userAnswers)
        case _ if propertyType == RentARoom => Success(userAnswers)
        case Some(adjustments) =>
          for {
            ua1 <- userAnswers.set(BalancingChargePage(propertyType), adjustments.balancingCharge)
            ua2 <- ua1.set(PrivateUseAdjustmentPage(propertyType), adjustments.privateUseAdjustment)
            ua3 <- ua2.set(PropertyIncomeAllowancePage(propertyType), adjustments.propertyIncomeAllowance)
            ua4 <-
              ua3.set(
                RenovationAllowanceBalancingChargePage(propertyType),
                adjustments.renovationAllowanceBalancingCharge
              )
            ua5 <- ua4.set(ResidentialFinanceCostPage(propertyType), adjustments.residentialFinanceCost)
            ua6 <- ua5.set(UnusedResidentialFinanceCostPage(propertyType), adjustments.unusedResidentialFinanceCost)
          } yield ua6
      }

    private def updateAllowancesPages(
      userAnswers: UserAnswers,
      maybeAllowances: Option[Allowances],
      propertyType: PropertyType
    ): Try[UserAnswers] =
      maybeAllowances match {
        case None                           => Success(userAnswers)
        case _ if propertyType == RentARoom => Success(userAnswers)
        case Some(allowances) =>
          for {
            ua1 <- userAnswers.set(AnnualInvestmentAllowancePage(propertyType), allowances.annualInvestmentAllowance)
            ua2 <- ua1.set(BusinessPremisesRenovationPage(propertyType), allowances.businessPremisesRenovationAllowance)
            ua3 <- ua2.set(ElectricChargePointAllowancePage, allowances.electricChargePointAllowance)
            ua4 <- ua3.set(OtherCapitalAllowancePage(propertyType), allowances.otherCapitalAllowance)
            ua5 <- ua4.set(ReplacementOfDomesticGoodsPage(propertyType), allowances.replacementOfDomesticGoodsAllowance)
            ua6 <- ua5.set(ZeroEmissionCarAllowancePage(propertyType), allowances.zeroEmissionCarAllowance)
            ua7 <-
              ua6.set(ZeroEmissionGoodsVehicleAllowancePage(propertyType), allowances.zeroEmissionGoodsVehicleAllowance)
          } yield ua7
      }

    private def updatePropertyRentalsIncomePages(
      userAnswers: UserAnswers,
      maybePropertyRentalsIncome: Option[RentalsIncome]
    ): Try[UserAnswers] =
      maybePropertyRentalsIncome match {
        case None => Success(userAnswers)
        case Some(propertyRentalsIncome) =>
          for {
            ua1 <-
              userAnswers.set(PropertyRentalIncomePage(Rentals), propertyRentalsIncome.incomeFromPropertyRentals)

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

    private def updateRentalsAndRaRIncomePages(
      userAnswers: UserAnswers,
      maybeRentalsAndRaRIncome: Option[RentalsAndRentARoomIncome]
    ): Try[UserAnswers] =
      maybeRentalsAndRaRIncome match {
        case None => Success(userAnswers)
        case Some(rentalsAndRaRIncome) =>
          for {
            ua1 <-
              userAnswers.set(IsNonUKLandlordPage(RentalsRentARoom), rentalsAndRaRIncome.isNonUKLandlord)

            ua2 <-
              ua1.set(OtherIncomeFromPropertyPage(RentalsRentARoom), rentalsAndRaRIncome.otherIncomeFromProperty)

            ua3 <-
              rentalsAndRaRIncome.deductingTax.fold(Try(ua2))(dt => ua2.set(DeductingTaxPage(RentalsRentARoom), dt))

            ua4 <-
              rentalsAndRaRIncome.receivedGrantLeaseAmount.fold(Try(ua3))(rgla =>
                ua3.set(ReceivedGrantLeaseAmountPage(RentalsRentARoom), rgla)
              )

            ua5 <- rentalsAndRaRIncome.premiumsGrantLease.fold(Try(ua4))(pgl =>
                     ua4.set(PremiumsGrantLeasePage(RentalsRentARoom), pgl)
                   )
            ua6 <- rentalsAndRaRIncome.yearLeaseAmount.fold(Try(ua5))(yla =>
                     ua5.set(YearLeaseAmountPage(RentalsRentARoom), yla.toInt)
                   ) // Recheck
            ua7 <- rentalsAndRaRIncome.calculatedFigureYourself.fold(Try(ua6))(cfy =>
                     ua6.set(CalculatedFigureYourselfPage(RentalsRentARoom), cfy)
                   )
            ua8 <- rentalsAndRaRIncome.reversePremiumsReceived.fold(Try(ua7))(rpr =>
                     ua7.set(ReversePremiumsReceivedPage(RentalsRentARoom), rpr)
                   )
          } yield ua8
      }

    private def updatePropertyRentalsExpensesPages(
      userAnswers: UserAnswers,
      maybePropertyRentalsExpenses: Option[RentalsExpense],
      propertyType: PropertyType
    ): Try[UserAnswers] =
      maybePropertyRentalsExpenses match {
        case None                           => Success(userAnswers)
        case _ if propertyType == RentARoom => Success(userAnswers)
        case Some(propertyRentalsExpenses) =>
          for {
            ua1 <- propertyRentalsExpenses.loanInterestOrOtherFinancialCost.fold(Try(userAnswers))(r =>
                     userAnswers.set(LoanInterestPage(propertyType), r)
                   )
            ua2 <-
              propertyRentalsExpenses.consolidatedExpenses.fold(Try(ua1))(r =>
                ua1.set(ConsolidatedExpensesPage(propertyType), r)
              )
            ua3 <- propertyRentalsExpenses.otherAllowablePropertyExpenses.fold(Try(ua2))(r =>
                     ua2.set(OtherAllowablePropertyExpensesPage(propertyType), r)
                   )
            ua4 <- propertyRentalsExpenses.propertyBusinessTravelCosts.fold(Try(ua3))(r =>
                     ua3.set(PropertyBusinessTravelCostsPage(propertyType), r)
                   )
            ua5 <- propertyRentalsExpenses.costsOfServicesProvided.fold(Try(ua4))(r =>
                     ua4.set(CostsOfServicesProvidedPage(propertyType), r)
                   )
            ua6 <-
              propertyRentalsExpenses.otherProfessionalFees.fold(Try(ua5))(r =>
                ua5.set(OtherProfessionalFeesPage(propertyType), r)
              )
            ua7 <-
              propertyRentalsExpenses.rentsRatesAndInsurance.fold(Try(ua6))(r =>
                ua6.set(RentsRatesAndInsurancePage(propertyType), r)
              )
            ua8 <-
              propertyRentalsExpenses.repairsAndMaintenanceCosts.fold(Try(ua7))(r =>
                ua7.set(RepairsAndMaintenanceCostsPage(propertyType), r)
              )
          } yield ua8
      }

    def updateStructureBuildingPages(
      userAnswers: UserAnswers,
      maybeSbasWithSupportingQuestions: Option[SbasWithSupportingQuestions],
      propertyType: PropertyType
    ): Try[UserAnswers] =
      maybeSbasWithSupportingQuestions match {
        case None                           => Success(userAnswers)
        case _ if propertyType == RentARoom => Success(userAnswers)
        case Some(sbasWithSupportingQuestions) =>
          for {
            ua1 <- userAnswers.set(
                     ClaimStructureBuildingAllowancePage(propertyType),
                     sbasWithSupportingQuestions.claimStructureBuildingAllowance
                   )
            ua3 <- updateAllSbas(ua1, sbasWithSupportingQuestions.structureBuildingFormGroup, propertyType)
          } yield ua3
      }

    def updateAllSbas(userAnswers: UserAnswers, fetchedData: List[Sba], propertyType: PropertyType): Try[UserAnswers] =
      fetchedData.zipWithIndex.foldLeft(Try(userAnswers)) { (acc, a) =>
        val (sba, index) = a
        acc.flatMap(ua => updateSba(ua, index, sba, propertyType))
      }

    def updateSba(userAnswers: UserAnswers, index: Int, sba: Sba, propertyType: PropertyType): Try[UserAnswers] =
      propertyType match {
        case RentARoom => Success(userAnswers)
        case _ =>
          for {
            ua1 <- userAnswers.set(
                     StructuredBuildingAllowanceAddressPage(index, propertyType),
                     sba.structuredBuildingAllowanceAddress
                   )
            ua2 <-
              ua1.set(StructureBuildingQualifyingDatePage(index, propertyType), sba.structureBuildingQualifyingDate)
            ua3 <-
              ua2.set(StructureBuildingQualifyingAmountPage(index, propertyType), sba.structureBuildingQualifyingAmount)
            ua4 <-
              ua3.set(StructureBuildingAllowanceClaimPage(index, propertyType), sba.structureBuildingAllowanceClaim)
          } yield ua4
      }

    private def updateEnhancedStructureBuildingPages(
      userAnswers: UserAnswers,
      maybeEsbasWithSupportingQuestions: Option[EsbasWithSupportingQuestions],
      propertyType: PropertyType
    ): Try[UserAnswers] =
      maybeEsbasWithSupportingQuestions match {
        case None                           => Success(userAnswers)
        case _ if propertyType == RentARoom => Success(userAnswers)
        case Some(esbasWithSupportingQuestions) =>
          for {
            ua1 <- userAnswers.set(
                     ClaimEsbaPage(propertyType),
                     esbasWithSupportingQuestions.claimEnhancedStructureBuildingAllowance
                   )
            ua2 <- ua1.set(EsbaClaimsPage(propertyType), esbasWithSupportingQuestions.esbaClaims.getOrElse(false))
            ua3 <- updateAllEsbas(ua2, esbasWithSupportingQuestions.esbas, propertyType)
          } yield ua3
      }

    def updateAllEsbas(
      userAnswers: UserAnswers,
      fetchedData: List[Esba],
      propertyType: PropertyType
    ): Try[UserAnswers] =
      fetchedData.zipWithIndex.foldLeft(Try(userAnswers)) { (acc, a) =>
        val (esba, index) = a
        acc.flatMap(ua => updateEsba(ua, index, esba, propertyType))
      }

    private def updateEsba(
      userAnswers: UserAnswers,
      index: Int,
      esba: Esba,
      propertyType: PropertyType
    ): Try[UserAnswers] = propertyType match {
      case RentARoom => Success(userAnswers)
      case _ =>
        for {
          ua1 <- userAnswers.set(EsbaAddressPage(index, Rentals), esba.esbaAddress)
          ua2 <- ua1.set(EsbaQualifyingDatePage(index, Rentals), esba.esbaQualifyingDate)
          ua3 <- ua2.set(EsbaQualifyingAmountPage(index, Rentals), esba.esbaQualifyingAmount)
          ua4 <- ua3.set(EsbaClaimPage(index, Rentals), esba.esbaClaim)
        } yield ua4
    }

    def updateRentARoomAbout(userAnswers: UserAnswers, maybeRentARoomAbout: Option[RaRAbout]): Try[UserAnswers] =
      maybeRentARoomAbout match {
        case None => Success(userAnswers)
        case Some(raRAbout) =>
          for {
            ua1 <- userAnswers.set(JointlyLetPage(RentARoom), raRAbout.jointlyLetYesOrNo)
            ua2 <- ua1.set(TotalIncomeAmountPage(RentARoom), raRAbout.totalIncomeAmount)
            ua3 <- ua2.set(ClaimExpensesOrReliefPage(RentARoom), raRAbout.claimExpensesOrRelief)
          } yield ua3
      }

    def updateRentalsAndRaRAbout(
      userAnswers: UserAnswers,
      maybeRentalsAndRaRAbout: Option[RentalsAndRaRAbout]
    ): Try[UserAnswers] =
      maybeRentalsAndRaRAbout match {
        case None => Success(userAnswers)
        case Some(rentalsRaRAbout) =>
          for {
            ua1 <- userAnswers.set(JointlyLetPage(RentalsRentARoom), rentalsRaRAbout.jointlyLetYesOrNo)
            ua2 <- ua1.set(TotalIncomeAmountPage(RentalsRentARoom), rentalsRaRAbout.totalIncomeAmount)
            ua3 <- ua2.set(ClaimExpensesOrReliefPage(RentalsRentARoom), rentalsRaRAbout.claimExpensesOrRelief)
            ua4 <- ua3.set(
                     ClaimPropertyIncomeAllowancePage(RentalsRentARoom),
                     rentalsRaRAbout.claimPropertyIncomeAllowanceYesOrNo
                   )
            ua5 <- ua4.set(PropertyRentalIncomePage(RentalsRentARoom), rentalsRaRAbout.incomeFromPropertyRentals)

          } yield ua5
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
