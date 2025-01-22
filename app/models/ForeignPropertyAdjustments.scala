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

import pages.PageConstants.adjustmentsPath
import play.api.libs.json.{Format, JsPath, Json}
import queries.{Gettable, Settable}

case class ForeignPropertyAdjustments(
  countryCode: String,
  privateUseAdjustment: BigDecimal,
  balancingCharge: BalancingCharge,
  residentialFinanceCost: Option[BigDecimal],
  unusedResidentialFinanceCost: Option[ForeignUnusedResidentialFinanceCost],
  propertyIncomeAllowanceClaim: Option[BigDecimal],
  unusedLossesPreviousYears: UnusedLossesPreviousYears,
  whenYouReportedTheLoss: Option[ForeignWhenYouReportedTheLoss]
)

object ForeignPropertyAdjustments
    extends Gettable[ForeignPropertyAdjustments] with Settable[ForeignPropertyAdjustments] {

  implicit val format: Format[ForeignPropertyAdjustments] = Json.format[ForeignPropertyAdjustments]

  override def path: JsPath = JsPath \ adjustmentsPath(ForeignProperty)
}

case class ReadForeignPropertyAdjustments(countryCode: String) extends Gettable[ForeignPropertyAdjustments] {

  override def path: JsPath = JsPath \ adjustmentsPath(ForeignProperty) \ toString

  override def toString: String = countryCode.toUpperCase

}
