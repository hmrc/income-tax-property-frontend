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
import models.PremiumCalculated
import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfTrue

import javax.inject.Inject

class CalculatedPremiumLeaseTaxableFormProvider @Inject() extends Mappings {

  def apply(individualOrAgent: String): Form[PremiumCalculated] =
    Form[PremiumCalculated](
      mapping(
        "calculatedPremiumLeaseTaxable" -> boolean(s"calculatedPremiumLeaseTaxable.error.required.$individualOrAgent"),
        "premiumsOfLeaseGrant" -> {
          mandatoryIfTrue(
            "calculatedPremiumLeaseTaxable",
            currency(
              s"premiumCalculated.amount.error.required.$individualOrAgent",
              "premiumCalculated.amount.error.twoDecimalPlaces",
              "premiumCalculated.amount.error.nonNumeric"
            )
              .verifying(inRange(BigDecimal(0), BigDecimal(100000000), "premiumCalculated.amount.error.outOfRange"))
          )
        }
      )(PremiumCalculated.apply)(PremiumCalculated.unapply)
    )
}
