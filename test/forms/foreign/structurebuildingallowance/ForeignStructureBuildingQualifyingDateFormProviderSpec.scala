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

package forms.foreign.structurebuildingallowance

import forms.behaviours.DateBehaviours
import play.api.data.FormError

import java.time.{LocalDate, Month}

class ForeignStructureBuildingQualifyingDateFormProviderSpec extends DateBehaviours {

  val form = new ForeignStructureBuildingQualifyingDateFormProvider()()

  ".foreignStructureBuildingQualifyingDate" - {

    val validData = datesBetween(
      min = LocalDate.of(2018, 10, 29),
      max = LocalDate.of(2026, 9, 1)
    )

    behave like dateField(form, "foreignStructureBuildingQualifyingDate", validData)

    behave like mandatoryDateField(form, "foreignStructureBuildingQualifyingDate", "foreignStructureBuildingQualifyingDate.error.required.all")

    behave like dateFieldWithMax(
      form, "foreignStructureBuildingQualifyingDate", LocalDate.of(2026, Month.SEPTEMBER, 1),
      FormError("foreignStructureBuildingQualifyingDate", "foreignStructureBuildingQualifyingDate.error.maxDate"))

    behave like dateFieldWithMin(
      form, "foreignStructureBuildingQualifyingDate", LocalDate.of(2018, Month.OCTOBER, 29),
      FormError("foreignStructureBuildingQualifyingDate", "foreignStructureBuildingQualifyingDate.error.minDate"))
  }
}
