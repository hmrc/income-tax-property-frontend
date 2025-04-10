
package forms.ukrentaroom

import forms.behaviours.CurrencyFieldBehaviours
import play.api.data.FormError

class ReliefAmountFormProviderSpec extends CurrencyFieldBehaviours{

  val minReliefAmount = 0
  val maxReliefAmount = 1000
  val form = new ReliefAmountFormProvider()("individual", maxReliefAmount)

  ".reliefAmount" - {

    val fieldName = "reliefAmount"

    val validDataGenerator = intsInRangeWithCommas(minReliefAmount, maxReliefAmount)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like currencyField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "ukrentaroom.reliefAmount.error.nonNumeric.individual"),
      twoDecimalPlacesError =
        FormError(fieldName, "ukrentaroom.reliefAmount.error.twoDecimalPlaces.individual")
    )

    behave like currencyFieldWithMaximum(
      form,
      fieldName,
      maxReliefAmount,
      expectedError = FormError(fieldName, "ukrentaroom.reliefAmount.error.maxAllowedClaim.individual", List(maxReliefAmount)),
    )

    behave like currencyFieldWithMinimum(
      form,
      fieldName,
      0,
      expectedError = FormError(fieldName, "ukrentaroom.reliefAmount.error.outOfRange.individual", List(minReliefAmount, maxReliefAmount)),
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "ukrentaroom.reliefAmount.error.required.individual")
    )
  }
}
