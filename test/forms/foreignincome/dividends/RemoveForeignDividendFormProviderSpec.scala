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

package forms.foreignincome.dividends

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class RemoveForeignDividendFormProviderSpec extends BooleanFieldBehaviours {
  val requiredKey = "removeForeignDividend.error.required"
  val invalidKey = "error.boolean"
  val countryName: String = "Spain"

  val form = new RemoveForeignDividendFormProvider()(countryName)

  ".value" - {

    val fieldName = "removeForeignDividend"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey, args = Seq(countryName))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, args = Seq(countryName))
    )
  }
}
