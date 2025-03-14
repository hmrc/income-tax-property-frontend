/*
 * Copyright 2025 HM Revenue & Customs
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

import audit._
import pages.PageConstants.{eSbaPath, esbas, foreignSbaFormGroup, sbaPath, structureBuildingFormGroup}
import pages.enhancedstructuresbuildingallowance.Esba
import play.api.libs.json.{JsPath, Json, OFormat}
import queries.{Gettable, Settable}

import java.time.LocalDate

final case class Adjustments(
  balancingCharge: BalancingCharge,
  privateUseAdjustment: PrivateUseAdjustment,
  propertyIncomeAllowance: BigDecimal,
  renovationAllowanceBalancingCharge: RenovationAllowanceBalancingCharge,
  residentialFinanceCost: BigDecimal,
  unusedResidentialFinanceCost: BigDecimal,
  unusedLossesBroughtForward: Option[UnusedLossesBroughtForward],
  whenYouReportedTheLoss: Option[WhenYouReportedTheLoss]
)

object Adjustments {
  implicit val format: OFormat[Adjustments] = Json.format[Adjustments]
}

final case class Allowances(
  annualInvestmentAllowance: Option[BigDecimal],
  businessPremisesRenovationAllowance: Option[BigDecimal],
  otherCapitalAllowance: Option[BigDecimal],
  replacementOfDomesticGoodsAllowance: Option[BigDecimal],
  zeroEmissionCarAllowance: Option[BigDecimal],
  zeroEmissionGoodsVehicleAllowance: Option[BigDecimal]
)

object Allowances {
  implicit val format: OFormat[Allowances] = Json.format[Allowances]
}
final case class PropertyRentals(
  propertyRentalsIncome: RentalsIncome,
  propertyRentalsExpense: RentalsExpense,
  propertyRentalsAbout: RentalsAbout
)
object PropertyRentals {
  implicit val format: OFormat[PropertyRentals] = Json.format[PropertyRentals]
}

final case class PremiumLease(
  calculatedFigureYourself: CalculatedFigureYourself,
  premiumForLease: Boolean,
  premiumsGrantLease: PremiumsGrantLease,
  receivedGrantLeaseAmount: BigDecimal,
  yearLeaseAmount: Int
)

object PremiumLease {
  implicit val format: OFormat[PremiumLease] = Json.format[PremiumLease]
}

final case class Sba(
  structuredBuildingAllowanceAddress: StructuredBuildingAllowanceAddress,
  structureBuildingQualifyingDate: LocalDate,
  structureBuildingQualifyingAmount: BigDecimal,
  structureBuildingAllowanceClaim: BigDecimal
)

object Sba {
  implicit val format: OFormat[Sba] = Json.format[Sba]
}

final case class SbaOnIndex(index: Int, propertyType: PropertyType) extends Gettable[Sba] {
  override def path: JsPath =
    JsPath \ sbaPath(propertyType) \ structureBuildingFormGroup \ index
}

final case class SbasWithSupportingQuestions(
  claimStructureBuildingAllowance: Boolean,
  sbaClaims: Option[Boolean],
  structureBuildingFormGroup: List[Sba]
)

object SbasWithSupportingQuestions
    extends Gettable[SbasWithSupportingQuestions] with Settable[SbasWithSupportingQuestions] {
  implicit val format: OFormat[SbasWithSupportingQuestions] = Json.format[SbasWithSupportingQuestions]

  override def path: JsPath = JsPath \ toString

  override def toString: String = sbaPath(Rentals)
}

final case class ForeignSba(
  foreignStructureBuildingAddress: ForeignStructuresBuildingAllowanceAddress,
  foreignStructureBuildingQualifyingDate: LocalDate,
  foreignStructureBuildingQualifyingAmount: BigDecimal,
  foreignStructureBuildingAllowanceClaim: BigDecimal
)

object ForeignSba {
  implicit val format: OFormat[ForeignSba] = Json.format[ForeignSba]
}

final case class ForeignSbaOnIndex(index: Int, countryCode: String) extends Gettable[ForeignSba] {
  override def path: JsPath =
    JsPath \ sbaPath(ForeignProperty) \ countryCode.toUpperCase \ foreignSbaFormGroup \ index
}

final case class EsbasWithSupportingQuestions(
  claimEnhancedStructureBuildingAllowance: Boolean,
  enhancedStructureBuildingAllowanceClaims: Option[Boolean],
  enhancedStructureBuildingAllowances: List[Esba]
)

object EsbasWithSupportingQuestions {
  implicit val format: OFormat[EsbasWithSupportingQuestions] = Json.format[EsbasWithSupportingQuestions]

}

final case class EsbasWithSupportingQuestionsPage(propertyType: PropertyType)
    extends Gettable[EsbasWithSupportingQuestions] with Settable[EsbasWithSupportingQuestions] {

  override def path: JsPath = JsPath \ eSbaPath(propertyType)

  override def toString: String = esbas
}

final case class JourneyWithStatus(journeyName: String, journeyStatus: String)

object JourneyWithStatus {
  implicit val format: OFormat[JourneyWithStatus] = Json.format[JourneyWithStatus]
}

final case class FetchedBackendData(
  capitalAllowancesForACar: Option[CapitalAllowancesForACar],
  propertyAbout: Option[models.PropertyAbout],
  propertyRentalsAbout: Option[RentalsAbout],
  rentalsAndRaRAbout: Option[RentalsAndRaRAbout],
  adjustments: Option[Adjustments],
  rentalsAndRaRAdjustments: Option[RentalsAndRentARoomAdjustment],
  allowances: Option[Allowances],
  esbasWithSupportingQuestions: Option[EsbasWithSupportingQuestions],
  rentalsAndRaREsbasWithSupportingQuestions: Option[EsbasWithSupportingQuestions],
  sbasWithSupportingQuestions: Option[SbasWithSupportingQuestions],
  rentalsAndRaRSbasWithSupportingQuestions: Option[SbasWithSupportingQuestions],
  propertyRentalsIncome: Option[RentalsIncome],
  rentalsAndRaRIncome: Option[RentalsAndRentARoomIncome],
  propertyRentalsExpenses: Option[RentalsExpense],
  raRAbout: Option[RaRAbout],
  rarExpenses: Option[RentARoomExpenses],
  raRAdjustments: Option[RentARoomAdjustments],
  rentARoomAllowances: Option[RentARoomAllowance],
  journeyStatuses: List[JourneyWithStatus],
  foreignPropertySelectCountry: Option[models.ForeignPropertySelectCountry]
)

object FetchedBackendData {
  implicit val format: OFormat[FetchedBackendData] = Json.format[FetchedBackendData]
}
