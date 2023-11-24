package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class OtherAllowablePropertyExpensesFormProvider @Inject() extends Mappings {

  def apply(): Form[Int] =
    Form(
      "value" -> int(
        "otherAllowablePropertyExpenses.error.required",
        "otherAllowablePropertyExpenses.error.wholeNumber",
        "otherAllowablePropertyExpenses.error.nonNumeric")
          .verifying(inRange(0, 100000000, "otherAllowablePropertyExpenses.error.outOfRange"))
    )
}
