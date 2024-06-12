package forms

import forms.behaviours.BooleanFieldBehaviours
import forms.ukrentaroom.expenses.ExpensesRRSectionCompleteFormProvider
import play.api.data.FormError

class ExpensesRRSectionCompleteFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "expensesRRSectionComplete.error.required"
  val invalidKey = "error.boolean"

  val form = new ExpensesRRSectionCompleteFormProvider()()

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
