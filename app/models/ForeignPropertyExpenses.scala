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

import pages.PageConstants.expensesPath
import play.api.libs.json.{Format, JsPath, Json}
import queries.{Gettable, Settable}

case class ForeignPropertyExpenses(
  countryCode: String,
  consolidatedExpenses: Option[ConsolidatedOrIndividualExpenses],
  premisesRunningCosts: Option[BigDecimal],
  repairsAndMaintenance: Option[BigDecimal],
  financialCosts: Option[BigDecimal],
  professionalFees: Option[BigDecimal],
  costOfServices: Option[BigDecimal],
  other: Option[BigDecimal]
)

object ForeignPropertyExpenses extends Gettable[ForeignPropertyExpenses] with Settable[ForeignPropertyExpenses] {

  implicit val format: Format[ForeignPropertyExpenses] = Json.format[ForeignPropertyExpenses]

  override def path: JsPath = JsPath \ expensesPath(ForeignProperty)
}

case class ReadForeignPropertyExpenses(countryCode: String) extends Gettable[ForeignPropertyExpenses] {

  override def path: JsPath = JsPath \ expensesPath(ForeignProperty) \ toString

  override def toString: String = countryCode.toUpperCase

}
