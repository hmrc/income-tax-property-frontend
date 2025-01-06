/*
 * Copyright 2025 HM Revenue & Customs
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

package forms.foreign.adjustments

import forms.behaviours.BooleanFieldBehaviours
import models.ForeignUnusedResidentialFinanceCost
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor1
import play.api.data.FormError

import scala.collection.immutable.ArraySeq

class ForeignUnusedResidentialFinanceCostFormProviderSpec extends BooleanFieldBehaviours with OptionValues {

  val scenarios: TableFor1[String] = Table[String]("individualOrAgent", "individual", "agent")
  val formProvider = new ForeignUnusedResidentialFinanceCostFormProvider()

  forAll(scenarios) { (individualOrAgent: String) =>
    val form = formProvider(individualOrAgent)
    s".foreignUnusedResidentialFinanceCost $individualOrAgent" - {
      "when foreignUnusedResidentialFinanceCostYesNo is true" - {
        val foreignUnusedResidentialFinanceCostYesNo = true

        "and an reversePremiums is entered, should successfully bind" in {
          val foreignUnusedResidentialFinanceCostAmount: BigDecimal = 4534.65
          val boundForm = form.bind(
            Map(
              "foreignUnusedResidentialFinanceCostYesNo" -> s"$foreignUnusedResidentialFinanceCostYesNo",
              "foreignUnusedResidentialFinanceCostAmount" -> s"$foreignUnusedResidentialFinanceCostAmount"
            )
          )
          boundForm.value.value mustBe ForeignUnusedResidentialFinanceCost(
            foreignUnusedResidentialFinanceCostYesNo,
            Some(foreignUnusedResidentialFinanceCostAmount)
          )
          boundForm.errors mustBe empty
        }

        "and no reversePremiums is entered, should fail to bind" in {
          val boundForm = form.bind(Map("foreignUnusedResidentialFinanceCostYesNo" -> s"$foreignUnusedResidentialFinanceCostYesNo"))
          boundForm.errors must contain(
            FormError(
              "foreignUnusedResidentialFinanceCostAmount",
              s"foreignUnusedResidentialFinanceCost.error.amount.required.$individualOrAgent"
            )
          )
        }

        "and a non numeric value is entered then should fail to bind" in {
          val boundForm =
            form.bind(
              Map(
                "foreignUnusedResidentialFinanceCostYesNo" -> s"$foreignUnusedResidentialFinanceCostYesNo",
                "foreignUnusedResidentialFinanceCostAmount" -> "non-numeric-value"
              )
            )
          boundForm.errors must contain(
            FormError(
              "foreignUnusedResidentialFinanceCostAmount",
              "foreignUnusedResidentialFinanceCost.error.amount.nonNumeric"
            )
          )
        }

        "and an reversePremiums is entered that has more than 2 decimal places then it should fail to bind" in {
          val boundForm =
            form.bind(
              Map(
                "foreignUnusedResidentialFinanceCostYesNo" -> s"$foreignUnusedResidentialFinanceCostYesNo",
                "foreignUnusedResidentialFinanceCostAmount" -> "4534.6545"
              )
            )
          boundForm.errors must contain(
            FormError(
              "foreignUnusedResidentialFinanceCostAmount",
              "foreignUnusedResidentialFinanceCost.error.amount.nonNumeric"
            )
          )
        }

        "and an reversePremiums is entered that is out of range then should fail to bind" in {
          val boundForm = form.bind(
            Map(
              "foreignUnusedResidentialFinanceCostYesNo" -> s"$foreignUnusedResidentialFinanceCostYesNo",
              "foreignUnusedResidentialFinanceCostAmount" -> "45334553534535345435345345434.65"
            )
          )
          boundForm.errors must contain(
            FormError(
              "foreignUnusedResidentialFinanceCostAmount",
              "foreignUnusedResidentialFinanceCost.error.amount.outOfRange",
              ArraySeq(0, 100000000)
            )
          )
        }
      }
    }
  }
}
