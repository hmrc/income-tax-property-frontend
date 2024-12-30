package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class ForeignSbaClaimsFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "foreignSbaClaims.error.required"
  val invalidKey = "error.boolean"

  val form = new ForeignSbaClaimsFormProvider()()

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
