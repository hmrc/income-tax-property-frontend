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

package audit

import models.{CapitalAllowancesForACar, Rentals}
import pages.PageConstants.allowancesPath
import play.api.libs.json.{Format, JsPath, Json}
import queries.{Gettable, Settable}

case class RentalsAllowance(
  capitalAllowancesForACar: Option[CapitalAllowancesForACar],
  annualInvestmentAllowance: Option[BigDecimal],
  zeroEmissionCarAllowance: Option[BigDecimal],
  zeroEmissionGoodsVehicleAllowance: Option[BigDecimal],
  businessPremisesRenovationAllowance: Option[BigDecimal],
  replacementOfDomesticGoodsAllowance: Option[BigDecimal],
  otherCapitalAllowance: Option[BigDecimal]
)

case object RentalsAllowance extends Gettable[RentalsAllowance] with Settable[RentalsAllowance] {
  implicit val format: Format[RentalsAllowance] = Json.format[RentalsAllowance]

  override def path: JsPath = JsPath \ toString

  override def toString: String = allowancesPath(Rentals)
}
