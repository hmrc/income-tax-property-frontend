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

import forms.behaviours.OptionFieldBehaviours
import models.TotalIncome
import org.scalatest.prop.TableFor1
import play.api.data.FormError

class TotalIncomeFormProviderSpec extends OptionFieldBehaviours {

  val scenarios: TableFor1[String] = Table[String]("individualOrAgent", "individual", "agent")

  forAll(scenarios) { (individualOrAgent: String) =>
    val form = new TotalIncomeFormProvider()(individualOrAgent)

    s".foreignTotalIncome for an $individualOrAgent" - {

      val fieldName = "foreignTotalIncome"
      val requiredKey = s"foreignTotalIncome.error.required.$individualOrAgent"

      behave like optionsField[TotalIncome](
        form,
        fieldName,
        validValues  = TotalIncome.values,
        invalidError = FormError(fieldName, "error.invalid")
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey)
      )
    }
  }



}
