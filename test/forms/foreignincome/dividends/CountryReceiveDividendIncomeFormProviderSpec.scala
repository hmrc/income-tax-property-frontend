/*
 * Copyright 2025 HM Revenue & Customs
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

package forms.foreignincome.dividends

import base.SpecBase
import forms.behaviours.StringFieldBehaviours
import models.UserAnswers
import org.scalacheck.Gen
import pages.CountryReceiveDividendIncomePage
import pages.foreign.Country
import play.api.data.FormError
import service.CountryNamesDataSource

class CountryReceiveDividendIncomeFormProviderSpec extends StringFieldBehaviours with SpecBase {

  val requiredKey = "countryReceiveDividendIncome.error.required"
  val lengthKey = "countryReceiveDividendIncome.error.validCountry"

  val country: Country = Country(name = "India", code = "IND")
  val index = 0
  val userType = "agent"
  val userAnswers: UserAnswers = UserAnswers(userAnswersId).set(CountryReceiveDividendIncomePage, country).success.value
  val invalidCharCountry: String = s"123456@£%^"

  val form = new CountryReceiveDividendIncomeFormProvider()(userAnswers)

  ".dividendIncomeCountry" - {

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

    "fail for invalid characters" in {
            form
              .bind(Map(fieldName -> invalidCharCountry))
              .fold(
                formWithErrors =>
                  formWithErrors.errors(fieldName).map(_.message) mustBe List("countryReceiveDividendIncome.error.validCountry", "countryReceiveDividendIncome.error.validCharacters"),
                _ => fail("This form should not succeed")
              )
        }

    "fail for already selected country" in {
      form
        .bind(Map(fieldName -> country.name))
        .fold(
          formWithErrors =>
            formWithErrors.errors(fieldName).map(_.message) mustBe List("countryReceiveDividendIncome.error.countryAlreadySelected"),
          _ => fail("This form should not succeed")
        )
    }
  }
}
