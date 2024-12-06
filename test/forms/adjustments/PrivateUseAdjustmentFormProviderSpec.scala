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

package forms.adjustments

import forms.behaviours.BooleanFieldBehaviours
import models.PrivateUseAdjustment
import org.scalatest.OptionValues
import play.api.data.FormError

import scala.collection.immutable.ArraySeq

class PrivateUseAdjustmentFormProviderSpec extends BooleanFieldBehaviours with OptionValues {

  val form = new PrivateUseAdjustmentFormProvider()("individual")

  ".privateUseAdjustmentAmount" - {
    "when privateUseAdjustment is true" - {
      "and an amount is entered, should successfully bind" in {
        val boundForm = form.bind(Map("privateUseAdjustmentAmount" -> "4534.65"))
        boundForm.value.value mustBe PrivateUseAdjustment(4534.65)
        boundForm.errors mustBe empty
      }

      "and no amount is entered, should fail to bind" in {
        val boundForm = form.bind(Map.empty[String, String])
        boundForm.errors must contain(FormError("privateUseAdjustmentAmount", "privateUseAdjustmentAmount.amount.error.required.individual"))
      }

      "and a non numeric value is entered then should fail to bind" in {
        val boundForm = form.bind(Map("privateUseAdjustmentAmount" -> "non-numeric-value"))
        boundForm.errors must contain(FormError("privateUseAdjustmentAmount", "privateUseAdjustmentAmount.amount.error.nonNumeric.individual"))
      }


      "and an amount is entered that has more than 2 decimal places then it should fail to bind" in {
        val boundForm = form.bind(Map("privateUseAdjustmentAmount" -> "4534.6545"))
        boundForm.errors must contain(FormError("privateUseAdjustmentAmount", "privateUseAdjustmentAmount.amount.error.twoDecimalPlaces.individual"))
      }


      "and an amount is entered that is out of range then should fail to bind" in {
        val boundForm = form.bind(Map("privateUseAdjustmentAmount" -> "45334553534535345435345345434.65"))
        boundForm.errors must contain(FormError("privateUseAdjustmentAmount", "privateUseAdjustmentAmount.amount.error.outOfRange", ArraySeq(0, 100000000)))
      }
    }
  }
}
