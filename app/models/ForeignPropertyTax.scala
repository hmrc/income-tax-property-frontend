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

import pages.PageConstants.foreignTaxPath
import play.api.libs.json.{Format, Json, JsPath}
import queries.{Gettable, Settable}

case class ForeignPropertyTax(foreignIncomeTax: Option[ForeignIncomeTax],foreignTaxCreditRelief:Option[Boolean])

object ForeignPropertyTax {
  implicit val format:Format[ForeignPropertyTax] = Json.format
}

case class ReadWriteForeignPropertyTax(countryCode: String) extends Gettable[ForeignPropertyTax] with Settable[ForeignPropertyTax] {

  override def path: JsPath = JsPath \ foreignTaxPath(ForeignProperty) \ toString

  override def toString: String = countryCode.toUpperCase

}
