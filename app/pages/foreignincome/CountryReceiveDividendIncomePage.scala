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

package pages.foreignincome

import models.ForeignIncome
import pages.PageConstants.foreignDividendsPath
import pages.QuestionPage
import pages.foreign.Country
import play.api.libs.json.JsPath
import queries.{Gettable, Settable}

case class CountryReceiveDividendIncomePage(index: Int) extends QuestionPage[Country] {

  override def path: JsPath = JsPath \ foreignDividendsPath(ForeignIncome) \ toString \ index

  override def toString: String = "dividendIncomeCountries"
}

case object DividendIncomeSourceCountries extends Gettable[Array[Country]] with Settable[Array[Country]] {

  override def path: JsPath = JsPath \ foreignDividendsPath(ForeignIncome) \ toString

  override def toString: String = "dividendIncomeCountries"
}
