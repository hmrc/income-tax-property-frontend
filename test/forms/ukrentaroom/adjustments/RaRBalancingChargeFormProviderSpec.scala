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

package forms.ukrentaroom.adjustments

import forms.behaviours.BooleanFieldBehaviours
import models.BalancingCharge
import org.scalatest.OptionValues
import play.api.data.FormError

class RaRBalancingChargeFormProviderSpec extends BooleanFieldBehaviours with OptionValues {

  val requiredKey = "raRbalancingCharge.error.required.individual"
  val invalidKey = "error.boolean"

  val form = new RaRBalancingChargeFormProvider()("individual")

  "raRbalancingChargeAmount" - {
    "when isRaRbalancingCharge is true" - {
      "and an amount is entered, should successfully bind" in {
        val boundForm = form.bind(Map("isRaRbalancingCharge" -> "true", "raRbalancingChargeAmount" -> "12.34"))
        boundForm.value.value mustBe BalancingCharge(isBalancingCharge = true, Some(12.34))
        boundForm.errors mustBe empty
      }

      "and no amount is entered, should fail to bind" in {
        val boundForm = form.bind(Map("isRaRbalancingCharge" -> "true"))
        boundForm.errors must contain(
          FormError("raRbalancingChargeAmount", "raRbalancingCharge.amount.error.required.individual")
        )
      }
    }
    "when consolidatedExpenses is false" - {
      "and an amount is entered, should successfully bind" in {
        val boundForm = form.bind(Map("isRaRbalancingCharge" -> "false", "raRbalancingChargeAmount" -> "1234"))
        boundForm.value.value mustBe BalancingCharge(isBalancingCharge = false, None)
        boundForm.errors mustBe empty
      }
      "and no amount is entered, should successfully bind" in {
        val boundForm = form.bind(Map("isRaRbalancingCharge" -> "false"))
        boundForm.value.value mustBe BalancingCharge(isBalancingCharge = false, None)
        boundForm.errors mustBe empty
      }
    }
  }
}
