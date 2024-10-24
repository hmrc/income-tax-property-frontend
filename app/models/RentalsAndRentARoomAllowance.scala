/*
 * Copyright 2023 HM Revenue & Customs
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

import pages.PageConstants.allowancesPath
import play.api.libs.json.{Format, JsPath, Json}
import queries.{Gettable, Settable}

final case class RentalsAndRentARoomAllowance(
  capitalAllowancesForACar: Option[CapitalAllowancesForACar],
  annualInvestmentAllowance: Option[BigDecimal],
  zeroEmissionCarAllowance: Option[BigDecimal],
  zeroEmissionGoodsVehicleAllowance: Option[BigDecimal],
  businessPremisesRenovationAllowance: Option[BigDecimal],
  replacementOfDomesticGoodsAllowance: Option[BigDecimal],
  otherCapitalAllowance: Option[BigDecimal]
)

case object RentalsAndRentARoomAllowance
    extends Gettable[RentalsAndRentARoomAllowance] with Settable[RentalsAndRentARoomAllowance] {
  implicit val format: Format[RentalsAndRentARoomAllowance] = Json.format[RentalsAndRentARoomAllowance]

  override def path: JsPath = JsPath \ toString

  override def toString: String = allowancesPath(RentalsRentARoom)
}
