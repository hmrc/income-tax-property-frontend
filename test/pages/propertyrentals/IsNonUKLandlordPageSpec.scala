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

package pages.propertyrentals

import base.SpecBase
import models.DeductingTax
import pages.DeductingTaxPage

class IsNonUKLandlordPageSpec extends SpecBase {

  "must remove the DeductingTax value when the answer is no" in {

    val answers = emptyUserAnswers.set(
      DeductingTaxPage,
      DeductingTax(false, None)).success.value

    val result = answers.set(IsNonUKLandlordPage, false).success.value

    result.get(IsNonUKLandlordPage) must be(defined)
    result.get(DeductingTaxPage)    must not be defined
  }

  "must keep the DeductingTax value when the answer is yes" in {

    val answers = emptyUserAnswers.set(
      DeductingTaxPage,
      DeductingTax(true, Some("100"))).success.value

    val result = answers.set(IsNonUKLandlordPage, true).success.value

    result.get(IsNonUKLandlordPage) must be(defined)
    result.get(DeductingTaxPage)    must be(defined)
  }

}
