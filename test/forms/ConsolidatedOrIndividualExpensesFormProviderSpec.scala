package forms

import forms.behaviours.OptionFieldBehaviours
import models.ConsolidatedOrIndividualExpenses
import play.api.data.FormError

class ConsolidatedOrIndividualExpensesFormProviderSpec extends OptionFieldBehaviours {

  val form = new ConsolidatedOrIndividualExpensesFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "consolidatedOrIndividualExpenses.error.required"

    behave like optionsField[ConsolidatedOrIndividualExpenses](
      form,
      fieldName,
      validValues  = ConsolidatedOrIndividualExpenses.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
