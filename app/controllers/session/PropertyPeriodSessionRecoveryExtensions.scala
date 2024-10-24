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
import pages.propertyrentals.{AboutPropertyRentalsSectionFinishedPage, ClaimPropertyIncomeAllowancePage}
import pages.propertyrentals.expenses._
import pages.propertyrentals.income._
import pages.rentalsandrentaroom.RentalsRaRAboutCompletePage
import pages.rentalsandrentaroom.adjustments.RentalsRaRAdjustmentsCompletePage
import pages.rentalsandrentaroom.allowances.RentalsRaRAllowancesCompletePage
import pages.rentalsandrentaroom.expenses.RentalsRaRExpensesCompletePage
import pages.rentalsandrentaroom.income.RentalsRaRIncomeCompletePage
import pages.structurebuildingallowance._
import pages.ukrentaroom.adjustments.{RaRAdjustmentsCompletePage, RaRBalancingChargePage}
import pages.ukrentaroom.allowances._
import pages.ukrentaroom.expenses.ExpensesRRSectionCompletePage
import pages.ukrentaroom.{AboutSectionCompletePage, ClaimExpensesOrReliefPage, JointlyLetPage, TotalIncomeAmountPage}
import play.api.libs.json.Writes
import queries.Settable

import scala.util.{Failure, Success, Try}

object PropertyPeriodSessionRecoveryExtensions {

  implicit class UserAnswersExtension(userAnswersArg: UserAnswers) {

    def update(fetchedData: FetchedBackendData): UserAnswers = {
      for {
        ua1 <- updatePart(userAnswersArg, CapitalAllowancesForACarPage(Rentals), fetchedData.capitalAllowancesForACar)
        ua2 <- updatePropertyAboutPages(ua1, fetchedData.propertyAbout)
        ua3 <- updatePropertyRentalsAboutPages(ua2, fetchedData.propertyRentalsAbout)
        ua4 <- updateAdjustmentsPages(ua3, fetchedData.adjustments)
        ua5 <- updateRentalsAndRaRAdjustmentsPages(ua4, fetchedData.rentalsAndRaRAdjustments)
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
        ua20 <- updateJourneyStatuses(ua19, fetchedData.journeyStatuses)
      } yield ua20
    }.getOrElse(userAnswersArg)

    private def updateJourneyStatuses(
      userAnswers: UserAnswers,
      journeyStatuses: List[JourneyWithStatus]
    ): Try[UserAnswers] = {
      val r: UserAnswers = journeyStatuses.foldLeft(userAnswers)((acc, a) =>
        acc.set(updateSingleJourneyStatus(a), isCompleted(a.journeyStatus)) match {
          case Success(s) => s
          case Failure(_) => acc
        }
      )
      Success(r)
    }

    private def isCompleted(status: String) =
      status.trim.toLowerCase().equals("completed")

    private def updateSingleJourneyStatus[T](journeyWithStatus: JourneyWithStatus): Settable[Boolean] =
      journeyWithStatus.journeyName match {
        case "property-about"                               => AboutPropertyCompletePage
        case "property-rental-about"                        => AboutPropertyRentalsSectionFinishedPage
        case "rental-income"                                => IncomeSectionFinishedPage
        case "rental-allowances"                            => AllowancesSectionFinishedPage
        case "rental-expenses"                              => ExpensesSectionFinishedPage
        case "rental-adjustments"                           => RentalsAdjustmentsCompletePage
        case "rental-sba"                                   => SbaSectionFinishedPage(Rentals)
        case "rental-esba"                                  => EsbaSectionFinishedPage(Rentals)
        case "rent-a-room-about"                            => AboutSectionCompletePage
        case "rent-a-room-allowances"                       => RaRAllowancesCompletePage
        case "rent-a-room-expenses"                         => ExpensesRRSectionCompletePage
        case "rent-a-room-adjustments"                      => RaRAdjustmentsCompletePage
        case "property-rentals-and-rent-a-room-about"       => RentalsRaRAboutCompletePage
        case "property-rentals-and-rent-a-room-income"      => RentalsRaRIncomeCompletePage
        case "property-rentals-and-rent-a-room-allowances"  => RentalsRaRAllowancesCompletePage
        case "property-rentals-and-rent-a-room-expenses"    => RentalsRaRExpensesCompletePage
        case "property-rentals-and-rent-a-room-adjustments" => RentalsRaRAdjustmentsCompletePage
        case "property-rentals-and-rent-a-room-sba"         => SbaSectionFinishedPage(RentARoom)
        case "property-rentals-and-rent-a-room-esba"        => EsbaSectionFinishedPage(RentARoom)
      }
//      case object About extends JourneyName("property-about")
//      case object RentalAbout extends JourneyName("property-rental-about")
//      case object RentalIncome extends JourneyName("rental-income")
//      case object RentalAllowances extends JourneyName("rental-allowances")
//      case object RentalExpenses extends JourneyName("rental-expenses")
//      case object RentalAdjustments extends JourneyName("rental-adjustments")
//      case object RentalSBA extends JourneyName("rental-sba")
//      case object RentalESBA extends JourneyName("rental-esba")
//      case object RentARoomAbout extends JourneyName("rent-a-room-about")
//      case object RentARoomAllowances extends JourneyName("rent-a-room-allowances")
//      case object RentARoomExpenses extends JourneyName("rent-a-room-expenses")
//      case object RentARoomAdjustments extends JourneyName("rent-a-room-adjustments")
//      case object RentalsAndRaRAbout extends JourneyName("property-rentals-and-rent-a-room-about")
//      case object RentalsAndRaRIncome extends JourneyName("property-rentals-and-rent-a-room-income")
//      case object RentalsAndRaRAllowances extends JourneyName("property-rentals-and-rent-a-room-allowances")
//      case object RentalsAndRaRExpenses extends JourneyName("property-rentals-and-rent-a-room-expenses")
//      case object RentalsAndRaRAdjustments extends JourneyName("property-rentals-and-rent-a-room-adjustments")
//      case object RentalsAndRaRSBA extends JourneyName("property-rentals-and-rent-a-room-sba")
//      case object RentalsAndRaRESBA extends JourneyName("property-rentals-and-rent-a-room-esba")

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
      maybeAdjustments: Option[Adjustments]
    ): Try[UserAnswers] =
      maybeAdjustments match {
        case None => Success(userAnswers)
        case Some(adjustments) =>
          for {
            ua1 <- userAnswers.set(BalancingChargePage(Rentals), adjustments.balancingCharge)
            ua2 <- ua1.set(PrivateUseAdjustmentPage(Rentals), adjustments.privateUseAdjustment)
            ua3 <- ua2.set(PropertyIncomeAllowancePage(Rentals), adjustments.propertyIncomeAllowance)
            ua4 <-
              ua3.set(
                RenovationAllowanceBalancingChargePage(Rentals),
                adjustments.renovationAllowanceBalancingCharge
              )
            ua5 <- ua4.set(ResidentialFinanceCostPage(Rentals), adjustments.residentialFinanceCost)
            ua6 <- ua5.set(UnusedResidentialFinanceCostPage(Rentals), adjustments.unusedResidentialFinanceCost)
          } yield ua6
      }

    private def updateRentalsAndRaRAdjustmentsPages(
      userAnswers: UserAnswers,
      maybeRentalsAndRaRAdjustments: Option[RentalsAndRentARoomAdjustment]
    ): Try[UserAnswers] =
      maybeRentalsAndRaRAdjustments match {
        case None => Success(userAnswers)
        case Some(adjustments) =>
          for {
            ua1 <- userAnswers.set(BalancingChargePage(RentalsRentARoom), adjustments.balancingCharge)
            ua2 <- ua1.set(
                     PrivateUseAdjustmentPage(RentalsRentARoom),
                     PrivateUseAdjustment(adjustments.privateUseAdjustment)
                   )
            ua3 <- adjustments.propertyIncomeAllowance
                     .map(ua2.set(PropertyIncomeAllowancePage(RentalsRentARoom), _))
                     .getOrElse(Success(ua2))
            ua4 <-
              ua3.set(
                RenovationAllowanceBalancingChargePage(RentalsRentARoom),
                adjustments.renovationAllowanceBalancingCharge
              )
            ua5 <- ua4.set(ResidentialFinanceCostPage(RentalsRentARoom), adjustments.residentialFinanceCost)
            ua6 <- adjustments.unusedResidentialFinanceCost
                     .map(ua5.set(UnusedResidentialFinanceCostPage(RentalsRentARoom), _))
                     .getOrElse(Success(ua5))
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
            ua3 <- ua2.set(OtherCapitalAllowancePage(propertyType), allowances.otherCapitalAllowance)
            ua4 <- ua3.set(ReplacementOfDomesticGoodsPage(propertyType), allowances.replacementOfDomesticGoodsAllowance)
            ua5 <- ua4.set(ZeroEmissionCarAllowancePage(propertyType), allowances.zeroEmissionCarAllowance)
            ua6 <-
              ua5.set(ZeroEmissionGoodsVehicleAllowancePage(propertyType), allowances.zeroEmissionGoodsVehicleAllowance)
          } yield ua6
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
              userAnswers.set(PropertyRentalIncomePage(Rentals), propertyRentalsIncome.propertyRentalIncome)

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
            ua5 <- ua4.set(PropertyRentalIncomePage(RentalsRentARoom), rentalsRaRAbout.propertyRentalIncome)

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
            ua3 <- rentARoomAllowance.zeroEmissionCarAllowance.fold[Try[UserAnswers]](Success(ua2))(zeca =>
                     ua2.set(RaRZeroEmissionCarAllowancePage, zeca)
                   )
            ua4 <- rentARoomAllowance.zeroEmissionGoodsVehicleAllowance.fold[Try[UserAnswers]](Success(ua3))(zegva =>
                     ua3.set(RaRZeroEmissionGoodsVehicleAllowancePage, zegva)
                   )
            ua5 <- rentARoomAllowance.replacementOfDomesticGoodsAllowance.fold[Try[UserAnswers]](Success(ua4))(rodga =>
                     ua4.set(RaRReplacementsOfDomesticGoodsPage, rodga)
                   )
            ua6 <- rentARoomAllowance.otherCapitalAllowance.fold[Try[UserAnswers]](Success(ua5))(oca =>
                     ua5.set(RaROtherCapitalAllowancesPage, oca)
                   )
          } yield ua6
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
