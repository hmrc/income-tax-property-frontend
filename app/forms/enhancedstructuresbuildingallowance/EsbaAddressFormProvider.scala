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

package forms.enhancedstructuresbuildingallowance

import forms.mappings.Mappings
import models.{EsbaAddress, UserAnswers}
import pages.enhancedstructuresbuildingallowance.EsbaAddressPage
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.validation.Constraints.pattern

import javax.inject.Inject
import scala.util.matching.Regex

class EsbaAddressFormProvider @Inject() extends Mappings {
  val postcodeRegex: Regex = "^([Gg][Ii][Rr] 0[Aa]{2})|((([A-Za-z][0-9]{1,2})|(([A-Za-z][A-Ha-hJ-Yj-y][0-9]{1,2})|(([A-Za-z][0-9][A-Za-z])|([A-Za-z][A-Ha-hJ-Yj-y][0-9][A-Za-z]?))))\\s?[0-9][A-Za-z]{2})$".r

  def apply(userAnswers: UserAnswers): Form[EsbaAddress] =
    Form(mapping(
      "buildingName" -> text("esbaAddress.buildingName.error.required")
        .verifying(maxLength(90, "esbaAddress.buildingName.error.max")),
      "buildingNumber" -> text("esbaAddress.buildingNumber.error.required"),
      "postcode" -> text("esbaAddress.postcode.error.required").verifying(
        pattern(postcodeRegex, "PostCode", "esbaAddress.postcode.error.invalid")))
    (EsbaAddress.apply)(EsbaAddress.unapply
    ).verifying(
      checkIfAddressAlreadyEntered(
        getAddresses(0, userAnswers, Nil),
        "esbaAddress.duplicate")
    )
    )

  private def getAddresses(index: Int, userAnswers: UserAnswers, addresses: List[EsbaAddress]): List[EsbaAddress] = {
    userAnswers.get(EsbaAddressPage(index)) match {
      case Some(ea) => getAddresses(index + 1, userAnswers, ea :: addresses)
      case None => addresses
    }
  }
}

object EsbaAddressFormProvider {
  implicit class EsbaAddressExtension(esbaAddress: EsbaAddress) {
    private def standardise(info: String): String = info.trim.toLowerCase().filterNot(_.isSpaceChar)

    def checkAddresses(other: EsbaAddress): Boolean = {
      standardise(esbaAddress.postCode).equals(standardise(other.postCode)) &&
        standardise(esbaAddress.buildingName).equals(standardise(other.buildingName)) &&
        standardise(esbaAddress.buildingNumber).equals(standardise(other.buildingNumber))
    }
  }
}

