package forms

import forms.behaviours.CurrencyFieldBehaviours
import play.api.data.FormError

class ClaimExpensesOrRRRFormProviderSpec extends CurrencyFieldBehaviours {

  val form = new ClaimExpensesOrRRRFormProvider()("individual")

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

    behave like currencyField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "claimExpensesOrRRR.error.nonNumeric.individual"),
      twoDecimalPlacesError = FormError(fieldName, "claimExpensesOrRRR.error.twoDecimalPlaces.individual")
    )

    behave like currencyFieldWithRange(
      form,
      fieldName,
      minimum = minimum,
      maximum = maximum,
      expectedError = FormError(fieldName, "claimExpensesOrRRR.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "claimExpensesOrRRR.error.required.individual")
    )
  }
}
