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

package pages.foreign.income

import models.{ForeignIncomeTax, ForeignProperty}
import pages.PageConstants.foreignTaxPath
import pages.QuestionPage
import play.api.libs.json.{JsPath, Json, OFormat}
import queries.Gettable

case class ForeignPropertyTax(
  countryCode: String,
  foreignIncomeTax: Option[ForeignIncomeTax],
  foreignTaxCreditRelief: Option[Boolean]
)
object ForeignPropertyTax {
  implicit val format: OFormat[ForeignPropertyTax] = Json.format[ForeignPropertyTax]
}

case class ForeignPropertyTaxPage(countryCode: String) extends QuestionPage[ForeignPropertyTax] {

  override def path: JsPath = JsPath \ foreignTaxPath(ForeignProperty) \ countryCode.toUpperCase

}

case class ForeignPropertyTaxSectionAddCountryCode(countryCode: String) extends QuestionPage[String] {

  override def path: JsPath = JsPath \ foreignTaxPath(ForeignProperty) \ countryCode.toUpperCase \ toString

  override def toString: String = "countryCode"
}

case class ReadForeignPropertyTax(countryCode: String) extends Gettable[ForeignPropertyTax] {

  override def path: JsPath = JsPath \ foreignTaxPath(ForeignProperty) \ toString

  override def toString: String = countryCode.toUpperCase

}
