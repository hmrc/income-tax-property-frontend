package models

import pages.PageConstants.incomePath
import play.api.libs.json.{Format, JsPath, Json}
import queries.{Gettable, Settable}

case class ForeignPropertyIncome(
                                  foreignPropertyRentalIncome: BigDecimal,
                                  premiumsGrantLeaseYN: Boolean,
                                  calculatedPremiumLeaseTaxable: Option[PremiumCalculated],
                                  foreignReceivedGrantLeaseAmount: Option[BigDecimal],
                                  foreignYearLeaseAmount: Option[Int],
                                  foreignPremiumsGrantLease: Option[ForeignPremiumsGrantLease],
                                  foreignReversePremiumsReceived: ReversePremiumsReceived,
                                  foreignOtherIncomeFromProperty: BigDecimal
                                )

object ForeignPropertyIncome extends Gettable[ForeignPropertyIncome] with Settable[ForeignPropertyIncome]{
  implicit val format: Format[ForeignPropertyIncome] = Json.format[ForeignPropertyIncome]

  override def path: JsPath = JsPath \ incomePath(ForeignProperty)
}
