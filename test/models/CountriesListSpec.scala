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

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.foreign.Country
import play.api.libs.json.{JsError, JsSuccess, Json}

class CountriesListSpec extends AnyFreeSpec with Matchers with OptionValues {

  "CountriesList" - {
    "write and read correctly" in {
      val countriesList = CountriesList(
        addAnotherCountry = true,
        rentIncomeCountries = Seq(Country("France", "FR"), Country("Germany", "DE"))
      )

      val json = Json.toJson(countriesList)
      json.toString() should include("France")
      json.toString() should include("Germany")

      val deserialized = json.validate[CountriesList]
      deserialized shouldBe a[JsSuccess[_]]
      deserialized.get shouldEqual countriesList
    }

    "handle empty rentIncomeCountries" in {
      val countriesList = CountriesList(
        addAnotherCountry = false,
        rentIncomeCountries = Seq.empty
      )

      val json = Json.toJson(countriesList)
      json.toString() should include("[]")

      val deserialized = json.validate[CountriesList]
      deserialized shouldBe a[JsSuccess[_]]
      deserialized.get shouldEqual countriesList
    }

    "fail to read invalid JSON" in {
      val invalidJson = Json.parse("""{"addAnotherCountry": "yes"}""")

      val deserialized = invalidJson.validate[CountriesList]
      deserialized shouldBe a[JsError]
    }
  }
}