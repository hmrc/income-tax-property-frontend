/*
 * Copyright 2025 HM Revenue & Customs
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

package forms.foreign.adjustments

import forms.behaviours.BooleanFieldBehaviours
import models.BalancingCharge
import org.scalatest.OptionValues
import play.api.data.FormError

import scala.collection.immutable.ArraySeq

class ForeignBalancingChargeFormProviderSpec extends BooleanFieldBehaviours with OptionValues {

  val requiredKey = "foreignBalancingCharge.error.required.individual"
  val invalidKey = "error.boolean"

  val form = new ForeignBalancingChargeFormProvider()("individual")

  ".balancingChargeAmount" - {
    "when balancingChargeYesNo is true" - {
      "and an amount is entered, should successfully bind" in {
        val boundForm = form.bind(Map("balancingChargeYesNo" -> "true", "balancingChargeAmount" -> "4534.65"))
        boundForm.value.value mustBe BalancingCharge(balancingChargeYesNo = true, Some(4534.65))
        boundForm.errors mustBe empty
      }

      "and no amount is entered, should fail to bind" in {
        val boundForm = form.bind(Map("balancingChargeYesNo" -> "true"))
        boundForm.errors must contain(FormError("balancingChargeAmount", "foreignBalancingCharge.amount.error.required.individual"))
      }

      "and a non numeric value is entered then should fail to bind" in {
        val boundForm = form.bind(Map("balancingChargeYesNo" -> "true", "balancingChargeAmount" -> "non-numeric-value"))
        boundForm.errors must contain(FormError("balancingChargeAmount", "foreignBalancingCharge.amount.error.nonNumeric.individual"))
      }


      "and an amount is entered that has more than 2 decimal places then it should fail to bind" in {
        val boundForm = form.bind(Map("balancingChargeYesNo" -> "true", "balancingChargeAmount" -> "4534.6545"))
        boundForm.errors must contain(FormError("balancingChargeAmount", "foreignBalancingCharge.amount.error.twoDecimalPlaces.individual"))
      }


      "and an amount is entered that is out of range then should fail to bind" in {
        val boundForm = form.bind(Map("balancingChargeYesNo" -> "true", "balancingChargeAmount" -> "4533455353453534543534"))
        boundForm.errors must contain(
          FormError(
            "balancingChargeAmount",
            "foreignBalancingCharge.amount.error.outOfRange",
            ArraySeq(0, 100000000)
          )
        )
      }
    }
  }
}
