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

import base.SpecBase
import forms.behaviours.StringFieldBehaviours
import models.UserAnswers
import org.scalacheck.Gen
import pages.foreign.{Country, SelectIncomeCountryPage}
import play.api.data.FormError
import service.CountryNamesDataSource

class SelectIncomeCountryFormProviderSpec extends StringFieldBehaviours with SpecBase {

  val requiredKey = "selectIncomeCountry.error.required.agent"
  val lengthKey = "selectIncomeCountry.error.validCountry"

  val country: Country = Country(name = "India", code = "IND")
  val index = 0
  val userType = "agent"
  val userAnswers: UserAnswers = UserAnswers(userAnswersId).set(SelectIncomeCountryPage(index), country).success.value

  val form = new SelectIncomeCountryFormProvider()("agent", userAnswers)

  ".incomeCountry" - {

    val fieldName = "country-autocomplete"

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
  }
}
