package forms

import forms.behaviours.CurrencyFieldBehaviours
import play.api.data.FormError

class $className$FormProviderSpec extends CurrencyFieldBehaviours {

  val form = new $className$FormProvider()("individual")

  ".value" - {

    val fieldName = "value"

    val minimum = $minimum$
    val maximum = $maximum$

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like currencyField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "$className;format="decap"$.error.nonNumeric.individual"),
      twoDecimalPlacesError = FormError(fieldName, "$className;format="decap"$.error.twoDecimalPlaces.individual")
    )

    behave like currencyFieldWithRange(
      form,
      fieldName,
      minimum = minimum,
      maximum = maximum,
      expectedError = FormError(fieldName, "$className;format="decap"$.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "$className;format="decap"$.error.required.individual")
    )
  }
}
