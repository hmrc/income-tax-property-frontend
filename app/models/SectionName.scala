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

package models

sealed trait SectionName

object SectionName extends Enumerable.Implicits {
  case object About extends WithName("About") with SectionName
  case object Income extends WithName("Income") with SectionName
  case object Allowances extends WithName("Allowances") with SectionName
  case object Adjustments extends WithName("Adjustments") with SectionName
  case object Expenses extends WithName("Expenses") with SectionName
  case object SBA extends WithName("SBA") with SectionName
  case object ESBA extends WithName("EnhancedStructureAndBuildingAllowance") with SectionName
  case object ForeignPropertyTax extends WithName("ForeignPropertyTax") with SectionName
  case object ForeignPropertySelectCountry extends WithName("ForeignPropertySelectCountry") with SectionName
  case object ForeignPropertyIncome extends WithName("ForeignPropertyIncome") with SectionName
  case object ForeignPropertyExpenses extends WithName("ForeignPropertyExpenses") with SectionName
  case object ForeignPropertyAdjustments extends WithName("ForeignPropertyAdjustments") with SectionName
  case object ForeignStructureBuildingAllowance extends WithName("foreign-property-sba") with SectionName



  val values: Seq[SectionName] = Seq(
    About,
    Income,
    Allowances,
    Adjustments,
    Expenses,
    SBA,
    ESBA,
    ForeignPropertySelectCountry,
    ForeignPropertyTax,
    ForeignPropertyIncome,
    ForeignPropertyExpenses,
    ForeignStructureBuildingAllowance
  )

  implicit val enumerable: Enumerable[SectionName] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
