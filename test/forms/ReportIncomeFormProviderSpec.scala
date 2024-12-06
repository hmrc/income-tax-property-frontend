package forms

import forms.behaviours.OptionFieldBehaviours
import models.ReportIncome
import play.api.data.FormError

class ReportIncomeFormProviderSpec extends OptionFieldBehaviours {

  val form = new ReportIncomeFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "reportIncome.error.required"

    behave like optionsField[ReportIncome](
      form,
      fieldName,
      validValues  = ReportIncome.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
