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

package service

import com.github.tototoshi.csv.CSVReader
import pages.foreign.Country
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem

import scala.io.Source

object CountryNamesDataSource {

  // Adding an empty option as a workaround for the Select component
  def countrySelectItems(lang: String): Seq[SelectItem] = {
      lazy val countrySelectItems: Seq[SelectItem] = emptyOption +: selectItems(lang)
    countrySelectItems
  }

  private lazy val loadedCountriesEn: Seq[Country] = loadCountriesEn
  private lazy val loadedCountriesCy: Seq[Country] = loadCountriesCy

  private def emptyOption: SelectItem = SelectItem(text = "", value = Some(""))

  def getCountry(code: String, lang: String): Option[Country] = {
    lang match {
    case "en" => loadedCountriesEn.find(item => item.code == code)
    case "cy" => loadedCountriesCy.find(item => item.code == code)
  }
  }

  private def selectItems(lang: String): Seq[SelectItem] = {
  lang match {
    case "en" => loadCountriesEn.map(country => SelectItem(text = country.name, value = Some(country.code)))
    case "cy" => loadCountriesCy.map(country => SelectItem(text = country.name, value = Some(country.code)))
  }}

  lazy val loadCountriesEn: Seq[Country] =
    CSVReader
      .open(Source.fromInputStream(getClass.getResourceAsStream("/iso-countries.csv"), "UTF-8"))
      .allWithOrderedHeaders
      ._2
      .sortBy(x => x("short_name"))
      .map(y => Country(name = y("short_name"), code = y("alpha_3_code")))

  lazy val loadCountriesCy: Seq[Country] =
    CSVReader
      .open(Source.fromInputStream(getClass.getResourceAsStream("/iso-countries-cy.csv"), "UTF-8"))
      .allWithOrderedHeaders
      ._2
      .sortBy(x => x("short_name"))
      .map(y => Country(name = y("short_name"), code = y("alpha_3_code")))


  // Adding a USA option as workaround for the `Select Auto-Complete` results to show United States of America
  def countrySelectItemsWithUSA(lang: String): Seq[SelectItem] = {
    val maybeUSA: Option[Country] = getCountry("USA", lang)
    maybeUSA
      .map(usa => SelectItem(text = usa.code, value = Some(usa.code)))
      .fold(countrySelectItems(lang))(countrySelectItems(lang) :+ _)
  }

}
