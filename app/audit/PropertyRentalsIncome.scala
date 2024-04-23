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

case class PropertyRentalsIncome(calculatedFigureYourself: Option[Boolean],
                                 calculatedFigureYourselfAmount: Option[BigDecimal],
                                 isNonUKLandlord: Option[BigDecimal],
                                 incomeFromPropertyRentals: Option[BigDecimal],
                                 leaseGrant: Option[BigDecimal],
                                 leasePeriodInMonths: Option[BigDecimal],
                                 premiumTaxableAmount: Option[BigDecimal],
                                 reversePremiumsReceived: Option[BigDecimal],
                                 reversePremiumsReceivedAmount: Option[BigDecimal],
                                 otherIncomeFromProperty: Option[BigDecimal])

case object PropertyRentalsIncome extends Gettable[PropertyRentalsIncome] {

  implicit val formats: OFormat[PropertyRentalsIncome] = Json.format[PropertyRentalsIncome]

  override def path: JsPath = JsPath \ toString

  override def toString: String = PageConstants.propertyRentalsIncome
}
