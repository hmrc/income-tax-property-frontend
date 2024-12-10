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

import forms.mappings.Mappings
import models.CountriesList
import pages.foreign.Country

import javax.inject.Inject
import play.api.data.Form
import play.api.data.Forms._


class CountriesListFormProvider @Inject() extends Mappings {

  def apply(): Form[CountriesList] =
    Form(
      mapping(
        "addAnotherCountry" -> boolean(s"selectIncomeCountry.error.required.individual"),
        "rentIncomeCountries" -> optional(seq(
          mapping(
            "countryName" -> nonEmptyText,
            "countryCode" -> nonEmptyText
          )(Country.apply)(Country.unapply)
        ).transform[Array[Country]](_.toArray, _.toSeq))
      )(CountriesList.apply)(CountriesList.unapply)
    )
}
