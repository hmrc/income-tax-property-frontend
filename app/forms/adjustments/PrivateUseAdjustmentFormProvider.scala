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

package forms.adjustments

import forms.mappings.Mappings
import models.PrivateUseAdjustment
import play.api.data.Form
import play.api.data.Forms.mapping

import javax.inject.Inject

class PrivateUseAdjustmentFormProvider @Inject() extends Mappings {

  def apply(individualOrAgent: String): Form[PrivateUseAdjustment] = {
    Form(mapping(
      "privateUseAdjustmentAmount" ->
          currency(
            s"privateUseAdjustmentAmount.amount.error.required.$individualOrAgent",
            s"privateUseAdjustmentAmount.amount.error.twoDecimalPlaces.$individualOrAgent",
            s"privateUseAdjustmentAmount.amount.error.nonNumeric.$individualOrAgent")
            .verifying(inRange(BigDecimal(0), BigDecimal(100000000),
              "privateUseAdjustmentAmount.amount.error.outOfRange"))
    )(PrivateUseAdjustment.apply)(PrivateUseAdjustment.unapply))
  }
}
