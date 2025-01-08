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

package forms.foreign.adjustments

import forms.mappings.Mappings

import javax.inject.Inject
import play.api.data.Form

class PropertyIncomeAllowanceClaimFormProvider @Inject() extends Mappings {

  def apply(individualOrAgent: String, totalIncomeAndBalancingCharge: BigDecimal): Form[BigDecimal] = {
    val minimum = BigDecimal(0)
    val maxCappedAllowance = BigDecimal(1000)

    val errorKey = if (totalIncomeAndBalancingCharge > maxCappedAllowance) {
      s"propertyIncomeAllowanceClaim.error.maxCapped.${individualOrAgent}"
    } else {
      s"propertyIncomeAllowanceClaim.error.maxAllowanceCombined.${individualOrAgent}"
    }
    val maximumAllowance = if (totalIncomeAndBalancingCharge > maxCappedAllowance) maxCappedAllowance else totalIncomeAndBalancingCharge

    Form(
      "propertyIncomeAllowanceClaimAmount" -> currency(
        s"propertyIncomeAllowanceClaim.error.required.$individualOrAgent",
        s"propertyIncomeAllowanceClaim.error.twoDecimalPlaces.$individualOrAgent",
        s"propertyIncomeAllowanceClaim.error.nonNumeric.$individualOrAgent")
        .verifying(minimumValueWithCustomArgument(minimum, s"propertyIncomeAllowanceClaim.error.outOfRange.$individualOrAgent", maximumAllowance))
        .verifying(maximumValue(maximumAllowance, errorKey))
    )
  }
}
