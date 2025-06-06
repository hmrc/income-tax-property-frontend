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

import pages.PageConstants.adjustmentsPath
import play.api.libs.json.{Format, JsPath, Json}
import queries.{Gettable, Settable}

final case class RentalsAndRentARoomAdjustment(
  privateUseAdjustment: BigDecimal,
  balancingCharge: BalancingCharge,
  propertyIncomeAllowance: Option[BigDecimal],
  renovationAllowanceBalancingCharge: RenovationAllowanceBalancingCharge,
  residentialFinanceCost: BigDecimal,
  unusedResidentialFinanceCost: Option[BigDecimal],
  unusedLossesBroughtForward: Option[UnusedLossesBroughtForward],
  whenYouReportedTheLoss: Option[WhenYouReportedTheLoss]
)

case object RentalsAndRentARoomAdjustment
    extends Gettable[RentalsAndRentARoomAdjustment] with Settable[RentalsAndRentARoomAdjustment] {
  implicit val format: Format[RentalsAndRentARoomAdjustment] = Json.format[RentalsAndRentARoomAdjustment]

  override def path: JsPath = JsPath \ toString

  override def toString: String = adjustmentsPath(RentalsRentARoom)
}
