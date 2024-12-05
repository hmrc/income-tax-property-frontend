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

package forms.foreign

import forms.behaviours.BooleanFieldBehaviours
import models.PremiumCalculated
import play.api.data.FormError

import scala.collection.immutable.ArraySeq

class CalculatedPremiumLeaseTaxableFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "calculatedPremiumLeaseTaxable.error.required.individual"
  val invalidKey = "error.boolean"

  val form = new CalculatedPremiumLeaseTaxableFormProvider()("individual")


  ".premiumsOfLeaseGrant" - {
    "calculatedPremiumLeaseTaxable" - {
      "and an reversePremiums is entered, should successfully bind" in {
        val boundForm = form.bind(Map("calculatedPremiumLeaseTaxable" -> "true", "premiumsOfLeaseGrant" -> "4534.65"))
        boundForm.value.value mustBe PremiumCalculated(calculatedPremiumLeaseTaxable = true, Some(4534.65))
        boundForm.errors mustBe empty
      }

      "and no reversePremiums is entered, should fail to bind" in {
        val boundForm = form.bind(Map("calculatedPremiumLeaseTaxable" -> "true"))
        boundForm.errors must contain(FormError("premiumsOfLeaseGrant", "premiumCalculated.reversePremiums.error.required.individual"))
      }

      "and a non numeric value is entered then should fail to bind" in {
        val boundForm = form.bind(Map("calculatedPremiumLeaseTaxable" -> "true", "premiumsOfLeaseGrant" -> "non-numeric-value"))
        boundForm.errors must contain(FormError("premiumsOfLeaseGrant", "premiumCalculated.reversePremiums.error.nonNumeric"))
      }


      "and an reversePremiums is entered that has more than 2 decimal places then it should fail to bind" in {
        val boundForm = form.bind(Map("calculatedPremiumLeaseTaxable" -> "true", "premiumsOfLeaseGrant" -> "4534.6545"))
        boundForm.errors must contain(FormError("premiumsOfLeaseGrant", "premiumCalculated.reversePremiums.error.twoDecimalPlaces"))
      }


      "and an reversePremiums is entered that is out of range then should fail to bind" in {
        val boundForm = form.bind(Map("calculatedPremiumLeaseTaxable" -> "true", "premiumsOfLeaseGrant" -> "4533455353453534543534"))
        boundForm.errors must contain(
          FormError(
            "premiumsOfLeaseGrant",
            "premiumCalculated.reversePremiums.error.outOfRange",
            ArraySeq(0, 100000000)
          )
        )
      }
    }
  }
}
