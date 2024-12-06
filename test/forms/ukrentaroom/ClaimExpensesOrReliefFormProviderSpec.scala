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

package forms.ukrentaroom

import forms.behaviours.CurrencyFieldBehaviours
import play.api.data.FormError

class ClaimExpensesOrReliefFormProviderSpec extends CurrencyFieldBehaviours {

  val minimum = 0
  val maximum = 10000
  val form = new ClaimExpensesOrReliefFormProvider()("individual", maximum)

  ".claimExpensesOrRelief" - {

    val fieldName = "claimExpensesOrRelief"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "claimExpensesOrRelief.error.required.individual")
    )
  }

  ".rentARoomAmount" - {

    val fieldName = "rentARoomAmount"

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like currencyField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "claimExpensesOrRelief.amount.error.nonNumeric.individual"),
      twoDecimalPlacesError = FormError(fieldName, "claimExpensesOrRelief.amount.error.twoDecimalPlaces.individual"),
      ("claimExpensesOrRelief", "true")
    )

    behave like currencyFieldWithMaximum(
      form,
      fieldName,
      maximum,
      expectedError = FormError(fieldName, "claimExpensesOrRelief.amount.error.maxAllowedClaim", List(maximum)),
      ("claimExpensesOrRelief", "true")
    )

    behave like currencyFieldWithMinimum(
      form,
      fieldName,
      0,
      expectedError = FormError(fieldName, "claimExpensesOrRelief.amount.error.outOfRange", List(minimum, maximum)),
      ("claimExpensesOrRelief", "true")
    )
  }
}
