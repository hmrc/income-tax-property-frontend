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

package pages.enhancedstructuresbuildingallowance

import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}

class EsbaSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues with Generators {

  "esba" - {

    "must deserialise valid values" in {

      val gen = genEsba()

      forAll(gen) {
        esba =>
          val json = Json.obj(
            "esbaQualifyingDate" -> esba.esbaQualifyingDate,
            "esbaQualifyingAmount" -> esba.esbaQualifyingAmount,
            "esbaClaimAmount" -> esba.esbaClaimAmount,
            "esbaAddress" -> esba.esbaAddress
          )
          Json.toJson(esba) mustBe json
          json.validate[Esba] mustBe JsSuccess(esba)
      }
    }

  }
}
