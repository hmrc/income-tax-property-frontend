package forms.ukrentaroom.allowances

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class ElectricChargePointAllowanceForAnEVFormProviderSpec extends IntFieldBehaviours {

  val form = new ElectricChargePointAllowanceForAnEVFormProvider()()

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
      nonNumericError  = FormError(fieldName, "electricChargePointAllowanceForAnEV.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "electricChargePointAllowanceForAnEV.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "electricChargePointAllowanceForAnEV.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "electricChargePointAllowanceForAnEV.error.required")
    )
  }
}
