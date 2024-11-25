package forms.foreign.expenses

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class ForeignPropertyRepairsAndMaintenanceFormProviderSpec extends IntFieldBehaviours {

  val form = new ForeignPropertyRepairsAndMaintenanceFormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = 0
    val maximum = 100000000000

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "foreignPropertyRepairsAndMaintenance.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "foreignPropertyRepairsAndMaintenance.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "foreignPropertyRepairsAndMaintenance.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "foreignPropertyRepairsAndMaintenance.error.required")
    )
  }
}
