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

package pages.enhancedstructuresbuildingallowance

import models.EsbaAddress
import pages.PageConstants
import play.api.libs.json.{JsPath, Json, OFormat}
import queries.{Gettable, Settable}

import java.time.LocalDate

final case class Esba(
  esbaQualifyingDate: LocalDate,
  esbaQualifyingAmount: BigDecimal,
  esbaClaim: BigDecimal,
  esbaAddress: EsbaAddress
)

object Esba {
  implicit val formatter: OFormat[Esba] = Json.format[Esba]
}

final case class EsbaOnIndex(index: Int) extends Gettable[Esba] {
  override def path: JsPath =
    JsPath \ PageConstants.esbasWithSupportingQuestions \ PageConstants.esbaFormGroup \ index
}

object Esbas extends Gettable[List[Esba]] with Settable[List[Esba]] {

  override def path: JsPath = JsPath \ PageConstants.esbasWithSupportingQuestions \ toString

  override def toString: String = PageConstants.esbaFormGroup
}
