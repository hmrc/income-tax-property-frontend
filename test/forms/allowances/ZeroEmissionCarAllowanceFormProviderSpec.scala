package forms.allowances

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class ZeroEmissionCarAllowanceFormProviderSpec extends IntFieldBehaviours {

  val form = new ZeroEmissionCarAllowanceFormProvider()()

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
      nonNumericError  = FormError(fieldName, "zeroEmissionCarAllowance.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "zeroEmissionCarAllowance.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "zeroEmissionCarAllowance.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "zeroEmissionCarAllowance.error.required")
    )
  }
}
