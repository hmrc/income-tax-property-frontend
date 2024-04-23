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

import pages.PageConstants
import play.api.libs.json.{JsPath, Json, OFormat}
import queries.Gettable

case class PropertyRentalsExpense(consolidatedExpensesYesNo: Option[Boolean],
                                  consolidatedExpensesAmount: Option[BigDecimal],
                                  rentsRatesAndInsurance: Option[BigDecimal],
                                  repairsAndMaintenanceCosts: Option[BigDecimal],
                                  loanInterestOrOtherFinancialCost: Option[BigDecimal],
                                  otherProfessionalFees: Option[BigDecimal],
                                  costsOfServicesProvided: Option[BigDecimal],
                                  propertyBusinessTravelCosts: Option[BigDecimal],
                                  otherAllowablePropertyExpenses: Option[BigDecimal])

case object PropertyRentalsExpense extends Gettable[PropertyRentalsExpense] {

  implicit val formats: OFormat[PropertyRentalsExpense] = Json.format[PropertyRentalsExpense]

  override def path: JsPath = JsPath \ toString

  override def toString: String = PageConstants.propertyRentalsExpense
}
