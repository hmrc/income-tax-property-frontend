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
import models.PremiumsGrantLease
import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfFalse

import javax.inject.Inject

class PremiumsGrantLeaseFormProvider @Inject() extends Mappings {

  def apply(individualOrAgent: String): Form[PremiumsGrantLease] =
    Form[PremiumsGrantLease](
      mapping(
        "yesOrNo" -> boolean(s"premiumsGrantLease.error.required.$individualOrAgent"),
        "premiumsGrantLeaseAmount" -> {
          mandatoryIfFalse("yesOrNo",
            currency(
              s"premiumsGrantLease.error.amount.required.$individualOrAgent",
              "premiumsGrantLease.error.amount.twoDecimalPlaces",
              s"premiumsGrantLease.error.amount.nonNumeric.$individualOrAgent")
              .verifying(inRange(BigDecimal(0), BigDecimal(100000000), "premiumsGrantLease.error.outOfRange"))
          )
        }
      )(PremiumsGrantLease.apply)(PremiumsGrantLease.unapply)
    )
}
