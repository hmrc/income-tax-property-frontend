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

package forms.ukrentaroom

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class JointlyLetFormProviderSpec extends BooleanFieldBehaviours {

  val scenarios = Table[String]("AgentOrIndividual", "agent", "individual")
  val invalidKey = s"error.boolean"

  forAll(scenarios) { (agentOrIndividual: String) =>
    val form = new JointlyLetFormProvider()(agentOrIndividual)
    val requiredKey = s"jointlyLet.error.required.$agentOrIndividual"

    s".value $agentOrIndividual" - {

      val fieldName = "isJointlyLet"

      behave like booleanField(
        form,
        fieldName,
        invalidError = FormError(fieldName, invalidKey)
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey)
      )
    }
  }
}
