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

package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.all.HintViewModel
import viewmodels.govuk.checkbox._

sealed trait UkAndForeignPropertyRentalTypeUk

object UkAndForeignPropertyRentalTypeUk extends Enumerable.Implicits {

  case object PropertyRentals extends WithName("propertyRentals") with UkAndForeignPropertyRentalTypeUk
  case object RentARoom extends WithName("rentARoom") with UkAndForeignPropertyRentalTypeUk

  val values: Seq[UkAndForeignPropertyRentalTypeUk] = Seq(
    PropertyRentals,
    RentARoom
  )

  def checkboxItems(implicit messages: Messages, individualOrAgent: String): Seq[CheckboxItem] =
    values.zipWithIndex.map {
      case (value, index) =>
        CheckboxItemViewModel(
          content = Text(messages(s"ukAndForeignPropertyRentalTypeUk.${value.toString}")),
          fieldId = "value",
          index   = index,
          value   = value.toString
        ).withHint(HintViewModel(Text(messages(s"ukAndForeignPropertyRentalTypeUk.${value.toString}.hint.${individualOrAgent}"))))
    }

  implicit val enumerable: Enumerable[UkAndForeignPropertyRentalTypeUk] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
