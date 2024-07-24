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

package forms.ukrentaroom

import forms.mappings.Mappings
import models.ClaimExpensesOrRelief
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfTrue

import javax.inject.Inject

class ClaimExpensesOrReliefFormProvider @Inject() extends Mappings {

  def apply(individualOrAgent: String, maxAllowedIncome: BigDecimal): Form[ClaimExpensesOrRelief] =
    Form[ClaimExpensesOrRelief](
      mapping(
        "claimExpensesOrRelief" -> boolean(s"claimExpensesOrRelief.error.required.$individualOrAgent"),
        "rentARoomAmount" -> {
          mandatoryIfTrue(
            "claimExpensesOrRelief",
            currency(
              s"claimExpensesOrRelief.amount.error.required.$individualOrAgent",
              s"claimExpensesOrRelief.amount.error.twoDecimalPlaces.$individualOrAgent",
              s"claimExpensesOrRelief.amount.error.nonNumeric.$individualOrAgent"
            )
              .verifying(
                minimumValueWithCustomArgument(
                  BigDecimal(0),
                  "claimExpensesOrRelief.amount.error.outOfRange",
                  maxAllowedIncome
                )
              )
              .verifying(maximumValue(maxAllowedIncome, "claimExpensesOrRelief.amount.error.maxAllowedClaim"))
          )
        }
      )(ClaimExpensesOrRelief.apply)(ClaimExpensesOrRelief.unapply)
    )
}
