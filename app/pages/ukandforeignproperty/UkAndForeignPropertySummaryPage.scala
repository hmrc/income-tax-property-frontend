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

import models.{UKPropertySelect, UkAndForeignPropertyRentalTypeUk, UserAnswers}
import pages.foreign.ForeignSummaryPage
import pages.ukandforeignproperty.UkAndForeignPropertySummaryPage._
import pages.{SummaryPage, UkAndForeignPropertyRentalTypeUkPage, foreign, isSelected}
import service.{CYADiversionService, ForeignCYADiversionService}
import viewmodels.summary.{TaskListItem, TaskListTag}

case class UkAndForeignPropertySummaryPage(
                                            taxYear: Int,
                                            startItems: Seq[TaskListItem],
                                            ukPropertyRentalListItems:Seq[TaskListItem],
                                            ukPropertyRentAroomListItems:Seq[TaskListItem],
                                            ukPropertyRentalAndRentARoomListItems:Seq[TaskListItem],
                                            foreignListItems:Seq[(CountryName, Seq[TaskListItem])]
                                          )
//ToDo this is a dummy implementation
object UkAndForeignPropertySummaryPage {
  type CountryName = String

  def apply(taxYear: Int,
            userAnswers: Option[UserAnswers],
            cyaDiversionService: CYADiversionService,
            foreignCYADiversionService: ForeignCYADiversionService
           ):UkAndForeignPropertySummaryPage =
    UkAndForeignPropertySummaryPage(
      taxYear,
      aboutItems(taxYear, userAnswers, cyaDiversionService, foreignCYADiversionService),
      ukPropertyRentalTaskList(taxYear, userAnswers, cyaDiversionService, foreignCYADiversionService),
      ukRentARoomTaskList(taxYear, userAnswers, cyaDiversionService, foreignCYADiversionService),
      ukPropertyREntalAndRentARoomTaskList(taxYear, userAnswers, cyaDiversionService, foreignCYADiversionService),
      foreignTaskList(taxYear, userAnswers, cyaDiversionService, foreignCYADiversionService)
    )

  def aboutItems(taxYear: Int,
                 userAnswers: Option[UserAnswers],
                 cyaDiversionService: CYADiversionService,
                 foreignCYADiversionService: ForeignCYADiversionService): Seq[TaskListItem] = {

    val summaryPage = SummaryPage(cyaDiversionService)
    val foreignSummaryPage = ForeignSummaryPage(foreignCYADiversionService)

    val ukPropertyItems: Seq[TaskListItem] = if (isSelected(userAnswers, UKPropertySelect.PropertyRentals)) {
      summaryPage.propertyAboutItems(userAnswers, taxYear)
    } else {
      Seq.empty
    }

    val foreignPropertyItems: Seq[TaskListItem] = foreignSummaryPage.foreignPropertyAboutItems(taxYear, userAnswers)

    val ukPropertyComplete = ukPropertyItems.exists(_.taskListTag == TaskListTag.Completed)
    val foreignPropertyComplete = foreignPropertyItems.exists(_.taskListTag == TaskListTag.Completed)

    val res = userAnswers.flatMap(_.get(UkAndForeignPropertyRentalTypeUkPage)).getOrElse(Set.empty).nonEmpty

    val combinedTaskListTag = (ukPropertyComplete, foreignPropertyComplete, res) match {
      case (true, true,true) => TaskListTag.Completed
      case (true, true,_) => TaskListTag.NotStarted
      case _ => TaskListTag.CanNotStart
    }

    Seq(
      TaskListItem(
        "summary.aboutUKAndForeignProperties",
        controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
        combinedTaskListTag, //TODO complete logic to make the status work correctly
        "uk_and_foreign_property_about_link"
      )
    )
  }


  def ukPropertyRentalTaskList(taxYear: Int,
                 userAnswers: Option[UserAnswers],
                 cyaDiversionService: CYADiversionService,
                 foreignCYADiversionService: ForeignCYADiversionService): Seq[TaskListItem] = {

    import UkAndForeignPropertyRentalTypeUk._
    val res = userAnswers.flatMap(_.get(UkAndForeignPropertyRentalTypeUkPage)).getOrElse(Set.empty).toSeq
    if(res.isEmpty || res.size == 2)
      Seq.empty
    else{
      val filtered  = res.filter(_ == UkAndForeignPropertyRentalTypeUk.PropertyRentals)

      if(filtered.nonEmpty)
      Seq(
        TaskListItem(
          "ukAndForeign.ukList.rentAroom.about",
          controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "uk_and_foreign_property_about_link"
        ),
        TaskListItem(
          "ukAndForeign.ukList.rentAroom.expenses",
          controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "uk_and_foreign_property_expenses_link"
        ),
        TaskListItem(
          "ukAndForeign.ukList.rentAroom.allowances",
          controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "uk_and_foreign_property_allowances_link"
        ),
        TaskListItem(
          "ukAndForeign.foreignList.structuresAndBuildingAllowance",
          controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "uk_and_foreign_property_tax_link"
        ),
        TaskListItem(
          "ukAndForeign.ukList.propertyRental.enhancedStructuresAndBuildingAllowance",
          controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "uk_and_foreign_property_tax_link"
        ),
        TaskListItem(
          "ukAndForeign.ukList.rentAroom.adjustments",
          controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "uk_and_foreign_property_adjustments_link"
        )
      )
    else
      Seq.empty
      }
  }

  def ukRentARoomTaskList(taxYear: Int,
                 userAnswers: Option[UserAnswers],
                 cyaDiversionService: CYADiversionService,
                 foreignCYADiversionService: ForeignCYADiversionService): Seq[TaskListItem] = {

    import UkAndForeignPropertyRentalTypeUk._
    val res = userAnswers.flatMap(_.get(UkAndForeignPropertyRentalTypeUkPage)).getOrElse(Set.empty).toSeq
    if(res.isEmpty || res.size == 2)
      Seq.empty
    else {
      val filtered  = res.filter(_ == UkAndForeignPropertyRentalTypeUk.RentARoom)

      if(filtered.nonEmpty)
        Seq(
          TaskListItem(
            "ukAndForeign.ukList.rentAroom.about",
            controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
            TaskListTag.NotStarted,
            "uk_and_foreign_property_about_link"
          ),
          TaskListItem(
            "ukAndForeign.ukList.rentAroom.expenses",
            controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
            TaskListTag.NotStarted,
            "uk_and_foreign_property_expenses_link"
          ),
          TaskListItem(
            "ukAndForeign.ukList.rentAroom.allowances",
            controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
            TaskListTag.NotStarted,
            "uk_and_foreign_property_allowances_link"
          ),
          TaskListItem(
            "ukAndForeign.foreignList.structuresAndBuildingAllowance",
            controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
            TaskListTag.NotStarted,
            "uk_and_foreign_property_tax_link"
          ),
          TaskListItem(
            "ukAndForeign.ukList.propertyRental.enhancedStructuresAndBuildingAllowance",
            controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
            TaskListTag.NotStarted,
            "uk_and_foreign_property_tax_link"
          ),
          TaskListItem(
            "ukAndForeign.ukList.rentAroom.adjustments",
            controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
            TaskListTag.NotStarted,
            "uk_and_foreign_property_adjustments_link"
          )
        )
      else
        Seq.empty
    }
  }

  def ukPropertyREntalAndRentARoomTaskList(taxYear: Int,
                               userAnswers: Option[UserAnswers],
                               cyaDiversionService: CYADiversionService,
                               foreignCYADiversionService: ForeignCYADiversionService): Seq[TaskListItem] = {

    import UkAndForeignPropertyRentalTypeUk._
    val res = userAnswers.flatMap(_.get(UkAndForeignPropertyRentalTypeUkPage)).filter(_.contains(UkAndForeignPropertyRentalTypeUk.RentARoom)).getOrElse(List.empty)
    if(res.size == 2)
      Seq(
        TaskListItem(
          "ukAndForeign.ukList.rentAroom.about",
          controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "uk_and_foreign_property_about_link"
        ),
        TaskListItem(
          "ukAndForeign.ukList.rentAroom.expenses",
          controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "uk_and_foreign_property_expenses_link"
        ),
        TaskListItem(
          "ukAndForeign.ukList.rentAroom.allowances",
          controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "uk_and_foreign_property_allowances_link"
        ),
        TaskListItem(
          "ukAndForeign.ukList.rentAroom.adjustments",
          controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "uk_and_foreign_property_adjustments_link"
        )
      )
    else
      Seq.empty
  }

  def foreignTaskList(
                       taxYear: Int,
                       userAnswers: Option[UserAnswers],
                       cyaDiversionService: CYADiversionService,
                       foreignCYADiversionService: ForeignCYADiversionService
                     ): Seq[(CountryName, Seq[TaskListItem])] = {

    val res = for {
      foreignCountries <- userAnswers.flatMap(_.get(SelectCountryPage)) //TODO  check if this stil ojk fopr UKandForeoign
      list =  foreignCountries.map(c => (c.name,  getTasklistForForeignCountry(taxYear,c, userAnswers, foreignCYADiversionService))).toList
    } yield list
    res.getOrElse(Seq.empty)
  }

  private def getTasklistForForeignCountry(
    taxYear: Int,
    country: foreign.Country,
    userAnswers: Option[UserAnswers],
    foreignCYADiversionService: ForeignCYADiversionService
  ): Seq[TaskListItem] =
      Seq(
        TaskListItem(
          "ukAndForeign.foreignList.tax",
          controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "uk_and_foreign_property_tax_link"
        ),
        TaskListItem(
          "ukAndForeign.foreignList.income",
          controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "uk_and_foreign_property_tax_link"
        ),
        TaskListItem(
          "ukAndForeign.foreignList.expenses",
          controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "uk_and_foreign_property_tax_link"
        ),
        TaskListItem(
          "ukAndForeign.foreignList.allowances",
          controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "uk_and_foreign_property_tax_link"
        ),
        TaskListItem(
          "ukAndForeign.foreignList.structuresAndBuildingAllowance",
          controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "uk_and_foreign_property_tax_link"
        ),
        TaskListItem(
          "ukAndForeign.foreignList.adjustments",
          controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "uk_and_foreign_property_tax_link"
        )
      )
}
