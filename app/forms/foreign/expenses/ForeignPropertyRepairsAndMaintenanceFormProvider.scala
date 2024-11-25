package forms.foreign.expenses

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class ForeignPropertyRepairsAndMaintenanceFormProvider @Inject() extends Mappings {

  def apply(): Form[Int] =
    Form(
      "value" -> currency(
        "foreignPropertyRepairsAndMaintenance.error.required",
        "foreignPropertyRepairsAndMaintenance.error.wholeNumber",
        "foreignPropertyRepairsAndMaintenance.error.nonNumeric")
          .verifying(inRange(0, 100000000000, "foreignPropertyRepairsAndMaintenance.error.outOfRange"))
    )
}
