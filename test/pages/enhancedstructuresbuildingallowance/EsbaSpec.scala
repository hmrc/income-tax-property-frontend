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
import models.EsbaAddress
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDate

class EsbaSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues with Generators {
  val esbaJson = """[
                   |            {
                   |                "enhancedStructureBuildingAllowanceQualifyingDate" : "2024-02-02",
                   |                "enhancedStructureBuildingAllowanceQualifyingAmount" : 2,
                   |                "enhancedStructureBuildingAllowanceClaim" : 2,
                   |                "enhancedStructureBuildingAllowanceAddress" : {
                   |                    "buildingName" : "2",
                   |                    "buildingNumber" : "2",
                   |                    "postCode" : "EH1 2QH"
                   |                }
                   |            }
                   |        ]""".stripMargin
  "esba" - {
    "must deserialise from json" in {
      Json.parse(esbaJson).validate[List[Esba]] mustBe JsSuccess(
        List(Esba(LocalDate.parse("2024-02-02"), 2, 2, EsbaAddress("2", "2", "EH1 2QH")))
      )
    }
    "must deserialise valid values" in {

      val gen = genEsba()

      forAll(gen) { esba =>
        val json = Json.obj(
          "enhancedStructureBuildingAllowanceQualifyingDate"   -> esba.enhancedStructureBuildingAllowanceQualifyingDate,
          "enhancedStructureBuildingAllowanceQualifyingAmount" -> esba.enhancedStructureBuildingAllowanceQualifyingAmount,
          "enhancedStructureBuildingAllowanceClaim"            -> esba.enhancedStructureBuildingAllowanceClaim,
          "enhancedStructureBuildingAllowanceAddress"          -> esba.enhancedStructureBuildingAllowanceAddress
        )
        Json.toJson(esba) mustBe json
        json.validate[Esba] mustBe JsSuccess(esba)
      }
    }
  }
}
