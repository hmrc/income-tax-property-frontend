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

package forms.structurebuildingallowance

import base.SpecBase
import forms.behaviours.StringFieldBehaviours
import models.{EsbaAddress, Rentals, StructuredBuildingAllowanceAddress, UserAnswers}
import pages.enhancedstructuresbuildingallowance.EsbaAddressPage
import pages.structurebuildingallowance.StructuredBuildingAllowanceAddressPage
import play.api.data.FormError

class StructuredBuildingAllowanceAddressFormProviderSpec extends StringFieldBehaviours with SpecBase {

  val buildingNameInSba = "High Building"
  val buildingNumberInSba = "100"
  val postCodeInSba = "SE12 8JF"

  val buildingNameInEsba = "Low Building"
  val buildingNumberInEsba = "200"
  val postCodeInEsba = "SE13 5DL"

  val uaDuplicateInSba: UserAnswers = emptyUserAnswers
    .set(
      StructuredBuildingAllowanceAddressPage(0, Rentals),
      StructuredBuildingAllowanceAddress(buildingNameInSba, buildingNumberInSba, postCodeInSba)
    )
    .get

  val uaDuplicateInEsba: UserAnswers = emptyUserAnswers
    .set(
      EsbaAddressPage(0, Rentals),
      EsbaAddress(buildingNameInEsba, buildingNumberInEsba, postCodeInEsba)
    )
    .get

  val sbaForm = new StructuredBuildingAllowanceAddressFormProvider()(emptyUserAnswers, Rentals, 0)
  ".buildingName" - {

    val fieldName = "buildingName"
    val requiredKey = "structureBuildingAllowanceAddress.buildingName.error.required"
    val lengthKey = "structureBuildingAllowanceAddress.buildingName.error.max"
    val maxLength = 90

    behave like fieldThatBindsValidData(
      sbaForm,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      sbaForm,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      sbaForm,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".buildingNumber" - {

    val fieldName = "buildingNumber"
    val requiredKey = "structureBuildingAllowanceAddress.buildingNumber.error.required"
    val validDataGenerator = "10"

    behave like fieldThatBindsValidData(
      sbaForm,
      fieldName,
      validDataGenerator
    )

    behave like mandatoryField(
      sbaForm,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".postcode" - {

    val fieldName = "postcode"
    val requiredKey = "structureBuildingAllowanceAddress.postcode.error.required"
    val validDataGenerator = "SE13 5DL"

    behave like fieldThatBindsValidData(
      sbaForm,
      fieldName,
      validDataGenerator
    )

    behave like mandatoryField(
      sbaForm,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  "existing address" - {
    "should give duplicate error for same address within Structured Building Allowance" in {
      val anotherIndexInSbaSection = 1 // Apart from the index that is being changed (which should be ignored).
      val formDuplicateInSba =
        new StructuredBuildingAllowanceAddressFormProvider()(uaDuplicateInSba, Rentals, anotherIndexInSbaSection)
      val requiredError = "structureBuildingAllowanceAddress.duplicateSba"
      val result = formDuplicateInSba.bind(
        Map("postcode" -> postCodeInSba, "buildingName" -> buildingNameInSba, "buildingNumber" -> buildingNumberInSba)
      )
      result.errors.head.messages.head mustEqual requiredError
    }

    "should give duplicate error for same address within Enhanced Structured Building Allowance" in {
      val formDuplicateInEsba = new StructuredBuildingAllowanceAddressFormProvider()(uaDuplicateInEsba, Rentals, 0)
      val requiredError = "structureBuildingAllowanceAddress.duplicateEsba"
      val result = formDuplicateInEsba.bind(
        Map(
          "postcode"       -> postCodeInEsba,
          "buildingName"   -> buildingNameInEsba,
          "buildingNumber" -> buildingNumberInEsba
        )
      )
      result.errors.head.messages.head mustEqual requiredError
    }

    "should NOT ignore duplicate error for same address within Structured Building Allowance when in change mode for the changed entity" in {
      val theChangedIndexInSbaSection = 0
      val formDuplicateInEsba =
        new StructuredBuildingAllowanceAddressFormProvider()(uaDuplicateInEsba, Rentals, theChangedIndexInSbaSection)
      val requiredError = "structureBuildingAllowanceAddress.duplicateEsba"
      val result = formDuplicateInEsba.bind(
        Map(
          "postcode"       -> postCodeInEsba,
          "buildingName"   -> buildingNameInEsba,
          "buildingNumber" -> buildingNumberInEsba
        )
      )
      result.errors.head.messages.head mustEqual requiredError
    }

    "should ignore duplicate error for same address within Structured Building Allowance when in change mode for the changed entity" in {
      val theChangedIndexInSbaSection = 0
      val formDuplicateInSba =
        new StructuredBuildingAllowanceAddressFormProvider()(uaDuplicateInSba, Rentals, theChangedIndexInSbaSection)

      val result = formDuplicateInSba.bind(
        Map("postcode" -> postCodeInSba, "buildingName" -> buildingNameInSba, "buildingNumber" -> buildingNumberInSba)
      )
      result.errors mustEqual List.empty
    }

  }

}
