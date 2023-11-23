package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class OtherProfessionalFeesFormProviderSpec extends IntFieldBehaviours {

  val form = new OtherProfessionalFeesFormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = 0
    val maximum = 100

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "otherProfessionalFees.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "otherProfessionalFees.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "otherProfessionalFees.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "otherProfessionalFees.error.required")
    )
  }
}
