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

package audit

import models.TotalIncome._
import models.UKPropertySelect
import play.api.libs.json.{Format, Json}

case class PropertyAbout(
  isTotalIncomeUnder1k: Boolean,
  isTotalIncomeBetween1kAnd85k: Boolean,
  isTotalIncomeOver85k: Boolean,
  ukProperty: Option[Seq[UKPropertySelect]],
  isReportPropertyIncome: Option[Boolean]
)

object PropertyAbout {
  implicit val format: Format[PropertyAbout] = Json.format[PropertyAbout]

  def apply(propertyAbout: models.PropertyAbout): PropertyAbout = {
    val (under, between, over) = propertyAbout.totalIncome match {
      case Under =>   (true, false, false)
      case Between => (false, true, false)
      case Over =>    (false, false, true)
    }
    PropertyAbout(
      isTotalIncomeUnder1k = under,
      isTotalIncomeBetween1kAnd85k = between,
      isTotalIncomeOver85k = over,
      ukProperty = propertyAbout.ukProperty,
      isReportPropertyIncome = propertyAbout.isReportPropertyIncome
    )
  }
}
