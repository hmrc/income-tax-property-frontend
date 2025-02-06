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

package models.ukAndForeign

import models._
import pages.PageConstants
import pages.foreign.Country
import play.api.libs.json.{Format, JsPath, Json}
import queries.Gettable

final case class UkAndForeignAbout(aboutUkAndForeign: AboutUkAndForeign, aboutUk: Option[AboutUk], aboutForeign: Option[AboutForeign])

case object UkAndForeignAbout {
  implicit val formats: Format[UkAndForeignAbout] = Json.format[UkAndForeignAbout]
}

final case class AboutUkAndForeign(
  totalPropertyIncome: TotalPropertyIncome,
  reportIncome: Option[ReportIncome],
  ukPropertyRentalType: Option[Seq[UkAndForeignPropertyRentalTypeUk]],
  countries: Option[List[Country]],
  claimExpensesOrRelief: Option[UkAndForeignPropertyClaimExpensesOrRelief],
  claimPropertyIncomeAllowanceOrExpenses: Option[UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses]
)
case object AboutUkAndForeign extends Gettable[AboutUkAndForeign] {
  implicit val formats: Format[AboutUkAndForeign] = Json.format[AboutUkAndForeign]

  override def path: JsPath = JsPath \ toString

  override def toString: String = PageConstants.aboutPath(UKAndForeignProperty)
}

final case class AboutUk(
  nonUkResidentLandlord: Option[Boolean],
  deductingTaxFromNonUkResidentLandlord: Option[DeductingTaxFromNonUkResidentLandlord],
  ukRentalPropertyIncomeAmount: Option[BigDecimal],
  balancingCharge: Option[BalancingCharge],
  premiumForLease: Option[Boolean],
  premiumGrantLeaseTax: Option[UkAndForeignPropertyPremiumGrantLeaseTax],
  amountReceivedForGrantOfLeasePage: Option[UkAndForeignPropertyAmountReceivedForGrantOfLease],
  yearLeaseAmount: Option[Int],
  premiumsGrantLease: Option[UKPremiumsGrantLease],
  reversePremiumsReceived: Option[ReversePremiumsReceived],
  otherIncomeFromProperty: Option[BigDecimal]
)
case object AboutUk extends Gettable[AboutUk] {
  implicit val formats: Format[AboutUk] = Json.format[AboutUk]

  override def path: JsPath = JsPath \ toString

  override def toString: String = PageConstants.aboutPath(UKAndForeignProperty)
}
final case class AboutForeign(
  foreignRentalPropertyIncomeAmount: Option[BigDecimal],
  foreignBalancingCharge: Option[BalancingCharge],
  foreignPremiumsForTheGrantOfALease: Option[Boolean],
  foreignCalculatedPremiumGrantLeaseTaxable: Option[PremiumCalculated],
  foreignLeaseGrantReceivedAmount: Option[BigDecimal],
  foreignYearLeaseAmount: Option[Int],
  foreignPremiumsGrantLease: Option[UkAndForeignPropertyForeignPremiumsGrantLease],
  foreignOtherIncomeFromProperty: Option[BigDecimal],
  propertyIncomeAllowanceClaim: Option[BigDecimal]
)
case object AboutForeign extends Gettable[AboutForeign] {
  implicit val formats: Format[AboutForeign] = Json.format[AboutForeign]

  override def path: JsPath = JsPath \ toString

  override def toString: String = PageConstants.aboutPath(UKAndForeignProperty)
}
