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

package forms.ukandforeignproperty

import forms.mappings.Mappings
import models.ukAndForeign.UkAndForeignPropertyAmountReceivedForGrantOfLease
import play.api.data.Forms._
import play.api.data._
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfTrue

import javax.inject.Inject

class UkAndForeignPropertyAmountReceivedForGrantOfLeaseFormProvider @Inject() extends Mappings {

  def apply(individualOrAgent: String): Form[UkAndForeignPropertyAmountReceivedForGrantOfLease] =
    Form[UkAndForeignPropertyAmountReceivedForGrantOfLease](
      mapping(
        "amountReceivedForGrantOfLease" -> {
            currency(
              s"ukAndForeignPropertyRentalTypeUk.amountReceivedForGrantOfLease.amount.error.required.$individualOrAgent",
              s"ukAndForeignPropertyRentalTypeUk.amountReceivedForGrantOfLease.amount.error.twoDecimalPlaces.$individualOrAgent",
              s"ukAndForeignPropertyRentalTypeUk.amountReceivedForGrantOfLease.amount.error.nonNumeric.$individualOrAgent"
            ).verifying(
              inRange(
                BigDecimal(0),
                BigDecimal(100000000),
                "ukAndForeignPropertyRentalTypeUk.amountReceivedForGrantOfLease.amount.error.outOfRange"
              )
          )
        }
      )(UkAndForeignPropertyAmountReceivedForGrantOfLease.apply)(UkAndForeignPropertyAmountReceivedForGrantOfLease.unapply)
    )
}

