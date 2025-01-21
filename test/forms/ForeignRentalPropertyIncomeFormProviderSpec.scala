package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class ForeignRentalPropertyIncomeFormProviderSpec extends IntFieldBehaviours {

  val form = new ForeignRentalPropertyIncomeFormProvider()()

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
      nonNumericError  = FormError(fieldName, "foreignRentalPropertyIncome.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "foreignRentalPropertyIncome.error.twoDecimalPlaces")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "foreignRentalPropertyIncome.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "foreignRentalPropertyIncome.error.required")
    )
  }
}
