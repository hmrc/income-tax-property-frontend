/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.propertyrentals.expenses

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class RentsRatesAndInsuranceFormProvider @Inject() extends Mappings {
  private val minValue = BigDecimal(0)
  private val maxValue = BigDecimal(100000000)

  def apply(): Form[BigDecimal] =
    Form(
      "RentsRatesAndInsurance" -> currency(
        "RentsRatesAndInsurance.error.required",
        "RentsRatesAndInsurance.error.wholeNumber",
        "RentsRatesAndInsurance.error.nonNumeric")
          .verifying(inRange(minValue, maxValue, "RentsRatesAndInsurance.error.outOfRange"))
    )
}
