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
import models.Index
import pages.foreign.Country
import play.api.data.Form
import play.api.data.validation.Constraints.nonEmpty

import javax.inject.Inject

class SelectCountryFormProvider @Inject() extends Mappings {

  def apply(userType: String, previouslyAddedCountries: List[Country], index: Index): Form[String] =
    Form(
      "country" -> text(s"selectCountry.error.required.$userType")
        .verifying(nonEmpty(errorMessage = s"selectCountry.error.required.$userType"))
        .verifying(validCountry("error.select.validCountry"))
        .verifying("selectCountry.error.duplicate", answer => {
          val indexedCountries = previouslyAddedCountries.zipWithIndex
          val hasCountryAlready = previouslyAddedCountries.exists(_.code == answer)
          val prevIndex = indexedCountries.find(_._1.code == answer).map(_._2)
          val hasSameIndex = prevIndex.contains(index.positionZeroIndexed)

          !hasCountryAlready || hasSameIndex
        })
    )
}
