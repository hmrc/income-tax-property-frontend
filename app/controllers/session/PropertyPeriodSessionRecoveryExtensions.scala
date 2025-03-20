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

package controllers.session

import audit._
import models._
import models.ukAndForeign.UkAndForeignAbout
import pages._
import pages.adjustments._
import pages.allowances._
import pages.enhancedstructuresbuildingallowance._
import pages.foreign.adjustments._
import pages.foreign.allowances._
import pages.foreign.expenses._
import pages.foreign.income.{ForeignIncomeSectionCompletePage, ForeignOtherIncomeFromPropertyPage, ForeignPropertyRentalIncomePage, PremiumsGrantLeaseYNPage}
import pages.foreign.structurebuildingallowance._
import pages.foreign._
import pages.{TotalIncomePage => UKTotalIncomePage}
import pages.premiumlease._
import pages.propertyrentals.expenses._
import pages.propertyrentals.income._
import pages.propertyrentals.{AboutPropertyRentalsSectionFinishedPage, ClaimPropertyIncomeAllowancePage}
import pages.rentalsandrentaroom.RentalsRaRAboutCompletePage
import pages.rentalsandrentaroom.adjustments.RentalsRaRAdjustmentsCompletePage
import pages.rentalsandrentaroom.allowances.RentalsRaRAllowancesCompletePage
import pages.rentalsandrentaroom.expenses.RentalsRaRExpensesCompletePage
import pages.rentalsandrentaroom.income.RentalsRaRIncomeCompletePage
import pages.structurebuildingallowance._
import pages.ukandforeignproperty._
import pages.ukrentaroom.adjustments._
import pages.ukrentaroom.allowances._
import pages.ukrentaroom.expenses._
import pages.ukrentaroom.{AboutSectionCompletePage, ClaimExpensesOrReliefPage, JointlyLetPage, TotalIncomeAmountPage}
import play.api.libs.json.Writes
import queries.Settable

import java.time.LocalDate
import scala.util.{Failure, Success, Try}

object PropertyPeriodSessionRecoveryExtensions {

  implicit class UserAnswersExtension(userAnswersArg: UserAnswers) {

    def update(fetchedData: FetchedPropertyData): UserAnswers = {
      for {
        ua1 <- updatePart(
                 userAnswersArg,
                 CapitalAllowancesForACarPage(Rentals),
                 fetchedData.ukPropertyData.capitalAllowancesForACar
               )
        ua2 <- updatePropertyAboutPages(ua1, fetchedData.ukPropertyData.propertyAbout)
        ua3 <- updatePropertyRentalsAboutPages(ua2, fetchedData.ukPropertyData.propertyRentalsAbout)
        ua4 <- updateRentalsAndRaRAbout(ua3, fetchedData.ukPropertyData.rentalsAndRaRAbout)
        ua5 <- updateRentalsAndRaRAdjustmentsPages(ua4, fetchedData.ukPropertyData.rentalsAndRaRAdjustments)
        ua6 <- updateAllowancesPages(ua5, fetchedData.ukPropertyData.allowances, Rentals)
        ua7 <- updateAllowancesPages(ua6, fetchedData.ukPropertyData.allowances, RentalsRentARoom)
        ua8 <- updateStructureBuildingPages(ua7, fetchedData.ukPropertyData.sbasWithSupportingQuestions, Rentals)
        ua9 <- updateStructureBuildingPages(ua8, fetchedData.ukPropertyData.rentalsAndRaRSbasWithSupportingQuestions, RentalsRentARoom)
        ua10 <-
          updateEnhancedStructureBuildingPages(ua9, fetchedData.ukPropertyData.esbasWithSupportingQuestions, Rentals)
        ua11 <- updateEnhancedStructureBuildingPages(
                  ua10,
                  fetchedData.ukPropertyData.rentalsAndRaREsbasWithSupportingQuestions,
                  RentalsRentARoom
                )
        ua12 <- updatePropertyRentalsIncomePages(ua11, fetchedData.ukPropertyData.propertyRentalsIncome)
        ua13 <- updateRentalsAndRaRIncomePages(ua12, fetchedData.ukPropertyData.rentalsAndRaRIncome)
        ua14 <- updatePropertyRentalsExpensesPages(ua13, fetchedData.ukPropertyData.propertyRentalsExpenses, Rentals)
        ua15 <-
          updatePropertyRentalsExpensesPages(ua14, fetchedData.ukPropertyData.propertyRentalsExpenses, RentalsRentARoom)
        ua16 <- updateRentARoomAbout(ua15, fetchedData.ukPropertyData.raRAbout)
        ua17 <- updateRentARoomExpenses(ua16, fetchedData.ukPropertyData.rarExpenses)
        ua18 <- updateRentARoomAllowance(ua17, fetchedData.ukPropertyData.rentARoomAllowances)
        ua19 <- updateRentARoomAdjustments(ua18, fetchedData.ukPropertyData.raRAdjustments)
        ua20 <- updateAdjustmentsPages(ua19, fetchedData.ukPropertyData.adjustments)
        ua21 <- updateForeignPropertySelectCountry(ua20, fetchedData.ukPropertyData.foreignPropertySelectCountry)
        ua22 <- updateJourneyStatuses(ua21, fetchedData.ukPropertyData.journeyStatuses)
        ua23 <- updateForeignPropertyIncome(ua22, fetchedData.foreignPropertyData.foreignPropertyIncome)
        ua24 <- updateForeignPropertyExpenses(ua23, fetchedData.foreignPropertyData.foreignPropertyExpenses)
        ua25 <- updateForeignPropertyTax(ua24, fetchedData.foreignPropertyData.foreignPropertyTax)
        ua26 <- updateForeignPropertyAllowances(ua25, fetchedData.foreignPropertyData.foreignPropertyAllowances)
        ua27 <- updateforeignPropertySbaPage(ua26, fetchedData.foreignPropertyData.foreignPropertySba)
        ua28 <- updateForeignPropertyAdjustments(ua27, fetchedData.foreignPropertyData.foreignPropertyAdjustments)
        ua29 <- updateForeignJourneyStatuses(ua28, fetchedData.foreignPropertyData.foreignJourneyStatuses)
        ua30 <- updateUkAndForeignPropertyAboutPages(ua29, fetchedData.ukAndForeignPropertyData.ukAndForeignAbout)
      } yield ua30
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
        case "foreign-property-select-country"              => ForeignSelectCountriesCompletePage
      }

    private def updateForeignJourneyStatuses(
      userAnswers: UserAnswers,
      maybeForeignJourneyStatuses: Option[Map[String, List[JourneyWithStatus]]]
    ): Try[UserAnswers] =
      maybeForeignJourneyStatuses match {
        case Some(foreignJourneyStatuses) =>
          val r: UserAnswers = foreignJourneyStatuses.foldLeft(userAnswers) {
            case (acc: UserAnswers, (countryCode: String, journeys: List[JourneyWithStatus])) =>
              journeys.foldLeft(acc) { case (innerAcc: UserAnswers, journey: JourneyWithStatus) =>
                innerAcc.set(
                  updateSingleForeignJourneyStatus(countryCode, journey),
                  isCompleted(journey.journeyStatus)
                ) match {
                  case Success(ua) => ua
                  case Failure(_)  => innerAcc
                }
              }
          }
          Success(r)
        case _ => Success(userAnswers)
      }

    private def updateSingleForeignJourneyStatus(
      countryCode: String,
      foreignJourneyWithStatus: JourneyWithStatus
    ): Settable[Boolean] =
      foreignJourneyWithStatus.journeyName match {
        case "foreign-property-tax"         => ForeignTaxSectionCompletePage(countryCode)
        case "foreign-property-income"      => ForeignIncomeSectionCompletePage(countryCode)
        case "foreign-property-expenses"    => ForeignExpensesSectionCompletePage(countryCode)
        case "foreign-property-allowances"  => ForeignAllowancesCompletePage(countryCode)
        case "foreign-property-sba"         => ForeignSbaCompletePage(countryCode)
        case "foreign-property-adjustments" => ForeignAdjustmentsCompletePage(countryCode)

      }

    private def updatePropertyAboutPages(
      userAnswers: UserAnswers,
      maybePropertyAbout: Option[models.PropertyAbout]
    ): Try[UserAnswers] =
      maybePropertyAbout match {
        case None => Success(userAnswers)
        case Some(propertyAbout) =>
          for {
            ua1 <- propertyAbout.ukProperty.fold(Try(userAnswers))(ukps => userAnswers.set(UKPropertyPage, ukps.toSet))
            ua2 <- ua1.set(UKTotalIncomePage, propertyAbout.totalIncome)
            ua3 <- updatePart(ua2, ReportPropertyIncomePage, propertyAbout.reportPropertyIncome)
          } yield ua3
      }

    private def updateForeignPropertySelectCountry(
      userAnswers: UserAnswers,
      maybeForeignPropertySelectCountry: Option[models.ForeignPropertySelectCountry]
    ): Try[UserAnswers] =
      maybeForeignPropertySelectCountry match {
        case None => Success(userAnswers)
        case Some(foreignPropertySelectCountry) =>
          for {
            totalIncomeAnswers <-
              userAnswers.set(pages.foreign.TotalIncomePage, foreignPropertySelectCountry.totalIncome)
            reportIncomeUserAnswers <-
              foreignPropertySelectCountry.reportPropertyIncome.fold[Try[UserAnswers]](
                Success(totalIncomeAnswers)
              )(reportIncome => totalIncomeAnswers.set(pages.foreign.PropertyIncomeReportPage, reportIncome))
            incomeCountriesUserAnswers <-
              foreignPropertySelectCountry.incomeCountries.fold[Try[UserAnswers]](Success(reportIncomeUserAnswers))(
                incomeCountries => reportIncomeUserAnswers.set(pages.foreign.IncomeSourceCountries, incomeCountries)
              )
            addCountriesUserAnswers <-
              foreignPropertySelectCountry.addAnotherCountry.fold[Try[UserAnswers]](
                Success(incomeCountriesUserAnswers)
              )(addCountries => incomeCountriesUserAnswers.set(pages.foreign.AddCountriesRentedPage, addCountries))
            claimPIAUserAnswers <-
              foreignPropertySelectCountry.claimPropertyIncomeAllowance.fold[Try[UserAnswers]](
                Success(addCountriesUserAnswers)
              )(claimAllowances =>
                addCountriesUserAnswers.set(pages.foreign.ClaimPropertyIncomeAllowanceOrExpensesPage, claimAllowances)
              )

          } yield claimPIAUserAnswers
      }

    private def updateUkAndForeignPropertyAboutPages(
      userAnswers: UserAnswers,
      maybePropertyAbout: Option[UkAndForeignAbout]
    ): Try[UserAnswers] =
      maybePropertyAbout match {
        case None => Success(userAnswers)
        case Some(ukAndForeignAbout) =>
          for {
            ua1 <- userAnswers.set(TotalPropertyIncomePage, ukAndForeignAbout.aboutUkAndForeign.totalPropertyIncome)
            ua2 <- updatePart(ua1, ReportIncomePage, ukAndForeignAbout.aboutUkAndForeign.reportIncome)
          } yield ua2
      }

    private def updateForeignPropertyIncome(
      userAnswers: UserAnswers,
      maybeForeignPropertyIncome: Option[Map[String, ForeignIncomeAnswers]]
    ): Try[UserAnswers] = maybeForeignPropertyIncome match {
      case None => Success(userAnswers)
      case Some(foreignPropertyIncomeMap) =>
        foreignPropertyIncomeMap.foldLeft[Try[UserAnswers]](Success(userAnswers)) {
          case (userAnswers: Try[UserAnswers], (countryCode: String, foreignPropertyIncome: ForeignIncomeAnswers)) =>
            for {
              ua <- userAnswers
              ua1 <- foreignPropertyIncome.rentIncome.fold[Try[UserAnswers]](Success(ua))(rentIncome =>
                       ua.set(ForeignPropertyRentalIncomePage(countryCode), rentIncome)
                     )
              ua2 <- ua1.set(PremiumsGrantLeaseYNPage(countryCode), foreignPropertyIncome.premiumsGrantLeaseReceived)
              ua3 <- foreignPropertyIncome.calculatedPremiumLeaseTaxable.fold[Try[UserAnswers]](Success(ua2))(
                       premiumCalculated => ua2.set(CalculatedPremiumLeaseTaxablePage(countryCode), premiumCalculated)
                     )
              ua4 <- foreignPropertyIncome.receivedGrantLeaseAmount.fold[Try[UserAnswers]](Success(ua3))(
                       receivedGrantLeaseAmount =>
                         ua3.set(ForeignReceivedGrantLeaseAmountPage(countryCode), receivedGrantLeaseAmount)
                     )
              ua5 <- foreignPropertyIncome.twelveMonthPeriodsInLease.fold[Try[UserAnswers]](Success(ua4))(
                       foreignYearLeaseAmount =>
                         ua4.set(TwelveMonthPeriodsInLeasePage(countryCode), foreignYearLeaseAmount.intValue)
                     )
              ua6 <- foreignPropertyIncome.premiumsOfLeaseGrantAgreed.fold[Try[UserAnswers]](Success(ua5))(
                       premiumsOfLeaseGrantAgreed =>
                         ua5.set(ForeignPremiumsGrantLeasePage(countryCode), premiumsOfLeaseGrantAgreed)
                     )
              ua7 <-
                foreignPropertyIncome.otherPropertyIncome.fold[Try[UserAnswers]](Success(ua6))(otherPropertyIncome =>
                  ua6.set(ForeignOtherIncomeFromPropertyPage(countryCode), otherPropertyIncome)
                )
            } yield ua7
        }
    }

    private def updateForeignPropertyTax(
      userAnswers: UserAnswers,
      maybeForeignPropertyTax: Option[Map[String, ForeignPropertyTax]]
    ): Try[UserAnswers] =
      maybeForeignPropertyTax match {
        case Some(foreignTaxMap) =>
          foreignTaxMap.foldLeft(Try(userAnswers)) {
            case (userAnswers: Try[UserAnswers], (countryCode: String, foreignPropertyTax: ForeignPropertyTax)) =>
              for {
                ua <- userAnswers
                ua1 <-
                  foreignPropertyTax.foreignIncomeTax.fold[Try[UserAnswers]](Success(ua)) { incomeTax =>
                    ua.set(
                      ForeignIncomeTaxPage(countryCode),
                      ForeignIncomeTax(incomeTax.foreignIncomeTaxYesNo, incomeTax.foreignTaxPaidOrDeducted)
                    )
                  }
                ua2 <-
                  foreignPropertyTax.foreignTaxCreditRelief.fold[Try[UserAnswers]](Success(ua1)) { taxRelief =>
                    ua1.set(ClaimForeignTaxCreditReliefPage(countryCode), taxRelief)
                  }
              } yield ua2
          }
        case None => Success(userAnswers)
      }

    private def updateForeignPropertyExpenses(
      userAnswers: UserAnswers,
      maybeForeignPropertyExpenses: Option[Map[String, ForeignExpensesAnswers]]
    ): Try[UserAnswers] = maybeForeignPropertyExpenses match {
      case None => Success(userAnswers)
      case Some(foreignPropertyExpensesMap) =>
        foreignPropertyExpensesMap.foldLeft[Try[UserAnswers]](Success(userAnswers)) {
          case (
                userAnswers: Try[UserAnswers],
                (countryCode: String, foreignPropertyExpenses: ForeignExpensesAnswers)
              ) =>
            for {
              ua <- userAnswers
              ua1 <-
                foreignPropertyExpenses.consolidatedExpenses.fold[Try[UserAnswers]](Success(ua))(consolidatedExpenses =>
                  ua.set(ConsolidatedOrIndividualExpensesPage(countryCode), consolidatedExpenses)
                )
              ua2 <-
                foreignPropertyExpenses.premisesRunningCosts.fold[Try[UserAnswers]](Success(ua1))(
                  premisesRunningCosts => ua1.set(ForeignRentsRatesAndInsurancePage(countryCode), premisesRunningCosts)
                )
              ua3 <- foreignPropertyExpenses.repairsAndMaintenance.fold[Try[UserAnswers]](Success(ua2))(
                       repairsAndMaintenance =>
                         ua2.set(ForeignPropertyRepairsAndMaintenancePage(countryCode), repairsAndMaintenance)
                     )
              ua4 <- foreignPropertyExpenses.financialCosts.fold[Try[UserAnswers]](Success(ua3))(financialCosts =>
                       ua3.set(ForeignNonResidentialPropertyFinanceCostsPage(countryCode), financialCosts)
                     )
              ua5 <- foreignPropertyExpenses.professionalFees.fold[Try[UserAnswers]](Success(ua4))(professionalFees =>
                       ua4.set(ForeignProfessionalFeesPage(countryCode), professionalFees)
                     )
              ua6 <- foreignPropertyExpenses.costOfServices.fold[Try[UserAnswers]](Success(ua5))(costOfServices =>
                       ua5.set(ForeignCostsOfServicesProvidedPage(countryCode), costOfServices)
                     )
              ua7 <- foreignPropertyExpenses.other.fold[Try[UserAnswers]](Success(ua6))(other =>
                       ua6.set(ForeignOtherAllowablePropertyExpensesPage(countryCode), other)
                     )
            } yield ua7
        }
    }

    private def updateForeignPropertyAllowances(
      userAnswers: UserAnswers,
      maybeForeignPropertyAllowances: Option[Map[String, models.ForeignPropertyAllowances]]
    ): Try[UserAnswers] = maybeForeignPropertyAllowances match {
      case None => Success(userAnswers)
      case Some(foreignAllowancesMap) =>
        foreignAllowancesMap.foldLeft[Try[UserAnswers]](Success(userAnswers)) {
          case (
                userAnswers: Try[UserAnswers],
                (countryCode: String, foreignPropertyAllowances: models.ForeignPropertyAllowances)
              ) =>
            for {
              ua <- userAnswers
              ua1 <-
                foreignPropertyAllowances.zeroEmissionsCarAllowance.fold[Try[UserAnswers]](Success(ua))(zeroEmissions =>
                  ua.set(ForeignZeroEmissionCarAllowancePage(countryCode), zeroEmissions)
                )
              ua2 <- foreignPropertyAllowances.zeroEmissionsGoodsVehicleAllowance.fold[Try[UserAnswers]](Success(ua1))(
                       goodsVehicleAllowance =>
                         ua1.set(ForeignZeroEmissionGoodsVehiclesPage(countryCode), goodsVehicleAllowance)
                     )
              ua3 <- foreignPropertyAllowances.costOfReplacingDomesticItems.fold[Try[UserAnswers]](Success(ua2))(
                       domesticItems => ua2.set(ForeignReplacementOfDomesticGoodsPage(countryCode), domesticItems)
                     )
              ua4 <- foreignPropertyAllowances.otherCapitalAllowance.fold[Try[UserAnswers]](Success(ua3))(
                       capitalAllowances => ua3.set(ForeignOtherCapitalAllowancesPage(countryCode), capitalAllowances)
                     )
              ua5 <- foreignPropertyAllowances.capitalAllowancesForACar.fold[Try[UserAnswers]](Success(ua4))(
                       capitalAllowancesForACar =>
                         ua4.set(ForeignCapitalAllowancesForACarPage(countryCode), capitalAllowancesForACar)
                     )
            } yield ua5
        }
    }

    private def updateforeignPropertySbaPage(
      userAnswers: UserAnswers,
      maybeSba: Option[Map[String, ForeignSbaAnswers]]
    ): Try[UserAnswers] =
      maybeSba match {
        case None => Success(userAnswers)
        case Some(sbaMap) =>
          sbaMap.foldLeft[Try[UserAnswers]](Success(userAnswers)) {
            case (userAnswers: Try[UserAnswers], (countryCode, sba: ForeignSbaAnswers)) =>
              for {
                ua <- userAnswers
                ua1 <-
                  ua.set(ForeignClaimStructureBuildingAllowancePage(countryCode), sba.claimStructureBuildingAllowance)
                ua2 <- sba.allowances.fold[Try[UserAnswers]](Success(ua1))(sbas =>
                         updateAllForeignPropertySbas(ua1, sbas, countryCode)
                       )
              } yield ua2
          }
      }

    private def updateAllForeignPropertySbas(
      userAnswers: UserAnswers,
      sbas: Seq[StructuredBuildingAllowance],
      countryCode: String
    ): Try[UserAnswers] =
      sbas.zipWithIndex.foldLeft(Try(userAnswers)) { (acc, a) =>
        val (sba, index) = a
        acc.flatMap(ua => updateForeignSba(ua, index, sba, countryCode))
      }

    private def updateForeignSba(
      userAnswers: UserAnswers,
      index: Int,
      sba: StructuredBuildingAllowance,
      countryCode: String
    ): Try[UserAnswers] =
      for {
        ua1 <- userAnswers.set(
                 ForeignStructuresBuildingAllowanceAddressPage(index, countryCode),
                 ForeignStructuresBuildingAllowanceAddress(
                   sba.building.name.getOrElse(""),
                   sba.building.number.getOrElse(""),
                   sba.building.postCode
                 )
               )
        ua2 <-
          ua1.set(
            ForeignStructureBuildingQualifyingDatePage(countryCode, index),
            sba.firstYear.map(_.qualifyingDate).getOrElse(LocalDate.now())
          )
        ua3 <-
          ua2.set(
            ForeignStructureBuildingQualifyingAmountPage(countryCode, index),
            sba.firstYear.map(_.qualifyingAmountExpenditure).getOrElse(BigDecimal(0))
          )
        ua4 <-
          ua3.set(ForeignStructureBuildingAllowanceClaimPage(countryCode, index), sba.amount)
      } yield ua4

    private def updateForeignPropertyAdjustments(
      userAnswers: UserAnswers,
      maybeAdjustmentsAnswers: Option[Map[String, ForeignAdjustmentsAnswers]]
    ): Try[UserAnswers] = maybeAdjustmentsAnswers match {
      case Some(adjustmentsMap) =>
        adjustmentsMap.foldLeft[Try[UserAnswers]](Success(userAnswers)) {
          case (userAnswers: Try[UserAnswers], (countryCode, adjustmentsAnswers: ForeignAdjustmentsAnswers)) =>
            for {
              ua <- userAnswers
              ua1 <- adjustmentsAnswers.privateUseAdjustment.fold[Try[UserAnswers]](Success(ua))(privateUseAdjustment =>
                       ua.set(ForeignPrivateUseAdjustmentPage(countryCode), privateUseAdjustment)
                     )
              ua2 <- adjustmentsAnswers.balancingCharge.fold[Try[UserAnswers]](Success(ua1))(balancingCharge =>
                       ua1.set(ForeignBalancingChargePage(countryCode), balancingCharge)
                     )
              ua3 <-
                adjustmentsAnswers.residentialFinanceCost.fold[Try[UserAnswers]](Success(ua2))(residentialFinanceCost =>
                  ua2.set(ForeignResidentialFinanceCostsPage(countryCode), residentialFinanceCost)
                )
              ua4 <- adjustmentsAnswers.unusedResidentialFinanceCost.fold[Try[UserAnswers]](Success(ua3))(
                       unusedResidentialFinanceCost =>
                         ua3.set(ForeignUnusedResidentialFinanceCostPage(countryCode), unusedResidentialFinanceCost)
                     )
              ua5 <- adjustmentsAnswers.propertyIncomeAllowanceClaim.fold[Try[UserAnswers]](Success(ua4))(
                       propertyIncomeAllowanceClaim =>
                         ua4.set(PropertyIncomeAllowanceClaimPage(countryCode), propertyIncomeAllowanceClaim)
                     )
              ua6 <- adjustmentsAnswers.unusedLossesPreviousYears.fold[Try[UserAnswers]](Success(ua5))(
                       unusedLossesPreviousYears =>
                         ua5.set(ForeignUnusedLossesPreviousYearsPage(countryCode), unusedLossesPreviousYears)
                     )
              ua7 <-
                adjustmentsAnswers.whenYouReportedTheLoss.fold[Try[UserAnswers]](Success(ua6))(whenYouReportedTheLoss =>
                  ua6.set(ForeignWhenYouReportedTheLossPage(countryCode), whenYouReportedTheLoss)
                )
            } yield ua7
        }
      case None => Success(userAnswers)
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
            ua7 <- adjustments.unusedLossesBroughtForward.fold[Try[UserAnswers]](Success(ua6))(unusedLossesBroughtForward =>
              ua6.set(UnusedLossesBroughtForwardPage(Rentals), unusedLossesBroughtForward))
            ua8 <- adjustments.whenYouReportedTheLoss.fold[Try[UserAnswers]](Success(ua7))(whenYouReportedTheLoss =>
              ua7.set(WhenYouReportedTheLossPage(Rentals), whenYouReportedTheLoss))
          } yield ua8
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
            ua1 <- allowances.annualInvestmentAllowance.fold[Try[UserAnswers]](Success(userAnswers)){
              annualInvestmentAllowance => userAnswers.set(AnnualInvestmentAllowancePage(propertyType), annualInvestmentAllowance)
            }
            ua2 <- allowances.businessPremisesRenovationAllowance.fold[Try[UserAnswers]](Success(ua1)){
              businessPremisesRenovationAllowance => ua1.set(BusinessPremisesRenovationPage(propertyType), businessPremisesRenovationAllowance)
            }
            ua3 <- allowances.otherCapitalAllowance.fold[Try[UserAnswers]](Success(ua2)){
              otherCapitalAllowance => ua2.set(OtherCapitalAllowancePage(propertyType), otherCapitalAllowance)
            }
            ua4 <- allowances.replacementOfDomesticGoodsAllowance.fold[Try[UserAnswers]](Success(ua3)){
              replacementOfDomesticGoodsAllowance => ua3.set(ReplacementOfDomesticGoodsPage(propertyType), replacementOfDomesticGoodsAllowance)
            }
            ua5 <- allowances.zeroEmissionCarAllowance.fold[Try[UserAnswers]](Success(ua4)) {
              zeroEmissionCarAllowance => ua4.set(ZeroEmissionCarAllowancePage(propertyType), zeroEmissionCarAllowance)
            }
            ua6 <-
              allowances.zeroEmissionGoodsVehicleAllowance.fold[Try[UserAnswers]](Success(ua5)) {
                zeroEmissionGoodsVehicleAllowance => ua5.set(ZeroEmissionGoodsVehicleAllowancePage(propertyType), zeroEmissionGoodsVehicleAllowance)
            }
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

    private def updateStructureBuildingPages(
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

    private def updateAllSbas(
      userAnswers: UserAnswers,
      fetchedData: List[Sba],
      propertyType: PropertyType
    ): Try[UserAnswers] =
      fetchedData.zipWithIndex.foldLeft(Try(userAnswers)) { (acc, a) =>
        val (sba, index) = a
        acc.flatMap(ua => updateSba(ua, index, sba, propertyType))
      }

    private def updateSba(
      userAnswers: UserAnswers,
      index: Int,
      sba: Sba,
      propertyType: PropertyType
    ): Try[UserAnswers] =
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
            ua2 <- ua1.set(EsbaClaimsPage(propertyType), esbasWithSupportingQuestions.enhancedStructureBuildingAllowanceClaims.getOrElse(false))
            ua3 <- updateAllEsbas(ua2, esbasWithSupportingQuestions.enhancedStructureBuildingAllowances, propertyType)
          } yield ua3
      }

    private def updateAllEsbas(
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
          ua1 <- userAnswers.set(EsbaAddressPage(index, Rentals), esba.enhancedStructureBuildingAllowanceAddress)
          ua2 <- ua1.set(EsbaQualifyingDatePage(index, Rentals), esba.enhancedStructureBuildingAllowanceQualifyingDate)
          ua3 <- ua2.set(EsbaQualifyingAmountPage(index, Rentals), esba.enhancedStructureBuildingAllowanceQualifyingAmount)
          ua4 <- ua3.set(EsbaClaimPage(index, Rentals), esba.enhancedStructureBuildingAllowanceClaim)
        } yield ua4
    }

    private def updateRentARoomAbout(
      userAnswers: UserAnswers,
      maybeRentARoomAbout: Option[RaRAbout]
    ): Try[UserAnswers] =
      maybeRentARoomAbout match {
        case None => Success(userAnswers)
        case Some(raRAbout) =>
          for {
            ua1 <- userAnswers.set(JointlyLetPage(RentARoom), raRAbout.jointlyLetYesOrNo)
            ua2 <- ua1.set(TotalIncomeAmountPage(RentARoom), raRAbout.totalIncomeAmount)
            ua3 <- ua2.set(ClaimExpensesOrReliefPage(RentARoom), raRAbout.claimExpensesOrRelief)
          } yield ua3
      }

    private def updateRentARoomExpenses(
      userAnswers: UserAnswers,
      maybeRentARoomExpenses: Option[RentARoomExpenses]
    ): Try[UserAnswers] =
      maybeRentARoomExpenses match {
        case None => Success(userAnswers)
        case Some(rarExpenses) =>
          for {
            ua1 <- rarExpenses.consolidatedExpenses.fold[Try[UserAnswers]](Success(userAnswers))(consolidatedExpenses =>
              userAnswers.set(ConsolidatedExpensesRRPage, ConsolidatedRRExpenses(
                consolidatedExpensesYesOrNo = consolidatedExpenses.consolidatedExpensesYesOrNo,
                consolidatedExpensesAmount = consolidatedExpenses.consolidatedExpensesAmount
              )))
            ua2 <- rarExpenses.rentsRatesAndInsurance.fold[Try[UserAnswers]](Success(ua1))(rentsRatesAndInsurance =>
              ua1.set(RentsRatesAndInsuranceRRPage, rentsRatesAndInsurance)
            )
            ua3 <- rarExpenses.repairsAndMaintenanceCosts.fold[Try[UserAnswers]](Success(ua2))(repairsAndMaintenanceCosts =>
              ua2.set(RepairsAndMaintenanceCostsRRPage, repairsAndMaintenanceCosts)
            )
            ua4 <- rarExpenses.legalManagementOtherFee.fold[Try[UserAnswers]](Success(ua3))(legalManagementOtherFee =>
              ua3.set(LegalManagementOtherFeeRRPage, legalManagementOtherFee)
            )
            ua5 <- rarExpenses.costOfServicesProvided.fold[Try[UserAnswers]](Success(ua4))(costOfServicesProvided =>
              ua4.set(CostOfServicesProvidedRRPage, costOfServicesProvided)
            )
            ua6 <- rarExpenses.otherPropertyExpenses.fold[Try[UserAnswers]](Success(ua5))(otherPropertyExpenses =>
              ua5.set(OtherPropertyExpensesRRPage, otherPropertyExpenses)
            )
          } yield ua6
      }

    private def updateRentalsAndRaRAbout(
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

    private def updateRentARoomAllowance(
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

    private def updateRentARoomAdjustments(
      userAnswers: UserAnswers,
      maybeRentARoomAdjustments: Option[RentARoomAdjustments]
    ): Try[UserAnswers] =
      maybeRentARoomAdjustments match {
        case None                       => Success(userAnswers)
        case Some(rentARoomAdjustments) =>
          for {
            ua1 <- userAnswers.set(RaRBalancingChargePage, rentARoomAdjustments.balancingCharge)
            ua2 <- ua1.set(RaRUnusedResidentialCostsPage, rentARoomAdjustments.unusedResidentialPropertyFinanceCostsBroughtFwd)
            ua3 <- rentARoomAdjustments.unusedLossesBroughtForward.fold[Try[UserAnswers]](Success(ua2))(unusedLossesBroughtForward =>
              ua2.set(RaRUnusedLossesBroughtForwardPage, unusedLossesBroughtForward))
            ua4 <- rentARoomAdjustments.whenYouReportedTheLoss.fold[Try[UserAnswers]](Success(ua3))(whenYouReportedTheLoss =>
              ua3.set(RarWhenYouReportedTheLossPage, whenYouReportedTheLoss))
          } yield ua4
      }
  }
}
