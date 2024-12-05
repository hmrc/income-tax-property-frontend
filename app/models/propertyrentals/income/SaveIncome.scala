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

package models.propertyrentals.income

import audit.RentalsIncome
import play.api.libs.json.{Json, OFormat}

final case class RentARoomIncome(rentsReceived: BigDecimal)
object RentARoomIncome {
  implicit val format: OFormat[RentARoomIncome] = Json.format[RentARoomIncome]
}
final case class UkOtherPropertyIncome(
  premiumsOfLeaseGrant: Option[BigDecimal],
  reversePremiums: Option[BigDecimal],
  periodAmount: Option[BigDecimal],
  taxDeducted: Option[BigDecimal],
  otherIncome: Option[BigDecimal],
  ukOtherRentARoom: Option[RentARoomIncome]
)

object UkOtherPropertyIncome {
  implicit val format: OFormat[UkOtherPropertyIncome] = Json.format[UkOtherPropertyIncome]
}
final case class UkRentARoomExpense(amountClaimed: BigDecimal)

object UkRentARoomExpense {
  implicit val format: OFormat[UkRentARoomExpense] = Json.format[UkRentARoomExpense]
}

final case class UkOtherPropertyExpenses(
  premisesRunningCosts: Option[BigDecimal],
  repairsAndMaintenance: Option[BigDecimal],
  financialCosts: Option[BigDecimal],
  professionalFees: Option[BigDecimal],
  travelCosts: Option[BigDecimal],
  costOfServices: Option[BigDecimal],
  other: Option[BigDecimal],
  residentialFinancialCost: Option[BigDecimal],
  residentialFinancialCostsCarriedForward: Option[BigDecimal],
  ukOtherRentARoom: Option[UkRentARoomExpense],
  consolidatedExpense: Option[BigDecimal]
)

object UkOtherPropertyExpenses {
  implicit val format: OFormat[UkOtherPropertyExpenses] = Json.format[UkOtherPropertyExpenses]
}

final case class SaveIncome(
  ukOtherPropertyExpenses: Option[UkOtherPropertyExpenses],
  ukOtherPropertyIncome: UkOtherPropertyIncome,
  incomeToSave: Income
)

object SaveIncome {
  implicit val format: OFormat[SaveIncome] = Json.format[SaveIncome]

  def fromPropertyRentalsIncome(propertyRentalsIncome: RentalsIncome): SaveIncome = {
    val ukOtherPropertyIncome = UkOtherPropertyIncome(
      propertyRentalsIncome.premiumsGrantLease.flatMap(_.premiumsGrantLease),
      propertyRentalsIncome.reversePremiumsReceived.flatMap(_.reversePremiums),
      propertyRentalsIncome.yearLeaseAmount,
      propertyRentalsIncome.deductingTax.flatMap(_.taxDeductedAmount),
      Some(propertyRentalsIncome.otherIncomeFromProperty),
      None // Todo: To be fetched from allowances in backend
    )

    val incomeToSave = Income(
      propertyRentalsIncome.isNonUKLandlord,
      propertyRentalsIncome.propertyRentalIncome,
      propertyRentalsIncome.premiumsGrantLease.map(_.premiumsGrantLeaseReceived).getOrElse(false), // ToDo: Recheck
      ReversePremiumsReceived(
        propertyRentalsIncome.reversePremiumsReceived.map(_.reversePremiumsReceived).getOrElse(false)
      ), // Todo: Recheck
      propertyRentalsIncome.deductingTax.map(x => DeductingTax(x.taxDeductedYesNo)),
      propertyRentalsIncome.calculatedFigureYourself.map(x => CalculatedFigureYourself(x.calculatedFigureYourself)),
      propertyRentalsIncome.yearLeaseAmount,
      propertyRentalsIncome.premiumsGrantLease.map(x => PremiumsGrantLease(x.premiumsGrantLeaseReceived)),
      propertyRentalsIncome.receivedGrantLeaseAmount
    )
    // Todo: None to be replaced by expenses
    SaveIncome(None, ukOtherPropertyIncome, incomeToSave)
  }
}
