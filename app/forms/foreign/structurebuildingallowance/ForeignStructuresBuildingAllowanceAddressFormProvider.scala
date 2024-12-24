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

package forms.foreign.structurebuildingallowance

import forms.mappings.Mappings
import models.{ForeignAddressable, ForeignStructuresBuildingAllowanceAddress, UserAnswers}
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class ForeignStructuresBuildingAllowanceAddressFormProvider @Inject() extends Mappings {

  def apply(
    userAnswers: UserAnswers,
    countryCode: String,
    indexToExclude: Int
  ): Form[ForeignStructuresBuildingAllowanceAddress] =
    Form(
      mapping(
        "buildingName" -> text("foreignStructuresBuildingAllowanceAddress.error.buildingName.required")
          .verifying(maxLength(90, "foreignStructuresBuildingAllowanceAddress.error.buildingName.length")),
        "buildingNumber" -> text("foreignStructuresBuildingAllowanceAddress.error.buildingNumber.required"),
        "postcode"       -> text("foreignStructuresBuildingAllowanceAddress.error.postcode.required")
      )(ForeignStructuresBuildingAllowanceAddress.apply)(ForeignStructuresBuildingAllowanceAddress.unapply)
        .verifying(
          checkIfForeignAddressAlreadyEntered[
            ForeignStructuresBuildingAllowanceAddress,
            ForeignStructuresBuildingAllowanceAddress
          ](
            ForeignAddressable
              .getForeignAddresses[ForeignStructuresBuildingAllowanceAddress](
                userAnswers,
                countryCode,
                Some(indexToExclude)
              ),
            "foreignStructuresBuildingAllowanceAddress.error.duplicate"
          )
        )
    )
}
