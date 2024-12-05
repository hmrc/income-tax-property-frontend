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
import models.ForeignPremiumsGrantLease
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor1
import play.api.data.FormError

import scala.collection.immutable.ArraySeq

class ForeignPremiumsGrantLeaseFormProviderSpec extends BooleanFieldBehaviours with OptionValues {

  val scenarios: TableFor1[String] = Table[String]("individualOrAgent", "individual", "agent")
  val formProvider = new ForeignPremiumsGrantLeaseFormProvider()

  forAll(scenarios) { (individualOrAgent: String) =>
    val form = formProvider(individualOrAgent)

    s".foreignPremiumsGrantLease for $individualOrAgent" - {
      "when foreignPremiumsGrantLeaseYesOrNo is false" - {
        val foreignPremiumsGrantLeaseYesOrNo = false

        "and an reversePremiums is entered, should successfully bind" in {
          val foreignPremiumsGrantLeaseAmount: BigDecimal = 4534.65
          val boundForm = form.bind(
            Map(
              "foreignPremiumsGrantLeaseYesOrNo" -> s"$foreignPremiumsGrantLeaseYesOrNo",
              "foreignPremiumsGrantLeaseAmount" -> s"$foreignPremiumsGrantLeaseAmount"
            )
          )
          boundForm.value.value mustBe ForeignPremiumsGrantLease(foreignPremiumsGrantLeaseYesOrNo, Some(foreignPremiumsGrantLeaseAmount))
          boundForm.errors mustBe empty
        }

        "and no reversePremiums is entered, should fail to bind" in {
          val boundForm = form.bind(Map("foreignPremiumsGrantLeaseYesOrNo" -> s"$foreignPremiumsGrantLeaseYesOrNo"))
          boundForm.errors must contain(
            FormError(
              "foreignPremiumsGrantLeaseAmount",
              s"foreignPremiumsGrantLease.error.reversePremiums.required.$individualOrAgent"
            )
          )
        }

        "and a non numeric value is entered then should fail to bind" in {
          val boundForm =
            form.bind(
              Map(
                "foreignPremiumsGrantLeaseYesOrNo" -> s"$foreignPremiumsGrantLeaseYesOrNo",
                "foreignPremiumsGrantLeaseAmount" -> "non-numeric-value"
              )
            )
          boundForm.errors must contain(
            FormError(
              "foreignPremiumsGrantLeaseAmount",
              s"foreignPremiumsGrantLease.error.reversePremiums.nonNumeric.$individualOrAgent"
            )
          )
        }

        "and an reversePremiums is entered that has more than 2 decimal places then it should fail to bind" in {
          val boundForm =
            form.bind(
              Map(
                "foreignPremiumsGrantLeaseYesOrNo" -> s"$foreignPremiumsGrantLeaseYesOrNo",
                "foreignPremiumsGrantLeaseAmount" -> "4534.6545"
              )
            )
          boundForm.errors must contain(
            FormError(
              "foreignPremiumsGrantLeaseAmount",
              "foreignPremiumsGrantLease.error.reversePremiums.twoDecimalPlaces"
            )
          )
        }

        "and an reversePremiums is entered that is out of range then should fail to bind" in {
          val boundForm = form.bind(
            Map(
              "foreignPremiumsGrantLeaseYesOrNo" -> s"$foreignPremiumsGrantLeaseYesOrNo",
              "foreignPremiumsGrantLeaseAmount" -> "45334553534535345435345345434.65"
            )
          )
          boundForm.errors must contain(
            FormError(
              "foreignPremiumsGrantLeaseAmount",
              "foreignPremiumsGrantLease.error.outOfRange",
              ArraySeq(0, 100000000)
            )
          )
        }
      }
    }
  }
}
