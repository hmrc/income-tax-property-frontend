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

package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait WhenYouReportedTheLoss

object WhenYouReportedTheLoss extends Enumerable.Implicits {

  case object y2018to2019 extends WithName("y2018to2019") with WhenYouReportedTheLoss
  case object y2019to2020 extends WithName("y2019to2020") with WhenYouReportedTheLoss
  case object y2020to2021 extends WithName("y2020to2021") with WhenYouReportedTheLoss
  case object y2021to2022 extends WithName("y2021to2022") with WhenYouReportedTheLoss
  case object y2022to2023 extends WithName("y2022to2023") with WhenYouReportedTheLoss

  val values: Seq[WhenYouReportedTheLoss] = Seq(
      y2018to2019,
      y2019to2020,
      y2020to2021,
      y2021to2022,
      y2022to2023
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"whenYouReportedTheLoss.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[WhenYouReportedTheLoss] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
