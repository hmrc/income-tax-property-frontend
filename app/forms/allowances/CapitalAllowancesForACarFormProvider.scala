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

package forms.allowances

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import models.CapitalAllowancesForACar
import play.api.data.Forms.mapping
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfTrue


class CapitalAllowancesForACarFormProvider @Inject() extends Mappings {

  val minimum = 0
  val maximum = 100000000

  def apply(individualOrAgent: String): Form[CapitalAllowancesForACar] = {
    Form(mapping(
      "isCapitalAllowancesForACar" -> boolean(s"capitalAllowancesForACar.error.required.$individualOrAgent"),
      "capitalAllowancesForACarAmount" -> {
        mandatoryIfTrue("isCapitalAllowancesForACar",
          currency(
            s"capitalAllowancesForACar.error.required.amount.$individualOrAgent",
            "capitalAllowancesForACar.error.twoDecimalPlaces",
            s"capitalAllowancesForACar.error.nonNumeric.$individualOrAgent")
            .verifying(inRange(BigDecimal(minimum), BigDecimal(maximum), "capitalAllowancesForACar.error.outOfRange"))
        )
      }
    )(CapitalAllowancesForACar.apply)(CapitalAllowancesForACar.unapply))
  }
}
