package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import models.TotalIncome

class TotalIncomeFormProvider @Inject() extends Mappings {

  def apply(): Form[TotalIncome] =
    Form(
      "value" -> enumerable[TotalIncome]("totalIncome.error.required")
    )
}
