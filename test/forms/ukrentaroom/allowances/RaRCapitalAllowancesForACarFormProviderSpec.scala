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

package forms.ukrentaroom.allowances

import forms.behaviours.FieldBehaviours
import models.CapitalAllowancesForACar
import play.api.data.FormError

class RaRCapitalAllowancesForACarFormProviderSpec extends FieldBehaviours {

  val requiredKey = "raRCapitalAllowancesForACar.error.required.individual"
  val invalidKey = "error.boolean"

  val form = new RaRCapitalAllowancesForACarFormProvider()("individual")

  "RaRCapitalAllowancesForACar" - {
    "when isRaRCapitalAllowancesForACar is true" - {
      "and an amount is entered, should successfully bind" in {
        val boundForm =
          form.bind(Map("isRaRCapitalAllowancesForACar" -> "true", "raRCapitalAllowancesForACarAmount" -> "12.34"))
        boundForm.value.value mustBe CapitalAllowancesForACar(isCapitalAllowancesForACar = true, Some(12.34))
        boundForm.errors mustBe empty
      }

      "and no amount is entered, should fail to bind" in {
        val boundForm = form.bind(Map("isRaRCapitalAllowancesForACar" -> "true"))
        boundForm.errors must contain(
          FormError("raRCapitalAllowancesForACarAmount", "raRCapitalAllowancesForACar.error.required.amount.individual")
        )
      }
    }
    "when isRaRCapitalAllowancesForACar is false" - {
      "and an amount is entered, should successfully bind" in {
        val boundForm =
          form.bind(Map("isRaRCapitalAllowancesForACar" -> "false", "raRCapitalAllowancesForACarAmount" -> "1234"))
        boundForm.value.value mustBe CapitalAllowancesForACar(isCapitalAllowancesForACar = false, None)
        boundForm.errors mustBe empty
      }
      "and no amount is entered, should successfully bind" in {
        val boundForm = form.bind(Map("isRaRCapitalAllowancesForACar" -> "false"))
        boundForm.value.value mustBe CapitalAllowancesForACar(isCapitalAllowancesForACar = false, None)
        boundForm.errors mustBe empty
      }
    }
  }
}
