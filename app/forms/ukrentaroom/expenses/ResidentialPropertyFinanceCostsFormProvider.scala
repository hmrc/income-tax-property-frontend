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

package forms.ukrentaroom.expenses

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class ResidentialPropertyFinanceCostsFormProvider @Inject() extends Mappings {

  private val minResidentialPropertyFinanceCosts: Int = 0
  private val maxResidentialPropertyFinanceCosts: Int = 100000000

  def apply(individualOrAgent: String): Form[BigDecimal] =
    Form(
      "residentialPropertyFinanceCosts" -> currency(
        requiredKey = s"ukrentaroom.expenses.residentialPropertyFinanceCosts.error.required.$individualOrAgent",
        twoDecimalPlacesKey =
          s"ukrentaroom.expenses.residentialPropertyFinanceCosts.error.twoDecimalPlaces.$individualOrAgent",
        nonNumericKey = s"ukrentaroom.expenses.residentialPropertyFinanceCosts.error.nonNumeric.$individualOrAgent"
      ).verifying(
        inRange(
          minimum = BigDecimal(minResidentialPropertyFinanceCosts),
          maximum = BigDecimal(maxResidentialPropertyFinanceCosts),
          errorKey = s"ukrentaroom.expenses.residentialPropertyFinanceCosts.error.outOfRange.$individualOrAgent"
        )
      )
    )
}
