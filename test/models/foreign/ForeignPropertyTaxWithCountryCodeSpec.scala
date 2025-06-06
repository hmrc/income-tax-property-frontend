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

import models.{ForeignIncomeTax, ForeignPropertyTaxWithCountryCode}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsSuccess, Json}

class ForeignPropertyTaxWithCountryCodeSpec extends AnyFreeSpec with Matchers with OptionValues {

  val jsonWithAllFields: String =
    """{
      |  "countryCode": "USA",
      |  "foreignIncomeTax": {
      |    "isForeignIncomeTax": true,
      |    "foreignTaxPaidOrDeducted": 23
      |  },
      |  "foreignTaxCreditRelief": true
      |}""".stripMargin

  "ForeignPropertyTaxWithCountryCode" - {

    "must serialize to JSON correctly" in {
      val model = ForeignPropertyTaxWithCountryCode(
        countryCode = "USA",
        foreignIncomeTax = Some(ForeignIncomeTax(isForeignIncomeTax = true, Some(BigDecimal(23)))),
        foreignTaxCreditRelief = Some(true)
      )

      val expectedJson = Json.parse(jsonWithAllFields)
      Json.toJson(model) mustBe expectedJson
    }

    "must deserialize from JSON correctly" in {
      val expectedModel = ForeignPropertyTaxWithCountryCode(
        countryCode = "USA",
        foreignIncomeTax = Some(ForeignIncomeTax(isForeignIncomeTax = true, Some(BigDecimal(23)))),
        foreignTaxCreditRelief = Some(true)
      )

      Json.parse(jsonWithAllFields).validate[ForeignPropertyTaxWithCountryCode] mustBe JsSuccess(expectedModel)
    }

  }
}
