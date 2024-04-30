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

package forms.premiumlease

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class YearLeaseAmountFormProvider @Inject() extends Mappings {

  def apply(): Form[Int] =
    Form(
      "yearLeaseAmount" -> int(
        "yearLeaseAmount.error.required",
        "yearLeaseAmount.error.wholeNumber",
        "yearLeaseAmount.error.nonNumeric", Seq(2.toString, 50.toString))
          .verifying(inRange(2, 50, "yearLeaseAmount.error.outOfRange"))
    )
}
