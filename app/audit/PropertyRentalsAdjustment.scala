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

import models.{BalancingCharge, PrivateUseAdjustment, RenovationAllowanceBalancingCharge}
import pages.PageConstants
import play.api.libs.json.{Format, JsPath, Json}
import queries.Gettable

final case class PropertyRentalsAdjustment(
  privateUseAdjustment: PrivateUseAdjustment,
  balancingCharge: BalancingCharge,
  propertyIncomeAllowance: BigDecimal,
  renovationAllowanceBalancingCharge: RenovationAllowanceBalancingCharge,
  residentialFinanceCost: BigDecimal,
  unusedResidentialFinanceCost: BigDecimal
)

object PropertyRentalsAdjustment extends Gettable[PropertyRentalsAdjustment] {
  implicit val format: Format[PropertyRentalsAdjustment] = Json.format[PropertyRentalsAdjustment]

  override def path: JsPath = JsPath \ toString

  override def toString: String = PageConstants.propertyRentalsAdjustment
}
