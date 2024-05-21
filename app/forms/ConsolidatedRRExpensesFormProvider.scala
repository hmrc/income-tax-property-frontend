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

import javax.inject.Inject
import forms.mappings.Mappings
import models.ConsolidatedRRExpenses
import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfTrue

class ConsolidatedRRExpensesFormProvider @Inject() extends Mappings {

  def apply(individualOrAgent: String): Form[ConsolidatedRRExpenses] =
    Form(
      mapping(
        "consolidatedExpensesOrIndiv" -> boolean(s"consolidatedExpenses.error.required.$individualOrAgent"),
        "consolidatedExpensesAmount" -> {
          mandatoryIfTrue(
            "consolidatedExpensesOrIndiv",
            currency(
              s"consolidatedRRExpenses.error.required.amount.$individualOrAgent",
              s"consolidatedRRExpenses.error.twoDecimalPlaces.$individualOrAgent",
              s"consolidatedRRExpenses.error.nonNumeric.$individualOrAgent"
            )
              .verifying(inRange(BigDecimal(0), BigDecimal(100000000), "consolidatedRRExpenses.error.outOfRange"))
          )
        }
      )(ConsolidatedRRExpenses.apply)(ConsolidatedRRExpenses.unapply)
    )
}
