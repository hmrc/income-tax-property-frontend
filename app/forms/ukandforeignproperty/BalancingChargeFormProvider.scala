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

import forms.mappings.Mappings
import models.BalancingCharge
import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfTrue

import javax.inject.Inject

class BalancingChargeFormProvider @Inject() extends Mappings {

  def apply(individualOrAgent: String): Form[BalancingCharge] =
    Form[BalancingCharge](
      mapping(
        "balancingChargeYesNo" -> boolean(s"balancingCharge.error.required.$individualOrAgent"),
        "balancingChargeAmount" -> {
          mandatoryIfTrue("balancingChargeYesNo",
            currency(
              s"balancingCharge.amount.error.required.$individualOrAgent",
              s"balancingCharge.amount.error.twoDecimalPlaces.$individualOrAgent",
              s"ukAndForeignPropertyBalancingCharge.amount.error.nonNumeric")
              .verifying(inRange(BigDecimal(0), BigDecimal(100000000),
                "balancingCharge.amount.error.outOfRange"))
          )
        }
      )(BalancingCharge.apply)(BalancingCharge.unapply)
    )
}
