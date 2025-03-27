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

package forms.ukandforeignproperty

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.FormError

class ForeignCountriesRentedFormProviderSpec extends AnyWordSpec with Matchers {

  val requiredKey = "selectIncomeCountry.error.required.individual"
  val form = new ForeignCountriesRentedFormProvider()()

  "CountriesListFormProvider" must {

    "bind true" in {
      val result = form.bind(Map("isAddAnother" -> "true"))
      result.value shouldBe Some(true)
    }

    "bind false" in {
      val result = form.bind(Map("isAddAnother" -> "false"))
      result.value mustBe Some(false)
    }

    "fail to bind when key is not present" in {
      val result = form.bind(Map.empty[String, String])
      result.errors must contain only FormError("isAddAnother", requiredKey)
    }

    "fail to bind when value is invalid" in {
      val result = form.bind(Map("isAddAnother" -> "invalid"))
      result.errors must contain only FormError("isAddAnother", "error.boolean")
    }
  }
}