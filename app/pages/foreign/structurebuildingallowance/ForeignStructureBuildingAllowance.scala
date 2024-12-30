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

import models.ForeignStructuresBuildingAllowanceAddress
import pages.PageConstants
import pages.PageConstants.sbaPath
import models.ForeignProperty
import play.api.libs.json.{Format, JsPath, Json}
import queries.{Gettable, Settable}

import java.time.LocalDate

case class ForeignStructureBuildingAllowance(
                                       foreignStructureBuildingQualifyingDate: LocalDate,
                                       foreignStructureBuildingQualifyingAmount: BigDecimal,
                                       foreignStructureBuildingAllowanceClaim: BigDecimal,
                                       foreignStructuredBuildingAllowanceAddress: ForeignStructuresBuildingAllowanceAddress
)

object ForeignStructureBuildingAllowance {
  implicit val format: Format[ForeignStructureBuildingAllowance] = Json.format[ForeignStructureBuildingAllowance]
}

case class ForeignStructureBuildingAllowanceGroup(countryCode: String)
    extends Gettable[Array[ForeignStructureBuildingAllowance]] with Settable[Array[ForeignStructureBuildingAllowance]] {
  override def path: JsPath = JsPath \ sbaPath(ForeignProperty) \ countryCode.toUpperCase \ toString

  override def toString: String = "foreignStructureBuildingFormGroup"
}

case class ForeignStructureBuildingAllowanceWithIndex(countryCode: String)
    extends Settable[Array[ForeignStructureBuildingAllowance]] {

  implicit val format: Format[ForeignStructureBuildingAllowance] = Json.format

  override def path: JsPath = JsPath \ sbaPath(ForeignProperty) \ countryCode.toUpperCase \ toString

  override def toString: String = "foreignStructureBuildingFormGroup"
}
