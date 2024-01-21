package forms

import forms.behaviours.CurrencyFieldBehaviours
import play.api.data.FormError

class StructureBuildingQualifyingAmountFormProviderSpec extends CurrencyFieldBehaviours {

  val form = new StructureBuildingQualifyingAmountFormProvider()("individual")

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
      nonNumericError = FormError(fieldName, "structureBuildingQualifyingAmount.error.nonNumeric.individual"),
      twoDecimalPlacesError = FormError(fieldName, "structureBuildingQualifyingAmount.error.twoDecimalPlaces.individual")
    )

    behave like currencyFieldWithRange(
      form,
      fieldName,
      minimum = minimum,
      maximum = maximum,
      expectedError = FormError(fieldName, "structureBuildingQualifyingAmount.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "structureBuildingQualifyingAmount.error.required.individual")
    )
  }
}
