package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class ExpensesLessThan1000FormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "expensesLessThan1000.error.required"
  val invalidKey = "error.boolean"

  val form = new ExpensesLessThan1000FormProvider()()

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
