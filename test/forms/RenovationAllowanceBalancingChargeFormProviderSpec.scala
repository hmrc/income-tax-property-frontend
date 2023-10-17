package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class RenovationAllowanceBalancingChargeFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "renovationAllowanceBalancingCharge.error.required"
  val invalidKey = "error.boolean"

  val form = new RenovationAllowanceBalancingChargeFormProvider()()

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
