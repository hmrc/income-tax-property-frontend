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

import base.SpecBase
import forms.behaviours.StringFieldBehaviours
import models.{EsbaAddress, Rentals, StructuredBuildingAllowanceAddress, UserAnswers}
import pages.enhancedstructuresbuildingallowance.EsbaAddressPage
import pages.structurebuildingallowance.StructuredBuildingAllowanceAddressPage
import play.api.data.FormError

class EsbaAddressFormProviderSpec extends StringFieldBehaviours with SpecBase {

  val buildingNameInEsba = "name"
  val buildingNumberInEsba = "1"
  val postCodeInEsba = "HT45 9GD"

  val buildingNameInSba = "name2"
  val buildingNumberInSba = "1"
  val postCodeInSba = "HT45 9GD"

  val uaDuplicateInEsba: UserAnswers = emptyUserAnswers
    .set(
      EsbaAddressPage(0, Rentals),
      EsbaAddress(buildingNameInEsba, buildingNumberInEsba, postCodeInEsba)
    )
    .get

  val uaDuplicateInSba: UserAnswers = emptyUserAnswers
    .set(
      StructuredBuildingAllowanceAddressPage(0, Rentals),
      StructuredBuildingAllowanceAddress(buildingNameInSba, buildingNumberInSba, postCodeInSba)
    )
    .get

  val form = new EsbaAddressFormProvider()(emptyUserAnswers, Rentals, 0)
  ".buildingName" - {

    val fieldName = "buildingName"
    val requiredKey = "esbaAddress.buildingName.error.required"
    val lengthKey = "esbaAddress.buildingName.error.max"
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
    val requiredKey = "esbaAddress.buildingNumber.error.required"
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
    val requiredKey = "esbaAddress.postcode.error.required"
    val validDataGenerator = "HT45 9GD"

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
    "should give duplicate error for same address within ESBA" in {
      val anotherIndexInEsbaSection = 1 // Apart from the index that is being changed (which should be ignored).
      val formDuplicateInEsba = new EsbaAddressFormProvider()(uaDuplicateInEsba, Rentals, anotherIndexInEsbaSection)
      val requiredError = "esbaAddress.duplicateEsba"
      val result = formDuplicateInEsba.bind(
        Map(
          "postcode"       -> postCodeInEsba,
          "buildingName"   -> buildingNameInEsba,
          "buildingNumber" -> buildingNumberInEsba
        )
      )
      result.errors.head.messages.head mustEqual requiredError
    }

    "should give duplicate error for same address within SBA" in {
      val theChangedIndexInEsbaSection = 0
      val formDuplicateInSba = new EsbaAddressFormProvider()(uaDuplicateInSba, Rentals, theChangedIndexInEsbaSection)
      val requiredError = "esbaAddress.duplicateSba"
      val result = formDuplicateInSba.bind(
        Map("postcode" -> postCodeInSba, "buildingName" -> buildingNameInSba, "buildingNumber" -> buildingNumberInSba)
      )
      result.errors.head.messages.head mustEqual requiredError
    }

    "should ignore duplicate error for same address within ESBA when in change mode for the changed entity" in {
      val theChangedIndexInEsbaSection = 0
      val formDuplicateInEsba = new EsbaAddressFormProvider()(uaDuplicateInEsba, Rentals, theChangedIndexInEsbaSection)
      val result = formDuplicateInEsba.bind(
        Map(
          "postcode"       -> postCodeInEsba,
          "buildingName"   -> buildingNameInEsba,
          "buildingNumber" -> buildingNumberInEsba
        )
      )
      result.errors mustEqual List.empty
    }

    "should NOT ignore duplicate error for same address within ESBA when in change mode for the changed entity" in {
      val theChangedIndexInESBASection = 0
      val formDuplicateInSba = new EsbaAddressFormProvider()(uaDuplicateInSba, Rentals, theChangedIndexInESBASection)
      val requiredError = "esbaAddress.duplicateSba"
      val result = formDuplicateInSba.bind(
        Map("postcode" -> postCodeInSba, "buildingName" -> buildingNameInSba, "buildingNumber" -> buildingNumberInSba)
      )
      result.errors.head.messages.head mustEqual requiredError
    }
  }
}
