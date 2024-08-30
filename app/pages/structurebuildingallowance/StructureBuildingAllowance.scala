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

package pages.structurebuildingallowance

import models.{PropertyType, StructuredBuildingAllowanceAddress}
import pages.PageConstants
import pages.PageConstants.sbaPath
import play.api.libs.json.{Format, JsPath, Json}
import queries.{Gettable, Settable}

import java.time.LocalDate

case class StructureBuildingAllowance(
  structureBuildingQualifyingDate: LocalDate,
  structureBuildingQualifyingAmount: BigDecimal,
  structureBuildingAllowanceClaim: BigDecimal,
  structuredBuildingAllowanceAddress: StructuredBuildingAllowanceAddress
)

object StructureBuildingAllowance {
  implicit val format: Format[StructureBuildingAllowance] = Json.format[StructureBuildingAllowance]
}

case class StructureBuildingAllowanceGroup(propertyType: PropertyType)
    extends Gettable[Array[StructureBuildingAllowance]] with Settable[Array[StructureBuildingAllowance]] {
  override def path: JsPath = JsPath \ sbaPath(propertyType) \ toString

  override def toString: String = PageConstants.structureBuildingFormGroup
}

case class StructureBuildingAllowanceWithIndex(index: Int, propertyType: PropertyType)
    extends Settable[Array[StructureBuildingAllowance]] {

  implicit val format: Format[StructureBuildingAllowance] = Json.format

  override def path: JsPath = JsPath \ sbaPath(propertyType) \ toString \ index

  override def toString: String = PageConstants.structureBuildingFormGroup
}
