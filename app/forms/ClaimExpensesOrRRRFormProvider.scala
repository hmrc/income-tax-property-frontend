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

package forms

import forms.mappings.Mappings
import models.{ClaimExpensesOrRRR, DeductingTax}
import play.api.data.Forms._
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfTrue

import javax.inject.Inject
import javax.inject.Inject
import play.api.data.Form

class ClaimExpensesOrRRRFormProvider @Inject() extends Mappings {

    def apply(individualOrAgent: String): Form[ClaimExpensesOrRRR] =
      Form[ClaimExpensesOrRRR](
        mapping(
          "claimExpensesOrRRR" -> boolean(s"claimExpensesOrRRR.error.required.$individualOrAgent"),
          "rentARoomAmount" -> {
            mandatoryIfTrue("claimExpensesOrRRR",
              currency(
                s"claimExpensesOrRRR.amount.error.required.$individualOrAgent",
                s"claimExpensesOrRRR.amount.error.twoDecimalPlaces.$individualOrAgent",
                s"claimExpensesOrRRR.amount.error.nonNumeric.$individualOrAgent")
                .verifying(inRange(BigDecimal(0), BigDecimal(100000000),
                  "claimExpensesOrRRR.amount.error.outOfRange"))
            )
          }
        )(ClaimExpensesOrRRR.apply)(ClaimExpensesOrRRR.unapply)
      )
  }
