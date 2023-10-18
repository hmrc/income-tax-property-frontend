package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class ResidentialFinanceCostFormProviderSpec extends IntFieldBehaviours {

  val form = new ResidentialFinanceCostFormProvider()()

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
      nonNumericError  = FormError(fieldName, "residentialFinanceCost.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "residentialFinanceCost.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "residentialFinanceCost.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "residentialFinanceCost.error.required")
    )
  }
}
