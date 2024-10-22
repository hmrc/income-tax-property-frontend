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
import models.Addressable._
import models.{EsbaAddress, PropertyType, StructuredBuildingAllowanceAddress, UserAnswers}
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.validation.Constraints.pattern

import javax.inject.Inject
import scala.util.matching.Regex

class EsbaAddressFormProvider @Inject() extends Mappings {
  val postcodeRegex: Regex =
    "^([Gg][Ii][Rr] 0[Aa]{2})|((([A-Za-z][0-9]{1,2})|(([A-Za-z][A-Ha-hJ-Yj-y][0-9]{1,2})|(([A-Za-z][0-9][A-Za-z])|([A-Za-z][A-Ha-hJ-Yj-y][0-9][A-Za-z]?))))\\s?[0-9][A-Za-z]{2})$".r

  def apply(userAnswers: UserAnswers, propertyType: PropertyType, indexToIgnore: Int): Form[EsbaAddress] = {
    val currentIndexNotToCheckAgainstInEsbaSection = Some(indexToIgnore)
    val indexToCheckAgainstInOtherSection = None
    Form(
      mapping(
        "buildingName" -> text("esbaAddress.buildingName.error.required")
          .verifying(maxLength(90, "esbaAddress.buildingName.error.max")),
        "buildingNumber" -> text("esbaAddress.buildingNumber.error.required"),
        "postcode" -> text("esbaAddress.postcode.error.required").verifying(
          pattern(postcodeRegex, "PostCode", "esbaAddress.postcode.error.invalid")
        )
      )(EsbaAddress.apply)(EsbaAddress.unapply)
        .verifying(
          checkIfAddressAlreadyEntered[EsbaAddress, EsbaAddress](
            getAddresses[EsbaAddress](
              userAnswers,
              propertyType,
              currentIndexNotToCheckAgainstInEsbaSection
            ),
            "esbaAddress.duplicateEsba"
          )
        )
        .verifying(
          checkIfAddressAlreadyEntered[EsbaAddress, StructuredBuildingAllowanceAddress](
            getAddresses[StructuredBuildingAllowanceAddress](
              userAnswers,
              propertyType,
              indexToCheckAgainstInOtherSection
            ),
            "esbaAddress.duplicateSba"
          )
        )
    )
  }
}
