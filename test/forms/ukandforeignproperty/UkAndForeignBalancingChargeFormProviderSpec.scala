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

package forms.ukandforeignproperty

import forms.behaviours.BooleanFieldBehaviours
import models.BalancingCharge
import play.api.data.FormError

import scala.collection.immutable.ArraySeq

class UkAndForeignBalancingChargeFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "ukAndForeignBalancingCharge.error.required"
  val invalidKey = "error.boolean"

  val form = new UkAndForeignBalancingChargeFormProvider()("individual")

  ".balancingChargeAmount" - {
    "when isBalancingCharge is true" - {
      "and an amount is entered, should successfully bind" in {
        val boundForm = form.bind(Map("isBalancingCharge" -> "true", "balancingChargeAmount" -> "4534.65"))
        boundForm.value.value mustBe BalancingCharge(isBalancingCharge = true, Some(4534.65))
        boundForm.errors mustBe empty
      }

      "and no amount is entered, should fail to bind" in {
        val boundForm = form.bind(Map("isBalancingCharge" -> "true"))
        boundForm.errors must contain(FormError("balancingChargeAmount", "ukAndForeignBalancingCharge.error.required.amount"))
      }

      "and a non numeric value is entered then should fail to bind" in {
        val boundForm = form.bind(Map("isBalancingCharge" -> "true", "balancingChargeAmount" -> "non-numeric-value"))
        boundForm.errors must contain(FormError("balancingChargeAmount", "ukAndForeignBalancingCharge.error.nonNumeric"))
      }


      "and an amount is entered that has more than 2 decimal places then it should fail to bind" in {
        val boundForm = form.bind(Map("isBalancingCharge" -> "true", "balancingChargeAmount" -> "4534.6545"))
        boundForm.errors must contain(FormError("balancingChargeAmount", "ukAndForeignBalancingCharge.error.twoDecimalPlaces"))
      }


      "and an amount is entered that is out of range then should fail to bind" in {
        val boundForm = form.bind(Map("isBalancingCharge" -> "true", "balancingChargeAmount" -> "4533455353453534543534"))
        boundForm.errors must contain(
          FormError(
            "balancingChargeAmount",
            "ukAndForeignBalancingCharge.amount.error.outOfRange",
            ArraySeq(0, 100000000)
          )
        )
      }
    }
  }
}
