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
import models.RenovationAllowanceBalancingCharge
import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfTrue

import javax.inject.Inject

class BusinessPremisesRenovationBalancingChargeFormProvider @Inject() extends Mappings {

  val minimum = BigDecimal(0)
  val maximum = BigDecimal(100000000)

  def apply(individualOrAgent: String): Form[RenovationAllowanceBalancingCharge] =
    Form[RenovationAllowanceBalancingCharge](
      mapping(
        "renovationAllowanceBalancingChargeYesNo" -> boolean(
          s"businessPremisesRenovationBalancingCharge.error.required.yesOrNo"
        ),
        "renovationAllowanceBalancingChargeAmount" -> {
          mandatoryIfTrue(
            "renovationAllowanceBalancingChargeYesNo",
            currency(
              s"businessPremisesRenovationBalancingCharge.error.required.$individualOrAgent",
              s"businessPremisesRenovationBalancingCharge.error.twoDecimalPlaces.$individualOrAgent",
              s"businessPremisesRenovationBalancingCharge.error.nonNumeric.$individualOrAgent"
            ).verifying(inRange(minimum, maximum, "businessPremisesRenovationBalancingCharge.error.outOfRange"))
          )
        }
      )(RenovationAllowanceBalancingCharge.apply)(
        RenovationAllowanceBalancingCharge.unapply
      )
    )
}
