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

import forms.behaviours.CurrencyFieldBehaviours
import play.api.data.FormError

class PropertyIncomeAllowanceClaimFormProviderSpec extends CurrencyFieldBehaviours {

  val fieldName = "propertyIncomeAllowanceClaimAmount"

  ".propertyIncomeAllowanceClaimAmount" - {

    val minimum = 0
    val maximum = 1000
    val form = new PropertyIncomeAllowanceClaimFormProvider()("individual", BigDecimal(maximum))

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like currencyField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "propertyIncomeAllowanceClaim.error.nonNumeric.individual"),
      twoDecimalPlacesError = FormError(fieldName, "propertyIncomeAllowanceClaim.error.twoDecimalPlaces.individual")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "propertyIncomeAllowanceClaim.error.required.individual")
    )

    behave like currencyFieldWithMaximum(
      form,
      fieldName,
      maximum,
      expectedError = FormError(fieldName, "propertyIncomeAllowanceClaim.error.maxAllowanceCombined.individual", List(maximum))
    )

    behave like currencyFieldWithMinimum(
      form,
      fieldName,
      0,
      expectedError = FormError(fieldName, "propertyIncomeAllowanceClaim.error.outOfRange.individual", List(0, 1000))
    )
  }

  "should set maxAllowanceCombined error if propertyIncomeAllowance is above combined allowance" - {
    val maxAllowanceCombined = 800
    val form = new PropertyIncomeAllowanceClaimFormProvider()("individual", BigDecimal(maxAllowanceCombined))

    behave like currencyFieldWithMaximum(
      form,
      fieldName,
      maxAllowanceCombined,
      expectedError = FormError(fieldName, "propertyIncomeAllowanceClaim.error.maxAllowanceCombined.individual", List(maxAllowanceCombined))
    )
  }

  "should set maxCapped error if propertyIncomeAllowance is above 1000" - {
    val maxAllowanceCombined = 1200
    val maximum = 1000
    val form = new PropertyIncomeAllowanceClaimFormProvider()("individual", BigDecimal(maxAllowanceCombined))

    behave like currencyFieldWithMaximum(
      form,
      fieldName,
      maximum,
      expectedError = FormError(fieldName, "propertyIncomeAllowanceClaim.error.maxCapped.individual", List(maximum))
    )
  }
}
