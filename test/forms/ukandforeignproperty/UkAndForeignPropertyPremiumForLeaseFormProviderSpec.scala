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

package forms.ukandforeignproperty

import forms.behaviours.BooleanFieldBehaviours
import org.scalatest.prop.TableFor1
import play.api.data.FormError

class UkAndForeignPropertyPremiumForLeaseFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "ukAndForeignPropertyPremiumForLease.error.required"
  val invalidKey = "error.boolean"
  val formProvider = new UkAndForeignPropertyPremiumForLeaseFormProvider()
  val scenarios: TableFor1[String] = Table[String]("individualOrAgent", "individual", "agent")

  forAll(scenarios) { (individualOrAgent: String) =>
    val form = formProvider(individualOrAgent)

    s".ukAndForeignPropertyPremiumForLeaseYesOrNo for an $individualOrAgent" - {

      val fieldName = "ukAndForeignPropertyPremiumForLeaseYesOrNo"

      behave like booleanField(
        form,
        fieldName,
        invalidError = FormError(fieldName, invalidKey)
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, s"$requiredKey.$individualOrAgent")
      )
    }
  }
}
