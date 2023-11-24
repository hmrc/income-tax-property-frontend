package forms

import forms.behaviours.IntFieldBehaviours
import forms.propertyrentals.expenses.RentsRatesAndInsuranceFormProvider
import play.api.data.FormError

class RentsRatesAndInsuranceFormProviderSpec extends IntFieldBehaviours {

  val form = new RentsRatesAndInsuranceFormProvider()()

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
      nonNumericError  = FormError(fieldName, "RentsRatesAndInsurance.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "RentsRatesAndInsurance.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "RentsRatesAndInsurance.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "RentsRatesAndInsurance.error.required")
    )
  }
}
