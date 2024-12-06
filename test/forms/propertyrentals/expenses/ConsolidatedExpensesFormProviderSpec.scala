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

package forms.propertyrentals.expenses

import forms.ConsolidatedExpensesFormProvider
import forms.behaviours.BooleanFieldBehaviours
import models.ConsolidatedExpenses
import org.scalatest.OptionValues
import play.api.data.FormError

class ConsolidatedExpensesFormProviderSpec extends BooleanFieldBehaviours with OptionValues {

  val requiredKey = "consolidatedExpenses.error.required.individual"
  val invalidKey = "error.boolean"

  val form = new ConsolidatedExpensesFormProvider()("individual")

  "consolidatedExpensesAmount" - {
    "when consolidatedExpenses is true" - {
      "and an amount is entered, should successfully bind" in {
        val boundForm = form.bind(Map("consolidatedExpensesYesOrNo" -> "true", "consolidatedExpensesAmount" -> "12.34"))
        boundForm.value.value mustBe ConsolidatedExpenses(true, Some(12.34))
        boundForm.errors mustBe empty
      }

      "and no amount is entered, should fail to bind" in {
        val boundForm = form.bind(Map("consolidatedExpensesYesOrNo" -> "true"))
        boundForm.errors must contain(FormError("consolidatedExpensesAmount", "consolidatedExpenses.error.required.amount.individual"))
      }
    }
    "when consolidatedExpenses is false" - {
      "and an amount is entered, should successfully bind" in {
        val boundForm = form.bind(Map("consolidatedExpensesYesOrNo" -> "false", "consolidatedExpensesAmount" -> "1234"))
        boundForm.value.value mustBe ConsolidatedExpenses(false, None)
        boundForm.errors mustBe empty
      }
      "and no amount is entered, should successfully bind" in {
        val boundForm = form.bind(Map("consolidatedExpensesYesOrNo" -> "false"))
        boundForm.value.value mustBe ConsolidatedExpenses(false, None)
        boundForm.errors mustBe empty
      }
    }
  }
}