package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class ForeignChangePIAExpensesFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "foreignChangePIAExpenses.error.required"
  val invalidKey = "error.boolean"

  val form = new ForeignChangePIAExpensesFormProvider()()

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
