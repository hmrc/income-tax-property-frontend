package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class DeductingTaxFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "deductingTax.error.required"
  val invalidKey = "error.boolean"

  val form = new DeductingTaxFormProvider()()

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
