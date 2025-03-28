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

package pages.ukandforeignproperty

import models.{UkAndForeignPropertyRentalTypeUk, UserAnswers}
import pages.foreign.ForeignSummaryPage
import pages.rentalsandrentaroom.RentalsRaRAboutCompletePage
import pages.ukandforeignproperty.UkAndForeignPropertySummaryPage._
import pages.{SummaryPage, isSelected}
import service.{BusinessService, CYADiversionService, ForeignCYADiversionService, UkAndForeignCYADiversionService}
import viewmodels.summary.{TaskListItem, TaskListTag}

case class UkAndForeignPropertySummaryPage(
  taxYear: Int,
  startItems: Seq[TaskListItem],
  ukPropertyRentalListItems: Seq[TaskListItem],
  ukPropertyRentARoomListItems: Seq[TaskListItem],
  ukPropertyRentalAndRentARoomListItems: Seq[TaskListItem],
  foreignListItems: Seq[(CountryName, Seq[TaskListItem])]
)

object UkAndForeignPropertySummaryPage {
  type CountryName = String
  def apply(
    taxYear: Int,
    ukAccrualsOrCash: Boolean,
    foreignAccrualsOrCash: Boolean,
    userAnswers: Option[UserAnswers],
    cyaDiversionService: CYADiversionService,
    foreignCYADiversionService: ForeignCYADiversionService,
    ukAndForeignCYADiversionService: UkAndForeignCYADiversionService
  ): UkAndForeignPropertySummaryPage = {
    val isClaimPIA: Boolean = userAnswers
      .flatMap(
        _.get(UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage)
          .map(_.isClaimPropertyIncomeAllowanceOrExpenses)
      )
      .contains(true)
    val isClaimRelief: Option[Boolean] = userAnswers
      .flatMap(
        _.get(UkAndForeignPropertyClaimExpensesOrReliefPage)
          .map(_.isClaimExpensesOrRelief)
      )

    val foreignPropertyTaskListItems =
      foreignTaskList(taxYear, userAnswers, isClaimPIA, foreignAccrualsOrCash, foreignCYADiversionService)

    val (ukRentals, ukRentARoom, ukRentalsAndRentARoom) = getUkPropertyTaskListItems(
      taxYear,
      userAnswers,
      isClaimPIA,
      isClaimRelief.contains(true),
      ukAccrualsOrCash,
      cyaDiversionService
    )

    UkAndForeignPropertySummaryPage(
      taxYear = taxYear,
      startItems = aboutItems(taxYear, userAnswers, ukAndForeignCYADiversionService),
      ukPropertyRentalListItems = ukRentals,
      ukPropertyRentARoomListItems = ukRentARoom,
      ukPropertyRentalAndRentARoomListItems = ukRentalsAndRentARoom,
      foreignListItems = foreignPropertyTaskListItems
    )
  }

  def getUkPropertyTaskListItems(
    taxYear: Int,
    userAnswers: Option[UserAnswers],
    isClaimPIA: Boolean,
    isClaimRelief: Boolean,
    isAccruals: Boolean,
    cyaDiversionService: CYADiversionService
  ): (Seq[TaskListItem], Seq[TaskListItem], Seq[TaskListItem]) = {
    val isUkPropertyRentalsSelected: Boolean = isSelected(userAnswers, UkAndForeignPropertyRentalTypeUk.PropertyRentals)
    val isUkRentARoomSelected: Boolean = isSelected(userAnswers, UkAndForeignPropertyRentalTypeUk.RentARoom)

    val emptyTaskItemList = Seq.empty[TaskListItem]

    (isUkPropertyRentalsSelected, isUkRentARoomSelected) match {
      case (true, false) =>
        (
          ukPropertyRentalTaskList(taxYear, userAnswers, isClaimPIA, isAccruals, cyaDiversionService),
          emptyTaskItemList,
          emptyTaskItemList
        )
      case (false, true) =>
        (
          emptyTaskItemList,
          ukRentARoomTaskList(taxYear, userAnswers, isClaimRelief, cyaDiversionService),
          emptyTaskItemList
        )
      case (true, true) =>
        (
          emptyTaskItemList,
          emptyTaskItemList,
          ukPropertyRentalAndRentARoomTaskList(
            taxYear,
            userAnswers,
            isClaimRelief,
            isClaimPIA,
            isAccruals,
            cyaDiversionService
          )
        )
      case (false, false) =>
        (emptyTaskItemList, emptyTaskItemList, emptyTaskItemList)
    }
  }

  def aboutItems(
    taxYear: Int,
    userAnswers: Option[UserAnswers],
    ukAndForeignCYADiversionService: UkAndForeignCYADiversionService
  ): Seq[TaskListItem] = {
    val isAboutSectionComplete = userAnswers.flatMap(_.get(SectionCompletePage))
    val taskListTag = isAboutSectionComplete
      .map(haveYouFinished => if (haveYouFinished) TaskListTag.Completed else TaskListTag.InProgress)
      .getOrElse {
        if (isAboutSectionComplete.isDefined) {
          TaskListTag.InProgress
        } else {
          TaskListTag.NotStarted
        }
      }

    Seq(
      TaskListItem(
        "summary.aboutUKAndForeignProperties",
        ukAndForeignCYADiversionService
          .redirectCallToCYAIfFinished(taxYear, userAnswers, UkAndForeignCYADiversionService.ABOUT) {
            controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear)
          },
        taskListTag,
        "uk_and_foreign_property_about_link"
      )
    )
  }

  def ukPropertyRentalTaskList(
    taxYear: Int,
    userAnswers: Option[UserAnswers],
    claimPIA: Boolean,
    isAccruals: Boolean,
    cyaDiversionService: CYADiversionService
  ): Seq[TaskListItem] = {

    val ukSummary = SummaryPage(cyaDiversionService)
    claimPIA match {
      case true =>
        Seq(
          ukSummary.propertyRentalsAdjustmentsItem(userAnswers, taxYear)
        )
      case false if isAccruals =>
        Seq(
          ukSummary.propertyRentalsIncomeItem(userAnswers, taxYear),
          ukSummary.propertyRentalsExpensesItem(userAnswers, taxYear),
          ukSummary.propertyAllowancesItem(taxYear, userAnswers),
          ukSummary.structuresAndBuildingAllowanceItem(userAnswers, taxYear),
          ukSummary.rentalsEsbaItem(userAnswers, taxYear),
          ukSummary
            .propertyRentalsAdjustmentsItem(userAnswers, taxYear)
        )
      case false =>
        Seq(
          ukSummary.propertyRentalsIncomeItem(userAnswers, taxYear),
          ukSummary.propertyRentalsExpensesItem(userAnswers, taxYear),
          ukSummary.propertyAllowancesItem(taxYear, userAnswers),
          ukSummary.propertyRentalsAdjustmentsItem(userAnswers, taxYear)
        )
    }
  }

  def ukRentARoomTaskList(
    taxYear: Int,
    userAnswers: Option[UserAnswers],
    claimRelief: Boolean,
    cyaDiversionService: CYADiversionService
  ): Seq[TaskListItem] = {
    val ukSummary = SummaryPage(cyaDiversionService)

    if (claimRelief) {
      Seq(ukSummary.ukRentARoomAboutItem(userAnswers, taxYear))
    } else {
      Seq(
        ukSummary.ukRentARoomAboutItem(userAnswers, taxYear),
        ukSummary.ukRentARoomExpensesItem(userAnswers, taxYear),
        ukSummary.ukRentARoomAllowancesItem(userAnswers, taxYear),
        ukSummary.ukRentARoomAdjustmentsItem(userAnswers, taxYear)
      )
    }
  }

  def ukPropertyRentalAndRentARoomTaskList(
    taxYear: Int,
    userAnswers: Option[UserAnswers],
    claimRelief: Boolean,
    claimPIA: Boolean,
    isAccruals: Boolean,
    cyaDiversionService: CYADiversionService
  ): Seq[TaskListItem] = {

    val ukSummary = SummaryPage(cyaDiversionService)

    val isAboutComplete = userAnswers.flatMap(_.get(RentalsRaRAboutCompletePage)).getOrElse(false)
    if (!isAboutComplete) {
      Seq(
        ukSummary.rentalsAndRaRAboutItem(taxYear, userAnswers)
      )
    } else {

      (claimRelief, claimPIA) match {
        case (true, true) =>
          Seq(
            ukSummary.rentalsAndRaRAboutItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRAdjustmentsItem(taxYear, userAnswers)
          )
        case (true, false) if isAccruals =>
          Seq(
            ukSummary.rentalsAndRaRAboutItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRIncomeItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRExpensesItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRAllowancesItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRSBAItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRESBAItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRAdjustmentsItem(taxYear, userAnswers)
          )
        case (true, false) =>
          Seq(
            ukSummary.rentalsAndRaRAboutItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRIncomeItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRExpensesItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRAllowancesItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRAdjustmentsItem(taxYear, userAnswers)
          )
        case (false, true) =>
          Seq(
            ukSummary.rentalsAndRaRAboutItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRExpensesItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRAllowancesItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRAdjustmentsItem(taxYear, userAnswers)
          )
        case (false, false) if isAccruals =>
          Seq(
            ukSummary.rentalsAndRaRAboutItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRIncomeItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRExpensesItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRAllowancesItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRSBAItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRESBAItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRAdjustmentsItem(taxYear, userAnswers)
          )
        case (false, false) =>
          Seq(
            ukSummary.rentalsAndRaRAboutItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRIncomeItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRExpensesItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRAllowancesItem(taxYear, userAnswers),
            ukSummary.rentalsAndRaRAdjustmentsItem(taxYear, userAnswers)
          )
      }
    }
  }

  def foreignTaskList(
    taxYear: Int,
    userAnswers: Option[UserAnswers],
    claimPIA: Boolean,
    isAccruals: Boolean,
    foreignCYADiversionService: ForeignCYADiversionService
  ): Seq[(CountryName, Seq[TaskListItem])] = {

    val res = for {
      foreignCountries <- userAnswers.flatMap(_.get(SelectCountryPage))
      list =
        foreignCountries.map(c =>
          (
            c.name,
            getTaskListForForeignCountry(
              taxYear,
              claimPIA,
              isAccruals,
              c.code,
              userAnswers,
              foreignCYADiversionService
            )
          )
        )
    } yield list
    res.getOrElse(Seq.empty)
  }

  def getTaskListForForeignCountry(
    taxYear: Int,
    claimPIA: Boolean,
    isAccruals: Boolean,
    countryCode: String,
    userAnswers: Option[UserAnswers],
    foreignCYADiversionService: ForeignCYADiversionService
  ): Seq[TaskListItem] = {
    val foreignSummary = ForeignSummaryPage(foreignCYADiversionService)

    val foreignTaxAndAdjustmentsItems = Seq(
      foreignSummary.foreignTaxItem(taxYear, countryCode, userAnswers),
      foreignSummary.foreignAdjustmentsItem(taxYear, countryCode, claimPIA, userAnswers)
    )
    val foreignFullTaskList = Seq(
      foreignSummary.foreignTaxItem(taxYear, countryCode, userAnswers),
      foreignSummary.foreignIncomeItem(taxYear, countryCode, userAnswers),
      foreignSummary.foreignExpensesItem(taxYear, countryCode, userAnswers),
      foreignSummary.foreignAllowancesItem(taxYear, countryCode, userAnswers),
      foreignSummary.foreignSBAItem(taxYear, countryCode, userAnswers),
      foreignSummary.foreignAdjustmentsItem(taxYear, countryCode, claimPIA, userAnswers)
    )
    val foreignNoSBATaskList = Seq(
      foreignSummary.foreignTaxItem(taxYear, countryCode, userAnswers),
      foreignSummary.foreignIncomeItem(taxYear, countryCode, userAnswers),
      foreignSummary.foreignExpensesItem(taxYear, countryCode, userAnswers),
      foreignSummary.foreignAllowancesItem(taxYear, countryCode, userAnswers),
      foreignSummary.foreignAdjustmentsItem(taxYear, countryCode, claimPIA, userAnswers)
    )

    (claimPIA, isAccruals) match {
      case (true, _)      => foreignTaxAndAdjustmentsItems
      case (false, true)  => foreignFullTaskList
      case (false, false) => foreignNoSBATaskList
    }
  }
}
