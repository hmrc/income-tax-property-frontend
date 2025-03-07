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

package pages.enhancedstructuresbuildingallowance

import models.PropertyType
import pages.PageConstants
import pages.PageConstants.eSbaPath
import play.api.libs.json.{Format, JsPath, Json}
import queries.{Gettable, Settable}

import java.time.LocalDate

case class EnhancedStructuresBuildingAllowance(
                                                enhancedStructureBuildingAllowanceQualifyingDate: LocalDate,
                                                enhancedStructureBuildingAllowanceQualifyingAmount: BigDecimal,
                                                enhancedStructureBuildingAllowanceClaim: BigDecimal
)

case object EnhancedStructuresBuildingAllowance {
  implicit val format: Format[EnhancedStructuresBuildingAllowance] = Json.format
}

case class EnhancedStructureBuildingAllowanceGroup(propertyType: PropertyType)
    extends Gettable[Array[EnhancedStructuresBuildingAllowance]]
    with Settable[Array[EnhancedStructuresBuildingAllowance]] {
  override def path: JsPath = JsPath \ eSbaPath(propertyType) \ toString

  override def toString: String = PageConstants.esbas
}

case class EnhancedStructuresBuildingAllowanceWithIndex(index: Int, propertyType: PropertyType)
    extends Settable[Array[EnhancedStructuresBuildingAllowance]] {

  implicit val format: Format[EnhancedStructuresBuildingAllowance] = Json.format

  override def path: JsPath = JsPath \ eSbaPath(propertyType) \ toString \ index

  override def toString: String = PageConstants.esbas
}
