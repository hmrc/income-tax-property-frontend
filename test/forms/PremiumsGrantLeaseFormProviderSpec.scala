package forms

import forms.behaviours.IntFieldBehaviours
import forms.premiumLease.PremiumsGrantLeaseFormProvider
import play.api.data.FormError

class PremiumsGrantLeaseFormProviderSpec extends IntFieldBehaviours {

  val form = new PremiumsGrantLeaseFormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = 0
    val maximum = 1000000000

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "premiumsGrantLease.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "premiumsGrantLease.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "premiumsGrantLease.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "premiumsGrantLease.error.required")
    )
  }
}
