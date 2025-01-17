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
  lazy val countrySelectItems: Seq[SelectItem] = emptyOption +: selectItems

  private lazy val loadedCountries: Seq[Country] = loadCountries

  private def emptyOption: SelectItem = SelectItem(text = "", value = Some(""))

  def getCountry(code: String): Option[Country] = loadedCountries.find(item => item.code == code)

  private def selectItems: Seq[SelectItem] =
    loadCountries.map(country => SelectItem(text = country.name, value = Some(country.code)))

  lazy val loadCountries: Seq[Country] =
    CSVReader
      .open(Source.fromInputStream(getClass.getResourceAsStream("/iso-countries.csv"), "UTF-8"))
      .allWithOrderedHeaders
      ._2
      .sortBy(x => x("short_name"))
      .map(y => Country(name = y("short_name"), code = y("alpha_3_code")))

}
