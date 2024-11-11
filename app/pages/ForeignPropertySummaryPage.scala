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

package pages

import models.NormalMode
import viewmodels.summary.{TaskListItem, TaskListTag}

case class ForeignPropertySummaryPage(taxYear: Int, startItems: Seq[TaskListItem], countries: Seq[TaskListItem])

object ForeignPropertySummaryPage {

  def propertyAboutItems(taxYear: Int): Seq[TaskListItem] =
    Seq(
      TaskListItem(
        "foreign.selectCountry",
        controllers.foreign.routes.ForeignPropertyDetailsController.onPageLoad(taxYear),
        TaskListTag.NotStarted,
        "foreign_property_select_country"
      )
    )

  def propertyCountries(taxYear: Int): Seq[TaskListItem] = {
    Seq(
      TaskListItem(
        "foreign.foreignTax",
        controllers.foreign.routes.ForeignIncomeTaxController.onPageLoad(taxYear,"ESP", NormalMode),
        TaskListTag.NotStarted,
        "foreign_property_select_country"
      )
    )
  }

}
