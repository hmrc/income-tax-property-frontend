package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class OtherAllowablePropertyExpensesFormProviderSpec extends IntFieldBehaviours {

  val form = new OtherAllowablePropertyExpensesFormProvider()()

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
      nonNumericError  = FormError(fieldName, "otherAllowablePropertyExpenses.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "otherAllowablePropertyExpenses.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "otherAllowablePropertyExpenses.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "otherAllowablePropertyExpenses.error.required")
    )
  }
}
