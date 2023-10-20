package forms.propertyrentals

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class OtherIncomeFormProviderSpec extends IntFieldBehaviours {

  val form = new OtherIncomeFormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = OtherIncome
    val maximum = 1000000

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "otherIncome.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "otherIncome.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "otherIncome.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "otherIncome.error.required")
    )
  }
}
