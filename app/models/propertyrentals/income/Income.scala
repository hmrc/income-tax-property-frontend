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

import play.api.libs.json.{Json, OFormat}

final case class Income(
  isNonUKLandlord: Boolean,
  propertyRentalIncome: BigDecimal,
  premiumForLease: Boolean,
  reversePremiumsReceived: ReversePremiumsReceived,
  taxDeductedYesNo: Option[DeductingTax],
  calculatedFigureYourself: Option[CalculatedFigureYourself],
  yearLeaseAmount: Option[BigDecimal],
  premiumsGrantLeaseYesNo: Option[PremiumsGrantLease],
  receivedGrantLeaseAmount: Option[BigDecimal]
)

object Income {
  implicit val format: OFormat[Income] = Json.format[Income]
}

final case class PremiumsGrantLease(yesOrNo: Boolean)

object PremiumsGrantLease {
  implicit val format: OFormat[PremiumsGrantLease] = Json.format[PremiumsGrantLease]
}
final case class ReversePremiumsReceived(reversePremiumsReceived: Boolean)

object ReversePremiumsReceived {
  implicit val format: OFormat[ReversePremiumsReceived] = Json.format[ReversePremiumsReceived]
}

final case class CalculatedFigureYourself(calculatedFigureYourself: Boolean)

object CalculatedFigureYourself {
  implicit val format: OFormat[CalculatedFigureYourself] = Json.format[CalculatedFigureYourself]
}

final case class DeductingTax(taxDeductedYesNo: Boolean)

object DeductingTax {
  implicit val format: OFormat[DeductingTax] = Json.format[DeductingTax]
}
