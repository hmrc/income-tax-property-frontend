package forms

import forms.behaviours.OptionFieldBehaviours
import models.TotalIncome
import play.api.data.FormError

class TotalIncomeFormProviderSpec extends OptionFieldBehaviours {

  val form = new TotalIncomeFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "totalIncome.error.required"

    behave like optionsField[TotalIncome](
      form,
      fieldName,
      validValues  = TotalIncome.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
