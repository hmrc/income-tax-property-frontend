package forms.enhancedstructuresbuildingallowance

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class EsbaSectionFinishedFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "esbaSectionFinished.error.required"
  val invalidKey = "error.boolean"

  val form = new EsbaSectionFinishedFormProvider()()

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
