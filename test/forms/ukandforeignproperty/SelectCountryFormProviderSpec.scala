/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms.ukandforeignproperty

import forms.behaviours.StringFieldBehaviours
import models.Index
import org.scalacheck.Gen
import pages.foreign.Country
import play.api.data.FormError
import service.CountryNamesDataSource

class SelectCountryFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "selectCountry.error.required.agent"

  val form = new SelectCountryFormProvider()("agent", Nil, Index(1))

  ".value" - {

    val fieldName = "country"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(CountryNamesDataSource.loadCountriesEn.map(_.code))
    )

    "should return the 'duplicate' error when the user has already added a country, then tries to add it again" in {
      val countryCode = "ESP"
      val countries = List(Country("Spain", countryCode))
      val form = new SelectCountryFormProvider().apply("agent", countries, Index(2))

      val result = form.fillAndValidate(countryCode).errors

      result.size mustBe 1
      result.headOption must contain(FormError(fieldName, "selectCountry.error.duplicate"))
    }

    "should not return the 'duplicate' error when the user has already added Spain, and amends it without changing their answer " in {
      val countryCode = "ESP"
      val countries = List(Country("Spain", countryCode))
      val form = new SelectCountryFormProvider().apply("agent", countries, Index(1))

      val result = form.fillAndValidate(countryCode).errors

      result mustBe Nil
    }

  }
}
