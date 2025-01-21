package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class ForeignRentalPropertyIncomeFormProvider @Inject() extends Mappings {

  def apply(): Form[Int] =
    Form(
      "value" -> int(
        "foreignRentalPropertyIncome.error.required",
        "foreignRentalPropertyIncome.error.twoDecimalPlaces",
        "foreignRentalPropertyIncome.error.nonNumeric")
          .verifying(inRange(0, 100000000, "foreignRentalPropertyIncome.error.outOfRange"))
    )
}
