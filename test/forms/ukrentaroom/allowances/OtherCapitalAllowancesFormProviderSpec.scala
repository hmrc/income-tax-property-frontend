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

import forms.behaviours.CurrencyFieldBehaviours
import play.api.data.FormError

import scala.math.BigDecimal.RoundingMode

class OtherCapitalAllowancesFormProviderSpec extends CurrencyFieldBehaviours {

  val formProvider = new OtherCapitalAllowancesFormProvider()
  val scenarios = Table[String]("AgencyOrIndividual", "agent", "individual")
  val hundredBillion = BigDecimal("100000000000")
  forAll(scenarios) { (agencyOrIndividual: String) =>
    val form = formProvider(agencyOrIndividual)
    s".otherCapitalAllowances for $agencyOrIndividual" - {

      val fieldName = "otherCapitalAllowances"

      val minimum = 0
      val maximum = hundredBillion

      val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum, RoundingMode.HALF_UP)

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        validDataGenerator
      )

      behave like currencyField(
        form,
        fieldName,
        nonNumericError =
          FormError(fieldName, s"ukRentARoom.otherCapitalAllowances.error.nonNumeric.$agencyOrIndividual"),
        twoDecimalPlacesError =
          FormError(fieldName, s"ukRentARoom.otherCapitalAllowances.error.twoDecimalPlaces.$agencyOrIndividual")
      )

      behave like currencyFieldWithRange(
        form,
        fieldName,
        minimum = minimum,
        maximum = maximum,
        expectedError =
          FormError(fieldName, "ukRentARoom.otherCapitalAllowances.error.outOfRange", Seq(minimum, maximum))
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, s"ukRentARoom.otherCapitalAllowances.error.required.$agencyOrIndividual")
      )
    }
  }
}
