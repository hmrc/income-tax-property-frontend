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
import play.api.libs.json.{JsSuccess, Json}

class RaRAboutSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  val raRAboutJson: String = """{
                               |    "jointlyLetYesOrNo" : true,
                               |    "totalIncomeAmount" : 34,
                               |    "claimExpensesOrRelief" : {
                               |        "claimExpensesOrReliefYesNo" : true,
                               |        "rentARoomAmount" : 23
                               |    }
                               |}""".stripMargin

  "RaRAbout" - {
    "must deserialize from json" in {

      Json.parse(raRAboutJson).validate[RaRAbout] mustBe JsSuccess(
        RaRAbout(
          jointlyLetYesOrNo = true,
          34,
          ClaimExpensesOrRelief(
            claimExpensesOrReliefYesNo = true,
            Some(23)
          )
        )
      )
    }
  }
}
