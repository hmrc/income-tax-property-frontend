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

package forms.allowances

import forms.behaviours.FieldBehaviours
import models.CapitalAllowancesForACar
import play.api.data.FormError

class CapitalAllowancesForACarFormProviderSpec extends FieldBehaviours {

  val requiredKey = "capitalAllowancesForACar.error.required.individual"
  val invalidKey = "error.boolean"

  val form = new CapitalAllowancesForACarFormProvider()("individual")

  "CapitalAllowancesForACar" - {
    "when capitalAllowancesForACar is true" - {
      "and an amount is entered, should successfully bind" in {
        val boundForm = form.bind(Map("capitalAllowancesForACarYesNo" -> "true", "capitalAllowancesForACarAmount" -> "12.34"))
        boundForm.value.value mustBe CapitalAllowancesForACar(true, Some(12.34))
        boundForm.errors mustBe empty
      }

      "and no amount is entered, should fail to bind" in {
        val boundForm = form.bind(Map("capitalAllowancesForACarYesNo" -> "true"))
        boundForm.errors must contain(FormError("capitalAllowancesForACarAmount", "capitalAllowancesForACar.error.required.amount.individual"))
      }
    }
    "when capitalAllowancesForACarYesNo is false" - {
      "and an amount is entered, should successfully bind" in {
        val boundForm = form.bind(Map("capitalAllowancesForACarYesNo" -> "false", "capitalAllowancesForACarAmount" -> "1234"))
        boundForm.value.value mustBe CapitalAllowancesForACar(false, None)
        boundForm.errors mustBe empty
      }
      "and no amount is entered, should successfully bind" in {
        val boundForm = form.bind(Map("capitalAllowancesForACarYesNo" -> "false"))
        boundForm.value.value mustBe CapitalAllowancesForACar(false, None)
        boundForm.errors mustBe empty
      }
    }
  }
}