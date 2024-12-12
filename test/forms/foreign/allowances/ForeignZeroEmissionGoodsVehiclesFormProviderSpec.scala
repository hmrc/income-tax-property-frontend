package forms.foreign.allowances

import forms.behaviours.CurrencyFieldBehaviours
import play.api.data.FormError

class ForeignZeroEmissionGoodsVehiclesFormProviderSpec extends CurrencyFieldBehaviours {

  val form = new ForeignZeroEmissionGoodsVehiclesFormProvider()("individual")

  ".foreignZeroEmissionGoodsVehiclesAmount" - {

    val fieldName = "foreignZeroEmissionGoodsVehiclesAmount"

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
      nonNumericError = FormError(fieldName, "foreignZeroEmissionGoodsVehicles.error.nonNumeric.individual"),
      twoDecimalPlacesError = FormError(fieldName, "foreignZeroEmissionGoodsVehicles.error.twoDecimalPlaces.individual")
    )

    behave like currencyFieldWithRange(
      form,
      fieldName,
      minimum = minimum,
      maximum = maximum,
      expectedError = FormError(fieldName, "foreignZeroEmissionGoodsVehicles.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "foreignZeroEmissionGoodsVehicles.error.required.individual")
    )
  }
}
