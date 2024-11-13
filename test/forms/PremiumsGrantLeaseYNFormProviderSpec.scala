package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class PremiumsGrantLeaseYNFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "premiumsGrantLeaseYN.error.required"
  val invalidKey = "error.boolean"

  val form = new PremiumsGrantLeaseYNFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
