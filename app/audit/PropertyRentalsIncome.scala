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

import models.{CalculatedFigureYourself, DeductingTax, PremiumsGrantLease, ReversePremiumsReceived}
import pages.PageConstants
import play.api.libs.json.{Format, JsPath, Json}
import queries.{Gettable, Settable}

case class PropertyRentalsIncome(
  isNonUKLandlord: Boolean,
  incomeFromPropertyRentals: BigDecimal,
  otherIncomeFromProperty: BigDecimal,
  deductingTax: Option[DeductingTax],
  calculatedFigureYourself: Option[CalculatedFigureYourself],
  yearLeaseAmount: Option[BigDecimal],
  receivedGrantLeaseAmount: Option[BigDecimal],
  premiumsGrantLease: Option[PremiumsGrantLease],
  reversePremiumsReceived: Option[ReversePremiumsReceived]
)

case object PropertyRentalsIncome extends Gettable[PropertyRentalsIncome] with Settable[PropertyRentalsIncome] {

  implicit val formats: Format[PropertyRentalsIncome] = Json.format[PropertyRentalsIncome]

  override def path: JsPath = JsPath \ toString

  override def toString: String = PageConstants.propertyRentalsIncome
}
