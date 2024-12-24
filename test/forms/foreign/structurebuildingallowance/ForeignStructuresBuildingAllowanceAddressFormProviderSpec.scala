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

import base.SpecBase
import forms.behaviours.StringFieldBehaviours
import models.{ForeignStructuresBuildingAllowanceAddress, UserAnswers}
import pages.foreign.structurebuildingallowance.ForeignStructuresBuildingAllowanceAddressPage
import play.api.data.FormError

class ForeignStructuresBuildingAllowanceAddressFormProviderSpec extends StringFieldBehaviours with SpecBase {

  val countryCode: String = "AUS"

  val buildingNameInForeignSba = "High Building"
  val buildingNumberInForeignSba = "100"
  val postCodeInForeignSba = "SE12 8JF"

  val uaDuplicateInForeignSba: UserAnswers = emptyUserAnswers
    .set(
      ForeignStructuresBuildingAllowanceAddressPage(0, countryCode),
      ForeignStructuresBuildingAllowanceAddress(
        buildingNameInForeignSba,
        buildingNumberInForeignSba,
        postCodeInForeignSba
      )
    )
    .get

  val form = new ForeignStructuresBuildingAllowanceAddressFormProvider()(emptyUserAnswers, countryCode, 0)

  ".buildingName" - {

    val fieldName = "buildingName"
    val requiredKey = "foreignStructuresBuildingAllowanceAddress.error.buildingName.required"
    val lengthKey = "foreignStructuresBuildingAllowanceAddress.error.buildingName.length"
    val maxLength = 90

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".buildingNumber" - {

    val fieldName = "buildingNumber"
    val requiredKey = "foreignStructuresBuildingAllowanceAddress.error.buildingNumber.required"
    val validDataGenerator = "10"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
  ".postcode" - {

    val fieldName = "postcode"
    val requiredKey = "foreignStructuresBuildingAllowanceAddress.error.postcode.required"
    val validDataGenerator = "postcode"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  "existing address" - {
    "should give duplicate error for same address within Foreign Structures Building Allowance" in {
      val anotherIndexInSbaSection = 1 // Apart from the index that is being changed (which should be ignored).
      val formDuplicateInSba =
        new ForeignStructuresBuildingAllowanceAddressFormProvider()(
          uaDuplicateInForeignSba,
          countryCode,
          anotherIndexInSbaSection
        )
      val requiredError = "foreignStructuresBuildingAllowanceAddress.error.duplicate"
      val result = formDuplicateInSba.bind(
        Map(
          "postcode"       -> postCodeInForeignSba,
          "buildingName"   -> buildingNameInForeignSba,
          "buildingNumber" -> buildingNumberInForeignSba
        )
      )
      result.errors.head.messages.head mustEqual requiredError
    }

    "should ignore duplicate error for same address within Foreign Structures Building Allowance when in change mode for the changed entity" in {
      val theChangedIndexInSbaSection = 0
      val formDuplicateInSba =
        new ForeignStructuresBuildingAllowanceAddressFormProvider()(
          uaDuplicateInForeignSba,
          countryCode,
          theChangedIndexInSbaSection
        )

      val result = formDuplicateInSba.bind(
        Map(
          "postcode"       -> postCodeInForeignSba,
          "buildingName"   -> buildingNameInForeignSba,
          "buildingNumber" -> buildingNumberInForeignSba
        )
      )
      result.errors mustEqual List.empty
    }
  }
}
