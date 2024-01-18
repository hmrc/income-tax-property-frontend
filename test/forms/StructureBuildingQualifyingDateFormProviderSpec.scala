package forms

import java.time.{LocalDate, ZoneOffset}

import forms.behaviours.DateBehaviours

class StructureBuildingQualifyingDateFormProviderSpec extends DateBehaviours {

  val form = new StructureBuildingQualifyingDateFormProvider()()

  ".value" - {

    val validData = datesBetween(
      min = LocalDate.of(2000, 1, 1),
      max = LocalDate.now(ZoneOffset.UTC)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "structureBuildingQualifyingDate.error.required.all")
  }
}
