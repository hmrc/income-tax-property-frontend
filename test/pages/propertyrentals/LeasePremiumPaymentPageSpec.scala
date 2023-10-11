
package pages.propertyrentals

import base.SpecBase
import models.CalculatedFigureYourself
import pages.CalculatedFigureYourselfPage
import pages.premiumlease.{LeasePremiumPaymentPage, PremiumsGrantLeasePage, RecievedGrantLeaseAmountPage, YearLeaseAmountPage}

class LeasePremiumPaymentPageSpec extends SpecBase {

  "must remove the correct data when the answer is no" in {

    val userData = emptyUserAnswers
      .set(RecievedGrantLeaseAmountPage, BigDecimal(10.11)).get
      .set(YearLeaseAmountPage, 10).get
      .set(PremiumsGrantLeasePage, BigDecimal(10.12)).get
      .set(CalculatedFigureYourselfPage, CalculatedFigureYourself(false, None)).get

    val result = userData.set(LeasePremiumPaymentPage, false).success.value

//    result.get(LeasePremiumPaymentPage) must be(defined)
//    result.get(CalculatedFigureYourselfPage) must not be defined
    result.get(RecievedGrantLeaseAmountPage) must not be defined
//    result.get(YearLeaseAmountPage) must not be defined
//    result.get(PremiumsGrantLeasePage) must not be defined

  }

  "must keep that data value when the answer is yes" in {

    val userData = emptyUserAnswers.set(RecievedGrantLeaseAmountPage, BigDecimal(10.11)).get
      .set(YearLeaseAmountPage, 10).get
      .set(PremiumsGrantLeasePage, BigDecimal(10.12)).get
      .set(CalculatedFigureYourselfPage, CalculatedFigureYourself(false, None)).get

    val result = userData.set(LeasePremiumPaymentPage, true).success.value

    result.get(LeasePremiumPaymentPage) must be(defined)
    result.get(CalculatedFigureYourselfPage) must be(defined)
    result.get(RecievedGrantLeaseAmountPage) must be(defined)
    result.get(YearLeaseAmountPage) must be(defined)
    result.get(PremiumsGrantLeasePage) must be(defined)
  }
}
