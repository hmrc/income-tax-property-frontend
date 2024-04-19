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
import play.api.libs.json.{JsPath, Json}
import queries.Gettable

final case class Adjustments(privateAdjustment: PrivateUseAdjustment,
                             balancingCharge: BalancingCharge,
                             propertyIncomeAllowance: BigDecimal,
                             renovationAllowanceBalancingCharge: RenovationAllowanceBalancingCharge,
                             residentialFinancialCost: BigDecimal,
                             unusedResidencialFinanceCost: BigDecimal)

object Adjustments extends Gettable[Adjustments] {
  implicit val format = Json.format[Adjustments]

  override def path: JsPath = JsPath \ PageConstants.adjustments
}
