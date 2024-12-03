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

package forms.foreign.expenses

import forms.behaviours.OptionFieldBehaviours
import models.ConsolidatedOrIndividualExpenses
import play.api.data.FormError

import scala.collection.immutable.ArraySeq

class ConsolidatedOrIndividualExpensesFormProviderSpec extends OptionFieldBehaviours {

  val requiredKey = "consolidatedOrIndividualExpenses.error.required.agent"
  val invalidKey = "error.boolean"
  
  val form = new ConsolidatedOrIndividualExpensesFormProvider()("agent")

  ".consolidatedExpensesAmount" - {
    "consolidatedOrIndividualExpenses" - {
      "and an amount is entered, should successfully bind" in {
        val boundForm = form.bind(Map("consolidatedOrIndividualExpenses" -> "true", "consolidatedExpensesAmount" -> "4534.65"))
        boundForm.value.value mustBe ConsolidatedOrIndividualExpenses(consolidatedOrIndividualExpensesYesNo = true, Some(4534.65))
        boundForm.errors mustBe empty
      }

      "and no amount is entered, should fail to bind" in {
        val boundForm = form.bind(Map("consolidatedOrIndividualExpenses" -> "true"))
        boundForm.errors must contain(FormError("consolidatedExpensesAmount", "consolidatedOrIndividualExpenses.amount.error.required.agent"))
      }

      "and a non numeric value is entered then should fail to bind" in {
        val boundForm = form.bind(Map("consolidatedOrIndividualExpenses" -> "true", "consolidatedExpensesAmount" -> "non-numeric-value"))
        boundForm.errors must contain(FormError("consolidatedExpensesAmount", "consolidatedOrIndividualExpenses.amount.error.nonNumerical.agent"))
      }


      "and an amount is entered that has more than 2 decimal places then it should fail to bind" in {
        val boundForm = form.bind(Map("consolidatedOrIndividualExpenses" -> "true", "consolidatedExpensesAmount" -> "4534.6545"))
        boundForm.errors must contain(FormError("consolidatedExpensesAmount", "consolidatedOrIndividualExpenses.amount.error.twoDecimalPlaces.agent"))
      }


      "and an amount is entered that is out of range then should fail to bind" in {
        val boundForm = form.bind(Map("consolidatedOrIndividualExpenses" -> "true", "consolidatedExpensesAmount" -> "4533455353453534543534"))
        boundForm.errors must contain(
          FormError(
            "consolidatedExpensesAmount",
            "consolidatedOrIndividualExpenses.amount.error.outOfRange",
            ArraySeq(0, 1000000000)
          )
        )
      }
    }
  }
}
