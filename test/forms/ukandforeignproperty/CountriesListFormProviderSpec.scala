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

package forms.ukandforeignproperty

import models.CountriesList
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpec
import pages.foreign.Country
import play.api.data.{Form, FormError}

class CountriesListFormProviderSpec extends AnyWordSpec with Matchers {

  val formProvider = new CountriesListFormProvider()
  val form: Form[CountriesList] = formProvider()

  "CountriesListFormProvider" must {
    "bind valid data" in {
      val data = Map(
        "addAnotherCountry" -> "true",
        "rentIncomeCountries[0].countryName" -> "France",
        "rentIncomeCountries[0].countryCode" -> "FR",
        "rentIncomeCountries[1].countryName" -> "Germany",
        "rentIncomeCountries[1].countryCode" -> "DE"
      )

      val result = form.bind(data)
      result.errors shouldBe empty

      val countriesList = CountriesList(
        addAnotherCountry = true,
        rentIncomeCountries = Seq(Country("France", "FR"), Country("Germany", "DE"))
      )

      result.get shouldEqual countriesList
    }

    "fail to bind when addAnotherCountry is missing" in {
      val data = Map(
        "rentIncomeCountries[0].countryName" -> "France",
        "rentIncomeCountries[0].countryCode" -> "FR"
      )

      val result = form.bind(data)
      result.errors should contain(FormError("addAnotherCountry", "selectIncomeCountry.error.required.individual"))
    }

    "fail to bind invalid data" in {
      val data = Map(
        "addAnotherCountry" -> "yes"
      )

      val result = form.bind(data)
      result.errors should not be empty
    }
  }
}