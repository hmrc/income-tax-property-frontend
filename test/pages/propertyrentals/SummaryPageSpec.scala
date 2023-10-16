package pages.propertyrentals

import base.SpecBase
import models.UKPropertySelect
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.{SummaryPage, UKPropertyPage}

import java.time.LocalDate

class SummaryPageSpec extends SpecBase {

  "SummaryPageSpec createUkPropertyRows" should {
    val taxYear = LocalDate.now.getYear
    "return empty rows, given an empty user data" in {
      SummaryPage.createUkPropertyRows(Some(emptyUserAnswers), taxYear).length should be(0)
    }
    "createUkPropertyRows return only one row when user has selected PropertyRentals but not selected ClaimPropertyIncomeAllowancePage" in {
      val userAnswersWithPropertyRentals = emptyUserAnswers.set(
        UKPropertyPage,
        Set[UKPropertySelect](UKPropertySelect.PropertyRentals)
      ).success.value

      SummaryPage.createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear).length should be(1)

    }
    "createUkPropertyRows return all row when ClaimPropertyIncomeAllowancePage exist in the user data" in {
      val userAnswersWithPropertyRentals = emptyUserAnswers.set(
        UKPropertyPage,
        Set[UKPropertySelect](UKPropertySelect.PropertyRentals)
      ).success.value.set(ClaimPropertyIncomeAllowancePage, true).success.value

      SummaryPage.createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear).length should be(3)

    }

  }
}
