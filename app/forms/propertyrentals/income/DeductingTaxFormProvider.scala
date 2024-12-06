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

package forms.propertyrentals.income

import forms.mappings.Mappings
import models.DeductingTax
import play.api.data.Forms._
import play.api.data._
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfTrue

import javax.inject.Inject

class DeductingTaxFormProvider @Inject() extends Mappings {

  def apply(individualOrAgent: String): Form[DeductingTax] =
    Form[DeductingTax](
      mapping(
        "taxDeductedYesNo" -> boolean(s"deductingTax.error.required.$individualOrAgent"),
        "taxDeductedAmount" -> {
          mandatoryIfTrue("taxDeductedYesNo",
            currency(
              s"deductingTax.amount.error.required.$individualOrAgent",
              s"deductingTax.amount.error.twoDecimalPlaces.$individualOrAgent",
              s"deductingTax.amount.error.nonNumeric.$individualOrAgent")
              .verifying(inRange(BigDecimal(0), BigDecimal(100000000),
                "deductingTax.amount.error.outOfRange"))
          )
        }
      )(DeductingTax.apply)(DeductingTax.unapply)
    )
}

