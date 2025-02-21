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

import models.{CapitalAllowancesForACar, ForeignProperty}
import pages.PageConstants.allowancesPath
import play.api.libs.json.{Format, JsPath, Json}
import queries.{Gettable, Settable}

case class ForeignPropertyAllowances(
  countryCode: String,
  zeroEmissionsCarAllowance: Option[BigDecimal],
  zeroEmissionsGoodsVehicleAllowance: Option[BigDecimal],
  costOfReplacingDomesticItems: Option[BigDecimal],
  otherCapitalAllowance: Option[BigDecimal],
  capitalAllowancesForACar: Option[CapitalAllowancesForACar]
)

case object ForeignPropertyAllowances
    extends Gettable[ForeignPropertyAllowances] with Settable[ForeignPropertyAllowances] {

  implicit val format: Format[ForeignPropertyAllowances] = Json.format[ForeignPropertyAllowances]

  override def path: JsPath = JsPath \ toString

  override def toString: String = allowancesPath(ForeignProperty)
}

case class ReadForeignPropertyAllowances(countryCode: String) extends Gettable[ForeignPropertyAllowances] {

  override def path: JsPath = JsPath \ allowancesPath(ForeignProperty) \ toString

  override def toString: String = countryCode.toUpperCase

}
