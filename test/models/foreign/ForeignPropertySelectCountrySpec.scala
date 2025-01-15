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

package models.foreign

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json._
import models.{ForeignProperty, ForeignPropertySelectCountry, TotalIncome}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages.foreign.Country
import pages.PageConstants.selectCountryPath

class ForeignPropertySelectCountrySpec extends AnyFreeSpec with Matchers {

  "ForeignPropertySelectCountry" - {

    ".toString" - {

      "must return a human-readable string" in {
        val instance = ForeignPropertySelectCountry(
          totalIncome = TotalIncome.Over,
          reportPropertyIncome = Some(true),
          incomeCountries = Some(Array(Country("France", "FRA"), Country("Brazil", "BRA"))),
          addAnotherCountry = Some(false),
          claimPropertyIncomeAllowance = Some(true)
        )

        instance.toString must include("ForeignPropertySelectCountry")
      }
    }

    ".format" - {

      "must serialize and deserialize correctly" in {
        val instance = ForeignPropertySelectCountry(
          totalIncome = TotalIncome.Over,
          reportPropertyIncome = Some(false),
          incomeCountries = None,
          addAnotherCountry = None,
          claimPropertyIncomeAllowance = None
        )

        val json = Json.toJson(instance)
        val deserialized = json.as[ForeignPropertySelectCountry]

        deserialized mustEqual instance
      }

      "must handle optional fields correctly" in {
        val instance = ForeignPropertySelectCountry(
          totalIncome = TotalIncome.Under,
          reportPropertyIncome = None,
          incomeCountries = None,
          addAnotherCountry = None,
          claimPropertyIncomeAllowance = Some(false)
        )

        val json = Json.toJson(instance)
        val deserialized = json.as[ForeignPropertySelectCountry]

        deserialized mustEqual instance
      }
    }

    ".path" - {

      "must resolve the correct JsPath" in {
        ForeignPropertySelectCountry.path mustEqual JsPath \ selectCountryPath(ForeignProperty)
      }
    }
  }
}
