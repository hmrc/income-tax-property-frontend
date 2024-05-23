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

package forms.ukrentaroom.allowances

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class ZeroEmissionCarAllowanceFormProvider @Inject() extends Mappings {

  private val minResidentialPropertyFinanceCosts = 0
  private val maxResidentialPropertyFinanceCosts = 100000000
  private val errorPrefix = "ukrentaroom.allowances.zeroEmissionCarAllowance.error"

  def apply(individualOrAgent: String): Form[BigDecimal] =
    Form(
      "zeroEmissionCarAllowance" -> currency(
        requiredKey = s"$errorPrefix.required.$individualOrAgent",
        twoDecimalPlacesKey = s"$errorPrefix.twoDecimalPlaces.$individualOrAgent",
        nonNumericKey = s"$errorPrefix.nonNumeric.$individualOrAgent"
      ).verifying(
        inRange(
          minimum = BigDecimal(minResidentialPropertyFinanceCosts),
          maximum = BigDecimal(maxResidentialPropertyFinanceCosts),
          errorKey = s"$errorPrefix.outOfRange.$individualOrAgent"
        )
      )
    )

}
