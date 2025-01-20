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

package pages.foreign.structurebuildingallowance

import models.{ForeignProperty, ForeignStructuresBuildingAllowanceAddress}
import pages.PageConstants.{foreignSbaFormGroup, sbaPath}
import play.api.libs.json.{Format, JsPath, Json, OFormat}
import queries.{Gettable, Settable}

import java.time.LocalDate

case class ForeignStructureBuildingAllowance(
  foreignStructureBuildingAllowanceClaim: BigDecimal,
  foreignStructureBuildingQualifyingDate: LocalDate,
  foreignStructureBuildingQualifyingAmount: BigDecimal,
  foreignStructureBuildingAddress: ForeignStructuresBuildingAllowanceAddress
)

object ForeignStructureBuildingAllowance {
  implicit val format: Format[ForeignStructureBuildingAllowance] = Json.format[ForeignStructureBuildingAllowance]
}

case class ForeignStructureBuildingAllowanceGroup(countryCode: String)
    extends Gettable[Array[ForeignStructureBuildingAllowance]] with Settable[Array[ForeignStructureBuildingAllowance]] {
  override def path: JsPath = JsPath \ sbaPath(ForeignProperty) \ countryCode \ toString
  override def toString: String = foreignSbaFormGroup
}

case class StructuredBuildingAllowanceDate(qualifyingDate: LocalDate, qualifyingAmountExpenditure: BigDecimal)

object StructuredBuildingAllowanceDate {
  implicit val format: OFormat[StructuredBuildingAllowanceDate] = Json.format[StructuredBuildingAllowanceDate]
}

case class StructuredBuildingAllowanceBuilding(name: Option[String], number: Option[String], postCode: String)

object StructuredBuildingAllowanceBuilding {
  implicit val format: OFormat[StructuredBuildingAllowanceBuilding] = Json.format[StructuredBuildingAllowanceBuilding]

}

case class ForeignStructureBuildingAllowanceWithIndex(index: Int, countryCode: String)
    extends Settable[Array[ForeignStructureBuildingAllowance]] {

  implicit val format: Format[ForeignStructureBuildingAllowance] = Json.format

  override def path: JsPath = JsPath \ sbaPath(ForeignProperty) \ countryCode \ toString \ index

  override def toString: String = foreignSbaFormGroup
}
