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

package pages.foreign

import models.ForeignProperty
import pages.PageConstants
import pages.PageConstants.selectCountry
import play.api.libs.json.{Format, JsPath, Json}
import queries.{Gettable, Settable}

case class Country(
  name: String,
  code: String
)

object Country {
  implicit val format: Format[Country] = Json.format[Country]
}

case class CountryGroup() extends Gettable[Array[Country]] with Settable[Array[Country]] {
  override def path: JsPath = JsPath \ selectCountry(ForeignProperty) \ toString

  override def toString: String = PageConstants.countriesRentedPropertyGroup
}
