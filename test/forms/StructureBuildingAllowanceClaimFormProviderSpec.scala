package forms

import forms.behaviours.CurrencyFieldBehaviours
import play.api.data.FormError

class StructureBuildingAllowanceClaimFormProviderSpec extends CurrencyFieldBehaviours {

  val form = new StructureBuildingAllowanceClaimFormProvider()("individual")

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
      nonNumericError = FormError(fieldName, "structureBuildingAllowanceClaim.error.nonNumeric.individual"),
      twoDecimalPlacesError = FormError(fieldName, "structureBuildingAllowanceClaim.error.twoDecimalPlaces.individual")
    )

    behave like currencyFieldWithRange(
      form,
      fieldName,
      minimum = minimum,
      maximum = maximum,
      expectedError = FormError(fieldName, "structureBuildingAllowanceClaim.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "structureBuildingAllowanceClaim.error.required.individual")
    )
  }
}
