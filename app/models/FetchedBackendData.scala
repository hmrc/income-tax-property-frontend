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

import audit.{PropertyAbout, RentalsAbout, RentalsExpense, RentalsIncome, RentARoomAdjustments, RentARoomAllowance, RentARoomExpenses}
import pages.PageConstants.esbasWithSupportingQuestions
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
  unusedResidentialFinanceCost: BigDecimal
)

object Adjustments {
  implicit val format = Json.format[Adjustments]
}

final case class Allowances(
  annualInvestmentAllowance: BigDecimal,
  businessPremisesRenovationAllowance: BigDecimal,
  electricChargePointAllowance: ElectricChargePointAllowance,
  otherCapitalAllowance: BigDecimal,
  replacementOfDomesticGoodsAllowance: BigDecimal,
  zeroEmissionCarAllowance: BigDecimal,
  zeroEmissionGoodsVehicleAllowance: BigDecimal
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
  leasePremiumPayment: Boolean,
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

final case class SbasWithSupportingQuestions(
  claimStructureBuildingAllowance: Boolean,
  structureBuildingFormGroup: List[Sba]
)

object SbasWithSupportingQuestions {
  implicit val format: OFormat[SbasWithSupportingQuestions] = Json.format[SbasWithSupportingQuestions]
}

final case class EsbasWithSupportingQuestions(
  claimEnhancedStructureBuildingAllowance: Boolean,
  esbaClaims: Option[Boolean],
  esbas: List[Esba]
)

object EsbasWithSupportingQuestions
    extends Gettable[EsbasWithSupportingQuestions] with Settable[EsbasWithSupportingQuestions] {
  implicit val format: OFormat[EsbasWithSupportingQuestions] = Json.format[EsbasWithSupportingQuestions]

  override def path: JsPath = JsPath \ toString

  override def toString: String = esbasWithSupportingQuestions
}

final case class FetchedBackendData(
                                     capitalAllowancesForACar: Option[CapitalAllowancesForACar],
                                     propertyAbout: Option[PropertyAbout],
                                     propertyRentalsAbout: Option[RentalsAbout],
                                     adjustments: Option[Adjustments],
                                     allowances: Option[Allowances],
                                     esbasWithSupportingQuestions: Option[EsbasWithSupportingQuestions],
                                     sbasWithSupportingQuestions: Option[SbasWithSupportingQuestions],
                                     propertyRentals: Option[PropertyRentals],
                                     propertyRentalsIncome: Option[RentalsIncome],
                                     propertyRentalsExpenses: Option[RentalsExpense],
                                     raRAbout: Option[RaRAbout],
                                     rarExpenses: Option[RentARoomExpenses],
                                     raRAdjustments: Option[RentARoomAdjustments],
                                     rentARoomAllowances: Option[RentARoomAllowance]
)

object FetchedBackendData {
  implicit val format: OFormat[FetchedBackendData] = Json.format[FetchedBackendData]
}
