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

import javax.inject.Inject
import forms.mappings.Mappings
import models.UserAnswers
import play.api.data.Form

class CountryReceiveDividendIncomeFormProvider @Inject() extends Mappings {

  def apply(index: Int, userAnswers: UserAnswers): Form[String] =
    Form(
      "country-autocomplete" -> text("countryReceiveDividendIncome.error.required")
        .verifying(validCountry("countryReceiveDividendIncome.error.validCountry"))
        .verifying(dividendCountryAlreadySelected("countryReceiveDividendIncome.error.countryAlreadySelected", index, userAnswers))
        .verifying(regexp("^[A-Za-z ]*?$", "countryReceiveDividendIncome.error.validCharacters"))
    )
}
