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

package forms.enhancedstructuresbuildingallowance

import forms.behaviours.DateBehaviours
import forms.enhancedstructuresbuildingallowance.EsbaQualifyingDateFormProvider
import play.api.data.FormError

import java.time.{LocalDate, Month}

class EsbaQualifyingDateFormProviderSpec extends DateBehaviours {

  val form = new EsbaQualifyingDateFormProvider()()

  ".value" - {
    val maxDate = LocalDate.of(2026, Month.SEPTEMBER, 1)
    val minDate = LocalDate.of(2018, Month.OCTOBER, 29)

    val validData = datesBetween(
      min = minDate,
      max = maxDate
    )

    behave like dateField(form, "esbaQualifyingDate", validData)
    behave like mandatoryDateField(form, "esbaQualifyingDate", "esbaQualifyingDate.error.required.all")

    behave like dateFieldWithMax(
      form, "esbaQualifyingDate", maxDate,
      FormError("esbaQualifyingDate", "esbaQualifyingDate.error.maxDate"))
  }
}
