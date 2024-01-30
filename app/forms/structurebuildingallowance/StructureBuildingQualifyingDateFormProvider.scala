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

package forms.structurebuildingallowance

import forms.mappings.Mappings
import play.api.data.Form

import java.time.{LocalDate, Month}
import javax.inject.Inject

class StructureBuildingQualifyingDateFormProvider @Inject() extends Mappings {

  val MAX_DATE: LocalDate = LocalDate.of(2026, Month.SEPTEMBER, 1)
  val MIN_DATE: LocalDate = LocalDate.of(2018, Month.OCTOBER, 29)

  def apply(): Form[LocalDate] = {

    Form(
      "structureBuildingQualifyingDate" -> localDate(
        invalidKey = "structureBuildingQualifyingDate.error.invalid",
        allRequiredKey = "structureBuildingQualifyingDate.error.required.all",
        twoRequiredKey = "structureBuildingQualifyingDate.error.required.two",
        requiredKey = "structureBuildingQualifyingDate.error.required"
      ).verifying(maxDate(MAX_DATE, "structureBuildingQualifyingDate.error.maxDate"),
        minDate(MIN_DATE, "structureBuildingQualifyingDate.error.minDate"))
    )
  }
}
