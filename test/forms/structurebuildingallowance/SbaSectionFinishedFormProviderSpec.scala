package forms.structurebuildingallowance

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class SbaSectionFinishedFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "sbaSectionFinished.error.required"
  val invalidKey = "error.boolean"

  val form = new SbaSectionFinishedFormProvider()()

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
