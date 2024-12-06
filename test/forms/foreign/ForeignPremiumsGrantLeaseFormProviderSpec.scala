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
    val premiumsOfLeaseGrantAgreed = false
    val premiumsOfLeaseGrant: BigDecimal = 4534.65

    s".premiumsOfLeaseGrant for $individualOrAgent" - {
      "when premiumsOfLeaseGrantAgreed is false" - {

        "and an amount is entered, should successfully bind" in {

          val boundForm = form.bind(
            Map(
              "premiumsOfLeaseGrantAgreed" -> s"$premiumsOfLeaseGrantAgreed",
              "premiumsOfLeaseGrant"       -> s"$premiumsOfLeaseGrant"
            )
          )
          boundForm.value.value mustBe ForeignPremiumsGrantLease(premiumsOfLeaseGrantAgreed, Some(premiumsOfLeaseGrant))
          boundForm.errors mustBe empty
        }

        "and no amount is entered, should fail to bind" in {
          val boundForm = form.bind(Map("premiumsOfLeaseGrantAgreed" -> s"$premiumsOfLeaseGrantAgreed"))
          boundForm.errors must contain(
            FormError(
              "premiumsOfLeaseGrant",
              s"foreignPremiumsGrantLease.error.amount.required.$individualOrAgent"
            )
          )
        }

        "and a non numeric value is entered then should fail to bind" in {
          val boundForm =
            form.bind(
              Map(
                "premiumsOfLeaseGrantAgreed" -> s"$premiumsOfLeaseGrantAgreed",
                "premiumsOfLeaseGrant"       -> "non-numeric-value"
              )
            )
          boundForm.errors must contain(
            FormError(
              "premiumsOfLeaseGrant",
              s"foreignPremiumsGrantLease.error.amount.nonNumeric.$individualOrAgent"
            )
          )
        }

        "and an amount is entered that has more than 2 decimal places then it should fail to bind" in {
          val boundForm =
            form.bind(
              Map(
                "premiumsOfLeaseGrantAgreed" -> s"$premiumsOfLeaseGrantAgreed",
                "premiumsOfLeaseGrant"       -> "4534.6545"
              )
            )
          boundForm.errors must contain(
            FormError(
              "premiumsOfLeaseGrant",
              "foreignPremiumsGrantLease.error.amount.twoDecimalPlaces"
            )
          )
        }

        "and an amount is entered that is out of range then should fail to bind" in {
          val boundForm = form.bind(
            Map(
              "premiumsOfLeaseGrantAgreed" -> s"$premiumsOfLeaseGrantAgreed",
              "premiumsOfLeaseGrant"       -> "45334553534535345435345345434.65"
            )
          )
          boundForm.errors must contain(
            FormError(
              "premiumsOfLeaseGrant",
              "foreignPremiumsGrantLease.error.outOfRange",
              ArraySeq(0, 100000000)
            )
          )
        }
      }
    }
  }
}
