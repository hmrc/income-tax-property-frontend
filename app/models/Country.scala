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

case class Country(code: String, name: String) {

  def toMap: Map[String, String] = Map(
    "Country" -> code,
    "Name"    -> name
  )
}

object Country {

  def apply(codeCountryMap: (String, Map[String, String])): Country = codeCountryMap match {
    case (code, countryMap) => new Country(countryMap("Country"), countryMap("Name"))
  }

  def toMap(country: Country): (String, Map[String, String]) =
    country.code -> country.toMap
}
