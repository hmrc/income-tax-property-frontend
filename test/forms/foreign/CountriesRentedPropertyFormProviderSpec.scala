package forms.foreign

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class CountriesRentedPropertyFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "countriesRentedProperty.error.required"
  val invalidKey = "error.boolean"

  val form = new CountriesRentedPropertyFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
