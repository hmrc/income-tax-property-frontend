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

import pages.PageConstants.incomePath
import play.api.libs.json.{Format, JsPath, Json}
import queries.{Gettable, Settable}

case class ForeignPropertyIncome(
  countryCode: String,
  rentIncome: BigDecimal,
  premiumsGrantLeaseReceived: Boolean,
  otherPropertyIncome: BigDecimal,
  calculatedPremiumLeaseTaxable: Option[CalculatedPremiumLeaseTaxable],
  receivedGrantLeaseAmount: Option[BigDecimal],
  twelveMonthPeriodsInLease: Option[BigDecimal],
  premiumsOfLeaseGrantAgreed: Option[PremiumsOfLeaseGrantAgreed]
)

object ForeignPropertyIncome extends Gettable[ForeignPropertyIncome] with Settable[ForeignPropertyIncome] {

  implicit val format: Format[ForeignPropertyIncome] = Json.format[ForeignPropertyIncome]

  override def path: JsPath = JsPath \ incomePath(ForeignProperty)
}

case class ReadForeignPropertyIncome(countryCode: String) extends Gettable[ForeignPropertyIncome] {

  override def path: JsPath = JsPath \ incomePath(ForeignProperty) \ toString

  override def toString: String = countryCode.toUpperCase

}
