package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class ExpensesLessThan1000FormProvider @Inject() extends Mappings {

  def apply(): Form[Boolean] =
    Form(
      "value" -> boolean("expensesLessThan1000.error.required")
    )
}
