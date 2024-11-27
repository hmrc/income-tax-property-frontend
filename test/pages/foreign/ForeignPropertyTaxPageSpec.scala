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

package pages.foreign

import models.{ForeignProperty, ForeignPropertyTax}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.PageConstants.foreignTaxPath
import pages.foreign.income.ForeignPropertyTaxPage
import play.api.libs.json.{JsPath, Json}

class ForeignPropertyTaxPageSpec extends AnyFreeSpec with Matchers {

  "ForeignPropertyTaxPage" - {

    val countryCode = "USA"
    val foreignPropertyTaxPage = ForeignPropertyTaxPage(countryCode)

    "must correctly calculate the path" in {
      foreignPropertyTaxPage.path mustEqual JsPath \ foreignTaxPath(ForeignProperty) \ "USA"
    }

    "must serialize and deserialize data correctly" in {
      val testTax = ForeignPropertyTax(None, None)

      val json = Json.obj(foreignPropertyTaxPage.countryCode.toUpperCase -> Json.toJson(testTax))
      val extracted = json.validate[Map[String, ForeignPropertyTax]].get

      extracted must contain("USA" -> testTax)
    }

  }
}
