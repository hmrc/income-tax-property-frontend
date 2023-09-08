package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class premiumGrantLeaseFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "premiumGrantLease.error.required"
  val lengthKey = "premiumGrantLease.error.length"
  val maxLength = PremiumGrantLease

  val form = new premiumGrantLeaseFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
