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

package forms.propertyrentals.income

import forms.behaviours.IntFieldBehaviours

class PropertyRentalIncomeFormProviderSpec extends IntFieldBehaviours {

  val invalidKey = "error.boolean"
  val minimum = 0
  val maximum = 100000000

  val form = new PropertyRentalIncomeFormProvider()("agent")

  ".propertyRentalIncome" - {

    val fieldName = "propertyRentalIncome"

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

  }
}
