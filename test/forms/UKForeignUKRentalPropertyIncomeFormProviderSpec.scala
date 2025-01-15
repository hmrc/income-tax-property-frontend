package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class uKRentalPropertyIncomeFormProviderSpec extends IntFieldBehaviours {

  val form = new uKRentalPropertyIncomeFormProvider()()

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
      nonNumericError  = FormError(fieldName, "uKRentalPropertyIncome.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "uKRentalPropertyIncome.error.twoDecimalPlaces")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "uKRentalPropertyIncome.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "uKRentalPropertyIncome.error.required")
    )
  }
}
