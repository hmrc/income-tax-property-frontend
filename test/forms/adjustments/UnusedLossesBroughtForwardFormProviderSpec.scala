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

package forms.adjustments

import forms.behaviours.BooleanFieldBehaviours
import models.UnusedLossesBroughtForward
import org.scalatest.OptionValues
import play.api.data.FormError

import scala.collection.immutable.ArraySeq

class UnusedLossesBroughtForwardFormProviderSpec extends BooleanFieldBehaviours with OptionValues {

  val invalidKey = "error.boolean"
  val individualOrAgent = "individual"

  val formProvider = new UnusedLossesBroughtForwardFormProvider()

  val form = formProvider(individualOrAgent)

  s".unusedLossesBroughtForward" - {
    "when unusedLossesBroughtForwardYesOrNo is true" - {
      val unusedLossesBroughtForwardYesOrNo = true

      "and an amount is entered, should successfully bind" in {
        val unusedLossesBroughtForwardAmount: BigDecimal = 4534.65
        val boundForm = form.bind(
          Map(
            "unusedLossesBroughtForwardYesOrNo" -> s"$unusedLossesBroughtForwardYesOrNo",
            "unusedLossesBroughtForwardAmount" -> s"$unusedLossesBroughtForwardAmount"
          )
        )
        boundForm.value.value mustBe UnusedLossesBroughtForward(unusedLossesBroughtForwardYesOrNo, Some(unusedLossesBroughtForwardAmount))
        boundForm.errors mustBe empty
      }

      "and no amount is entered, should fail to bind" in {
        val boundForm = form.bind(Map("unusedLossesBroughtForwardYesOrNo" -> s"$unusedLossesBroughtForwardYesOrNo"))
        boundForm.errors must contain(
          FormError(
            "unusedLossesBroughtForwardAmount",
            s"unusedLossesBroughtForward.error.amount.required.$individualOrAgent"
          )
        )
      }

      "and a non numeric value is entered then should fail to bind" in {
        val boundForm =
          form.bind(
            Map(
              "unusedLossesBroughtForwardYesOrNo" -> s"$unusedLossesBroughtForwardYesOrNo",
              "unusedLossesBroughtForwardAmount" -> "non-numeric-value"
            )
          )
        boundForm.errors must contain(
          FormError(
            "unusedLossesBroughtForwardAmount",
            "unusedLossesBroughtForward.error.amount.nonNumeric"
          )
        )
      }

      "and an amount is entered that has more than 2 decimal places then it should fail to bind" in {
        val boundForm =
          form.bind(
            Map(
              "unusedLossesBroughtForwardYesOrNo" -> s"$unusedLossesBroughtForwardYesOrNo",
              "unusedLossesBroughtForwardAmount" -> "4534.6545"
            )
          )
        boundForm.errors must contain(
          FormError(
            "unusedLossesBroughtForwardAmount",
            "unusedLossesBroughtForward.error.amount.nonNumeric"
          )
        )
      }

      "and an amount is entered that is out of range then should fail to bind" in {
        val boundForm = form.bind(
          Map(
            "unusedLossesBroughtForwardYesOrNo" -> s"$unusedLossesBroughtForwardYesOrNo",
            "unusedLossesBroughtForwardAmount" -> "45334553534535345435345345434.65"
          )
        )
        boundForm.errors must contain(
          FormError(
            "unusedLossesBroughtForwardAmount",
            "unusedLossesBroughtForward.error.amount.outOfRange",
            ArraySeq(formProvider.minimum, formProvider.maximum)
          )
        )
      }
    }
  }
}
