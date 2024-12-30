/*
 * Copyright 2023 HM Revenue & Customs
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

package pages.foreign.structurebuildingallowance

import models.ForeignProperty
import pages.PageConstants.{foreignSbaFormGroup, sbaPath}
import pages.QuestionPage
import play.api.libs.json.JsPath

case class ForeignStructureBuildingQualifyingAmountPage(countryCode: String, index: Int)
    extends QuestionPage[BigDecimal] {

  override def path: JsPath =
    JsPath \ sbaPath(ForeignProperty) \ countryCode.toUpperCase \ foreignSbaFormGroup \ index \ toString

  override def toString: String = "foreignStructureBuildingQualifyingAmount"
}
