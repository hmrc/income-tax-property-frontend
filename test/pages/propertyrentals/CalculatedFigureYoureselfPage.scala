package pages.propertyrentals

import base.SpecBase
import models.{CalculatedFigureYourself, DeductingTax}
import pages.premiumlease.{PremiumsGrantLeasePage, RecievedGrantLeaseAmountPage, YearLeaseAmountPage}
import pages.{CalculatedFigureYourselfPage, DeductingTaxPage}

class CalculatedFigureYoureselfPage extends SpecBase {

  "must remove the correct data when the answer is yes" in {

    val userData = emptyUserAnswers
                      .set(RecievedGrantLeaseAmountPage, BigDecimal(10.10)).get
                      .set(YearLeaseAmountPage, 10).get
                      .set(PremiumsGrantLeasePage, BigDecimal(10.10)).get

    val result = userData.set(CalculatedFigureYourselfPage, CalculatedFigureYourself(true, Some(10.10))).success.value

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
