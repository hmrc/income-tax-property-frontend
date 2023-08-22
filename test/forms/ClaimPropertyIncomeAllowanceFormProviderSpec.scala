package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class ClaimPropertyIncomeAllowanceFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "claimPropertyIncomeAllowance.error.required"
  val invalidKey = "error.boolean"

  val form = new ClaimPropertyIncomeAllowanceFormProvider()()

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
