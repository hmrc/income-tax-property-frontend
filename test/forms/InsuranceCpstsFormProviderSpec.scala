package forms

import forms.behaviours.IntFieldBehaviours
import forms.propertyrentals.expenses.RepairsAndMaintenanceCostsFormProvider
import play.api.data.FormError

class RepairsAndMaintenanceCostsFormProviderSpec extends IntFieldBehaviours {

  val form = new RepairsAndMaintenanceCostsFormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = 0
    val maximum = 100000000

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "RepairsAndMaintenanceCosts.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "RepairsAndMaintenanceCosts.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "RepairsAndMaintenanceCosts.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "RepairsAndMaintenanceCosts.error.required")
    )
  }
}
