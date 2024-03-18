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
import models.EsbaAddress
import pages.enhancedstructuresbuildingallowance.EsbaAddressPage
import play.api.data.FormError

class EsbaAddressFormProviderSpec extends StringFieldBehaviours with SpecBase {

  val buildingName = "name"
  val buildingNumber = "1"
  val postCode = "HT45 9GD"
  val ua = emptyUserAnswers.set(EsbaAddressPage(0), EsbaAddress("name", "1", "HT45 9GD")).get
  val form = new EsbaAddressFormProvider()(ua)

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
    "should give duplicate error" in {
      val requiredError = "esbaAddress.duplicate"
      val result = form.bind(Map("postcode" -> postCode, "buildingName" -> buildingName, "buildingNumber" -> buildingNumber))
      result.errors.head.messages.head mustEqual requiredError
    }
  }
}