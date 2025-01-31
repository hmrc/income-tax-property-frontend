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

import models.{UKPropertySelect, UserAnswers}
import pages.foreign.ForeignSummaryPage
import pages.{SummaryPage, isSelected}
import service.{CYADiversionService, ForeignCYADiversionService}
import viewmodels.summary.{TaskListItem, TaskListTag}


case class UkAndForeignPropertySummaryPage(
                                            taxYear: Int,
                                            startItems: Seq[TaskListItem]
                                          )

object UkAndForeignPropertySummaryPage {

  def ukAndForeignPropertyAboutItems(taxYear: Int, userAnswers: Option[UserAnswers], cyaDiversionService: CYADiversionService, foreignCYADiversionService: ForeignCYADiversionService): Seq[TaskListItem] = {

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

    Seq(
      TaskListItem(
        "summary.aboutUKAndForeignProperties",
        controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
        TaskListTag.NotStarted, //TODO complete logic to make the status work correctly
        "uk_and_foreign_property_about_link"
      )
    )
  }
}