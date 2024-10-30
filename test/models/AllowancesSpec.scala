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

import audit.RentalsAllowance
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsSuccess, Json}

class AllowancesSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  val allowancesJson: String =
    """
      {
      |	"annualInvestmentAllowance" : 100,
      |	"zeroEmissionCarAllowance" :300,
      |	"zeroEmissionGoodsVehicleAllowance" : 400,
      |	"businessPremisesRenovationAllowance" :500,
      |	"replacementOfDomesticGoodsAllowance" : 600,
      |	"otherCapitalAllowance" :700
      |}""".stripMargin

  val annualInvestmentAllowanceSummaryValue = 100
  val annualInvestmentAllowanceSummary = BigDecimal.valueOf(annualInvestmentAllowanceSummaryValue)

  val zeroEmissionCarAllowanceValue = 300
  val zeroEmissionCarAllowance = BigDecimal.valueOf(zeroEmissionCarAllowanceValue)

  val zeroEmissionGoodsVehicleAllowanceValue = 400
  val zeroEmissionGoodsVehicleAllowance = BigDecimal.valueOf(zeroEmissionGoodsVehicleAllowanceValue)

  val businessPremisesRenovationValue = 500
  val businessPremisesRenovation = BigDecimal.valueOf(businessPremisesRenovationValue)

  val replacementOfDomesticGoodsValue = 600
  val replacementOfDomesticGoods = BigDecimal.valueOf(replacementOfDomesticGoodsValue)

  val otherCapitalAllowanceValue = 700
  val otherCapitalAllowance = BigDecimal.valueOf(otherCapitalAllowanceValue)

  "Allowances" - {
    "must deserialize from json" in {
      Json.parse(allowancesJson).validate[RentalsAllowance] mustBe JsSuccess(
        RentalsAllowance(
          capitalAllowancesForACar = None,
          annualInvestmentAllowance = Some(annualInvestmentAllowanceSummary),
          zeroEmissionCarAllowance = Some(zeroEmissionCarAllowance),
          zeroEmissionGoodsVehicleAllowance = Some(zeroEmissionGoodsVehicleAllowance),
          businessPremisesRenovationAllowance = Some(businessPremisesRenovation),
          replacementOfDomesticGoodsAllowance = Some(replacementOfDomesticGoods),
          otherCapitalAllowance = Some(otherCapitalAllowance)
        )
      )
    }
  }

}
