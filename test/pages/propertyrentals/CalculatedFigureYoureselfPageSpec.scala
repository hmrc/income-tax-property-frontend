package pages.propertyrentals

import base.SpecBase
import models.CalculatedFigureYourself
import pages.premiumlease.{PremiumsGrantLeasePage, RecievedGrantLeaseAmountPage, YearLeaseAmountPage}
import pages.CalculatedFigureYourselfPage

class CalculatedFigureYoureselfPageSpec extends SpecBase {

  "must remove the correct data when the answer is yes" in {

    val userData = emptyUserAnswers
                      .set(RecievedGrantLeaseAmountPage, BigDecimal(10.11)).success.value
                      .set(YearLeaseAmountPage, 10).success.value
                      .set(PremiumsGrantLeasePage, BigDecimal(10.12)).success.value

    val result = userData.set(CalculatedFigureYourselfPage, CalculatedFigureYourself(true, Some(10.13))).success.value

    result.get(CalculatedFigureYourselfPage)  must be(defined)
    result.get(RecievedGrantLeaseAmountPage)  must not be defined
    result.get(YearLeaseAmountPage)           must not be defined
    result.get(PremiumsGrantLeasePage)        must not be defined

  }

  "must keep that data value when the answer is yes" in {

    val userData = emptyUserAnswers
      .set(RecievedGrantLeaseAmountPage, BigDecimal(10.10)).get
      .set(YearLeaseAmountPage, 10).get
      .set(PremiumsGrantLeasePage, BigDecimal(10.10)).get

    val result = userData.set(CalculatedFigureYourselfPage, CalculatedFigureYourself(false, None)).success.value

    result.get(CalculatedFigureYourselfPage)    must be(defined)
    result.get(RecievedGrantLeaseAmountPage)    must be(defined)
    result.get(YearLeaseAmountPage)             must be(defined)
    result.get(PremiumsGrantLeasePage)          must be(defined)
  }

}
