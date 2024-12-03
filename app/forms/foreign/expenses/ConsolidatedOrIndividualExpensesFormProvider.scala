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

package forms.foreign.expenses

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms.mapping
import models.ConsolidatedOrIndividualExpenses
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfTrue

class ConsolidatedOrIndividualExpensesFormProvider @Inject() extends Mappings {

  def apply(individualOrAgent: String): Form[ConsolidatedOrIndividualExpenses] =
    Form[ConsolidatedOrIndividualExpenses](
      mapping(
      "consolidatedOrIndividualExpenses" -> boolean(s"consolidatedOrIndividualExpenses.error.required.${individualOrAgent}"),
      "consolidatedExpensesAmount" -> {
        mandatoryIfTrue("consolidatedOrIndividualExpenses",
          currency(
            s"consolidatedOrIndividualExpenses.amount.error.required.${individualOrAgent}",
            s"consolidatedOrIndividualExpenses.amount.error.twoDecimalPlaces.${individualOrAgent}",
            s"consolidatedOrIndividualExpenses.amount.error.nonNumerical.${individualOrAgent}")
            .verifying(inRange(BigDecimal(0), BigDecimal(1000000000),
              "consolidatedOrIndividualExpenses.amount.error.outOfRange"))
          )
        }
      )(ConsolidatedOrIndividualExpenses.apply)(ConsolidatedOrIndividualExpenses.unapply)
    )
}
