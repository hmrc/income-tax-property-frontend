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

package forms.rentalsandrentaroom.adjustments

import forms.adjustments.BusinessPremisesRenovationBalancingChargeFormProvider
import forms.behaviours.BooleanFieldBehaviours
import models._
import org.scalatest.OptionValues
import play.api.data.FormError

import scala.collection.immutable.ArraySeq

class BusinessPremisesRelocationAllowanceBalancingChargeFormProviderSpec
    extends BooleanFieldBehaviours with OptionValues {

  val form = new BusinessPremisesRenovationBalancingChargeFormProvider()("individual")

  ".businessPremisesRenovationAllowanceBalancingChargeAmount" - {
    "when businessPremisesRenovationAllowanceBalancingChargeYesOrNo is true" - {
      "and an amount is entered, should successfully bind" in {
        val boundForm = form.bind(
          Map(
            "renovationAllowanceBalancingChargeYesNo" -> "true",
            "renovationAllowanceBalancingChargeAmount"  -> "4534.65"
          )
        )
        boundForm.value.value mustBe RenovationAllowanceBalancingCharge(true, Some(BigDecimal(4534.65)))
        boundForm.errors mustBe empty
      }

      "and no amount is entered, should fail to bind" in {
        val boundForm = form.bind(Map("renovationAllowanceBalancingChargeYesNo" -> "true"))
        boundForm.errors must contain(
          FormError(
            "renovationAllowanceBalancingChargeAmount",
            "businessPremisesRenovationBalancingCharge.error.required.individual"
          )
        )
      }

      "and a non numeric value is entered then should fail to bind" in {
        val boundForm =
          form.bind(
            Map(
              "renovationAllowanceBalancingChargeYesNo" -> "true",
              "renovationAllowanceBalancingChargeAmount"  -> "non-numeric-value"
            )
          )
        boundForm.errors must contain(
          FormError(
            "renovationAllowanceBalancingChargeAmount",
            "businessPremisesRenovationBalancingCharge.error.nonNumeric.individual"
          )
        )
      }

      "and an amount is entered that has more than 2 decimal places then it should fail to bind" in {
        val boundForm = form.bind(
          Map(
            "renovationAllowanceBalancingChargeYesNo" -> "true",
            "renovationAllowanceBalancingChargeAmount"  -> "4534.6545"
          )
        )
        boundForm.errors must contain(
          FormError(
            "renovationAllowanceBalancingChargeAmount",
            "businessPremisesRenovationBalancingCharge.error.twoDecimalPlaces.individual"
          )
        )
      }

      "and an amount is entered that is out of range then should fail to bind" in {
        val boundForm = form.bind(
          Map(
            "renovationAllowanceBalancingChargeYesNo" -> "true",
            "renovationAllowanceBalancingChargeAmount"  -> "45334553534535345435345345434.65"
          )
        )
        boundForm.errors must contain(
          FormError(
            "renovationAllowanceBalancingChargeAmount",
            "businessPremisesRenovationBalancingCharge.error.outOfRange",
            ArraySeq(0, 100000000)
          )
        )
      }
    }
  }
}
