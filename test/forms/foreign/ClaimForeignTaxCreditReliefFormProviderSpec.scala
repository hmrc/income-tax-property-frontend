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
import org.scalatest.prop.TableFor1
import play.api.data.FormError

class ClaimForeignTaxCreditReliefFormProviderSpec extends BooleanFieldBehaviours {

  val scenarios: TableFor1[String] = Table[String]("individualOrAgent", "individual", "agent")
  val fieldName = "claimForeignTaxCreditRelief"
  val formProvider = new ClaimForeignTaxCreditReliefFormProvider()
  
  forAll(scenarios) { (individualOrAgent: String) =>
    val form = formProvider(individualOrAgent)
    val requiredKey = s"claimForeignTaxCreditRelief.error.required.$individualOrAgent"
    s".claimForeignTaxCreditRelief for an $individualOrAgent" - {

      behave like booleanField(
        form,
        fieldName,
        invalidError = FormError(fieldName, "error.boolean")
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey)
      )
    }
  }

}
