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

package forms.ukandforeignproperty

import forms.behaviours.CurrencyFieldBehaviours
import forms.ukandforeignproperty.income.UkAndForeignPropertyForeignOtherIncomeFromPropertyFormProvider
import org.scalatest.prop.TableFor1
import play.api.data.FormError

class UkAndForeignPropertyForeignOtherIncomeFromPropertyFormProviderSpec extends CurrencyFieldBehaviours {

  val scenarios: TableFor1[String] = Table[String]( "individual", "agent")
  val fieldName = "otherPropertyIncome"

  val minimum = 0
  val maximum = 100000000
  val formProvider = new UkAndForeignPropertyForeignOtherIncomeFromPropertyFormProvider()

  forAll(scenarios) { (individualOrAgent: String) =>
    val form = formProvider(individualOrAgent)
    val requiredKey = s"ukAndForeignProperty.otherIncomeFromForeignProperty.error.required.$individualOrAgent"
    val outOutRangeErrorKey = s"ukAndForeignProperty.otherIncomeFromForeignProperty.error.outOfRange.$individualOrAgent"
    val nonNumericErrorKey = s"ukAndForeignProperty.otherIncomeFromForeignProperty.error.nonNumeric.$individualOrAgent"
    val twoDecimalErrorKey = s"ukAndForeignProperty.otherIncomeFromForeignProperty.error.twoDecimalPlaces.$individualOrAgent"

    s".foreignPropertyRentalIncome for an $individualOrAgent" - {

      val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        validDataGenerator
      )

      behave like currencyField(
        form,
        fieldName,
        nonNumericError = FormError(fieldName, nonNumericErrorKey),
        twoDecimalPlacesError = FormError(fieldName, twoDecimalErrorKey)
      )

      behave like currencyFieldWithRange(
        form,
        fieldName,
        minimum = minimum,
        maximum = maximum,
        expectedError = FormError(fieldName, outOutRangeErrorKey, Seq(minimum, maximum))
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey)
      )
    }
  }
}
