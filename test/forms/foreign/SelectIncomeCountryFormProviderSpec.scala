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

package forms.foreign

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError
import service.CountryNamesDataSource

class SelectIncomeCountryFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "selectIncomeCountry.error.required.agent"
  val lengthKey = "selectIncomeCountry.error.validCountry"

  val form = new SelectIncomeCountryFormProvider()("agent")

  ".incomeCountry" - {

    val fieldName = "incomeCountry"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(CountryNamesDataSource.countrySelectItems.flatMap(_.value))
    )
  }
}
