package forms

import forms.behaviours.OptionFieldBehaviours
import models.TotalPropertyIncome
import play.api.data.FormError

class TotalPropertyIncomeFormProviderSpec extends OptionFieldBehaviours {

  val form = new TotalPropertyIncomeFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "totalPropertyIncome.error.required"

    behave like optionsField[TotalPropertyIncome](
      form,
      fieldName,
      validValues  = TotalPropertyIncome.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
