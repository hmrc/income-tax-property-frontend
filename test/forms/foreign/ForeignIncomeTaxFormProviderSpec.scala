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

import forms.behaviours.BooleanFieldBehaviours
import models.ForeignIncomeTax
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor1
import play.api.data.FormError

import scala.collection.immutable.ArraySeq

class ForeignIncomeTaxFormProviderSpec extends BooleanFieldBehaviours with OptionValues {

  val scenarios: TableFor1[String] = Table[String]("individualOrAgent", "individual", "agent")
  val invalidKey = "error.boolean"

  val formProvider = new ForeignIncomeTaxFormProvider()

  forAll(scenarios) { (individualOrAgent: String) =>
    val form = formProvider(individualOrAgent)

    s".foreignIncomeTax for $individualOrAgent" - {
      "when foreignIncomeTaxYesNo is true" - {
        val foreignIncomeTaxYesNo = true

        "and an reversePremiums is entered, should successfully bind" in {
          val foreignTaxPaidOrDeducted: BigDecimal = 4534.65
          val boundForm = form.bind(
            Map(
              "foreignIncomeTaxYesNo" -> s"$foreignIncomeTaxYesNo",
              "foreignTaxPaidOrDeducted" -> s"$foreignTaxPaidOrDeducted"
            )
          )
          boundForm.value.value mustBe ForeignIncomeTax(foreignIncomeTaxYesNo, Some(foreignTaxPaidOrDeducted))
          boundForm.errors mustBe empty
        }

        "and no reversePremiums is entered, should fail to bind" in {
          val boundForm = form.bind(Map("foreignIncomeTaxYesNo" -> s"$foreignIncomeTaxYesNo"))
          boundForm.errors must contain(
            FormError(
              "foreignTaxPaidOrDeducted",
              s"foreignIncomeTax.error.amount.required.$individualOrAgent"
            )
          )
        }

        "and a non numeric value is entered then should fail to bind" in {
          val boundForm =
            form.bind(
              Map(
                "foreignIncomeTaxYesNo" -> s"$foreignIncomeTaxYesNo",
                "foreignTaxPaidOrDeducted" -> "non-numeric-value"
              )
            )
          boundForm.errors must contain(
            FormError(
              "foreignTaxPaidOrDeducted",
              "foreignIncomeTax.error.amount.nonNumeric"
            )
          )
        }

        "and an reversePremiums is entered that has more than 2 decimal places then it should fail to bind" in {
          val boundForm =
            form.bind(
              Map(
                "foreignIncomeTaxYesNo" -> s"$foreignIncomeTaxYesNo",
                "foreignTaxPaidOrDeducted" -> "4534.6545"
              )
            )
          boundForm.errors must contain(
            FormError(
              "foreignTaxPaidOrDeducted",
              "foreignIncomeTax.error.amount.nonNumeric"
            )
          )
        }

        "and an reversePremiums is entered that is out of range then should fail to bind" in {
          val boundForm = form.bind(
            Map(
              "foreignIncomeTaxYesNo" -> s"$foreignIncomeTaxYesNo",
              "foreignTaxPaidOrDeducted" -> "45334553534535345435345345434.65"
            )
          )
          boundForm.errors must contain(
            FormError(
              "foreignTaxPaidOrDeducted",
              "foreignIncomeTax.error.amount.outOfRange",
              ArraySeq(0, 100000000)
            )
          )
        }
      }
    }
  }
}
