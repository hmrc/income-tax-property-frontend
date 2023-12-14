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

package forms.allowances

import forms.behaviours.CurrencyFieldBehaviours
import play.api.data.FormError

class AnnualInvestmentAllowanceFormProviderSpec extends CurrencyFieldBehaviours {

  val form = new AnnualInvestmentAllowanceFormProvider()("individual")

  ".annualInvestmentAllowanceAmount" - {

    val fieldName = "annualInvestmentAllowanceAmount"

    val minimum = 0
    val maximum = 100000000

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like currencyField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "annualInvestmentAllowance.error.nonNumeric.individual"),
      twoDecimalPlacesError = FormError(fieldName, "annualInvestmentAllowance.error.twoDecimalPlaces.individual")
    )

    behave like currencyFieldWithRange(
      form,
      fieldName,
      minimum = minimum,
      maximum = maximum,
      expectedError = FormError(fieldName, "annualInvestmentAllowance.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "annualInvestmentAllowance.error.required.individual")
    )
  }
}
