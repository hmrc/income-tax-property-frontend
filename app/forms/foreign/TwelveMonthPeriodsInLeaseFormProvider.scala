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

package forms.foreign

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class TwelveMonthPeriodsInLeaseFormProvider @Inject() extends Mappings {

  val minimum = 2
  val maximum = 50

  def apply(): Form[Int] =
    Form(
      "twelveMonthPeriodsInLease" -> int(
        "twelveMonthPeriodsInLease.error.required",
        "twelveMonthPeriodsInLease.error.nonNumeric",
        "twelveMonthPeriodsInLease.error.nonNumeric")
          .verifying(inRange(minimum, maximum, "twelveMonthPeriodsInLease.error.nonNumeric"))
    )
}
