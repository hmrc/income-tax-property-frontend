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

package forms

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.validation.{Constraint, Invalid, Valid}
import service.CountryNamesDataSource

import javax.inject.Inject

class SelectIncomeCountryFormProvider @Inject() (source: CountryNamesDataSource) extends Mappings {

  def apply(individualOrAgent: String): Form[String] =
    Form(
      "incomeCountry" -> text(s"selectIncomeCountry.error.required.$individualOrAgent")
        .verifying(validCountry)
    )

  private def validCountry: Constraint[String] =
    Constraint {
      case countryCode if source.countrySelectItems.flatMap(_.value).contains(countryCode) =>
        Valid
      case _ =>
        Invalid("selectIncomeCountry.error.validCountry")
    }

}
