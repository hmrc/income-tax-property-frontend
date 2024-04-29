package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class ClaimExpensesOrRRRFormProvider @Inject() extends Mappings {

  val minimum = 0;
  val maximum = 100000000;
  def apply(individualOrAgent: String): Form[BigDecimal] =
    Form(
      "value" -> currency(
        "claimExpensesOrRRR.error.required",
        "claimExpensesOrRRR.error.wholeNumber",
        "claimExpensesOrRRR.error.nonNumeric")
          .verifying(inRange(BigDecimal(minimum), BigDecimal(maximum), "claimExpensesOrRRR.error.outOfRange"))
    )
}
