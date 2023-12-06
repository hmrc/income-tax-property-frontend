package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class $className$FormProvider @Inject() extends Mappings {

  val minimum = $minimum$;
  val maximum = $maximum$;
  def apply(individualOrAgent: String): Form[BigDecimal] =
    Form(
      "value" -> int(
        "$className;format="decap"$.error.required.${individualOrAgent}",
        "$className;format="decap"$.error.wholeNumber.${individualOrAgent}",
        "$className;format="decap"$.error.nonNumeric.${individualOrAgent}")
          .verifying(inRange(BigDecimal(minimum), BigDecimal(maximum), "$className;format="decap"$.error.outOfRange"))
    )
}
