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

package forms.foreign.expenses

import forms.behaviours.CurrencyFieldBehaviours
import org.scalatest.prop.TableFor1
import play.api.data.FormError

class ForeignRentsRatesAndInsuranceFormProviderSpec extends CurrencyFieldBehaviours {

  val formProvider = new ForeignRentsRatesAndInsuranceFormProvider()
  val scenarios: TableFor1[String] = Table[String]("individualOrAgent", "individual", "agent")

  forAll(scenarios) { (individualOrAgent: String) =>
    val form = formProvider(individualOrAgent)
    s".foreignRentsRatesAndInsurance for an $individualOrAgent" - {

      val fieldName = "foreignRentsRatesAndInsurance"

      val minimum = formProvider.minimum.toInt
      val maximum = formProvider.maximum.toInt

      val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        validDataGenerator
      )

      behave like currencyField(
        form,
        fieldName,
        nonNumericError = FormError(fieldName, s"foreignRentsRatesAndInsurance.error.nonNumeric.$individualOrAgent"),
        twoDecimalPlacesError = FormError(fieldName, s"foreignRentsRatesAndInsurance.error.nonNumeric.$individualOrAgent")
      )

      behave like currencyFieldWithRange(
        form,
        fieldName,
        minimum = minimum,
        maximum = maximum,
        expectedError = FormError(fieldName, "foreignRentsRatesAndInsurance.error.outOfRange", Seq(minimum, maximum))
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, s"foreignRentsRatesAndInsurance.error.required.$individualOrAgent")
      )
    }
  }
}
