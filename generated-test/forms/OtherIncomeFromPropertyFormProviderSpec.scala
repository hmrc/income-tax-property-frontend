package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class OtherIncomeFromPropertyFormProviderSpec extends IntFieldBehaviours {

  val form = new OtherIncomeFromPropertyFormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = OtherIncomeFromProperty
    val maximum = 10000000

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "otherIncomeFromProperty.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "otherIncomeFromProperty.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "otherIncomeFromProperty.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "otherIncomeFromProperty.error.required")
    )
  }
}
