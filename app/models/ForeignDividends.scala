/*
 * Copyright 2025 HM Revenue & Customs
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

import pages.PageConstants.foreignDividendsPath
import pages.foreign.Country
import play.api.libs.json.{Format, Json, JsPath}
import queries.{Gettable, Settable}

final case class ForeignDividends(
  dividendIncomeCountries: Array[Country],
  foreignTaxDeductedFromDividendIncome: Boolean
                                 )

object ForeignDividends extends Gettable[ForeignDividends] with Settable[ForeignDividends]{
  implicit val format: Format[ForeignDividends] = Json.format[ForeignDividends]

  override def path: JsPath = JsPath \ foreignDividendsPath(ForeignIncome)

}

final case class ForeignDividendAnswers(countryCode: String) extends Gettable[ForeignDividendAnswers] with Settable[ForeignDividendAnswers]{
  implicit val format: Format[ForeignDividendAnswers] = Json.format[ForeignDividendAnswers]

  override def path: JsPath = JsPath \ foreignDividendsPath(ForeignIncome) \ countryCode.toUpperCase
}