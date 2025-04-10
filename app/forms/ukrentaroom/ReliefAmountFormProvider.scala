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

package forms.ukrentaroom

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class ReliefAmountFormProvider @Inject() extends Mappings {

  def apply(individualOrAgent: String, maxAllowedRelief: BigDecimal): Form[BigDecimal] =
    Form(
      "reliefAmount" ->
        currency(
          requiredKey = s"ukrentaroom.reliefAmount.error.required.$individualOrAgent",
          twoDecimalPlacesKey = s"ukrentaroom.reliefAmount.error.twoDecimalPlaces.$individualOrAgent",
          nonNumericKey = s"ukrentaroom.reliefAmount.error.nonNumeric.$individualOrAgent"
        )
          .verifying(
            minimumValueWithCustomArgument(
              BigDecimal(0),
              s"ukrentaroom.reliefAmount.error.outOfRange.$individualOrAgent",
              maxAllowedRelief
            )
          )
          .verifying(
            maximumValue(maxAllowedRelief, s"ukrentaroom.reliefAmount.error.maxAllowedClaim.$individualOrAgent")
          )
    )
}
