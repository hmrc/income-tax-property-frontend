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

package forms.ukrentaroom.adjustments

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class RaRUnusedResidentialCostsFormProvider @Inject() extends Mappings {

  val min = 0
  val max = 100000000

  def apply(individualOrAgent: String): Form[BigDecimal] =
    Form(
      "unusedResidentialPropertyFinanceCostsBroughtFwd" -> currency(
        requiredKey =
          s"ukrentaroom.expenses.unusedResidentialPropertyFinanceCostsBroughtFwd.error.required.$individualOrAgent",
        twoDecimalPlacesKey =
          s"ukrentaroom.expenses.unusedResidentialPropertyFinanceCostsBroughtFwd.error.twoDecimalPlaces.$individualOrAgent",
        nonNumericKey =
          s"ukrentaroom.expenses.unusedResidentialPropertyFinanceCostsBroughtFwd.error.nonNumeric.$individualOrAgent"
      ).verifying(
        inRange(
          minimum = BigDecimal(min),
          maximum = BigDecimal(max),
          errorKey =
            s"ukrentaroom.expenses.unusedResidentialPropertyFinanceCostsBroughtFwd.error.outOfRange.$individualOrAgent"
        )
      )
    )
}
