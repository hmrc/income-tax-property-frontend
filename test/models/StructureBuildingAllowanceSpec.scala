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
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.structurebuildingallowance.StructureBuildingAllowance
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDate

class StructureBuildingAllowanceSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  val structureBuildingFormGroupJson: String =
    """
      |[{
      |	"structureBuildingQualifyingDate" : "2024-04-22",
      |    "structureBuildingQualifyingAmount" : 100,
      |    "structureBuildingAllowanceClaim" : 200,
      |    "structuredBuildingAllowanceAddress" : {
      |      "buildingName" : "Building One",
      |      "buildingNumber" : "10",
      |      "postCode" : "SE13 5FG"
      |    }
      |}]
      |""".stripMargin

  val structureBuildingQualifyingAmount = 100
  val structureBuildingAllowanceClaim = 200

  val year = 2024
  val month = 4
  val day = 22

  "Structure Building Allowances" - {
    "must deserialize from json" in {
      Json.parse(structureBuildingFormGroupJson).validate[List[StructureBuildingAllowance]]
      Json.parse(structureBuildingFormGroupJson).validate[List[StructureBuildingAllowance]] mustBe JsSuccess(
        List(
          StructureBuildingAllowance(
            structureBuildingQualifyingDate = LocalDate.of(year, month, day),
            structureBuildingQualifyingAmount = structureBuildingQualifyingAmount,
            structureBuildingAllowanceClaim = structureBuildingAllowanceClaim,
            structuredBuildingAllowanceAddress = StructuredBuildingAllowanceAddress(
              buildingName = "Building One",
              buildingNumber = "10",
              postCode = "SE13 5FG"
            )
          )
        )
      )
    }
  }

}
