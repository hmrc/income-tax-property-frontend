package pages.propertyrentals

import base.SpecBase
import models.UKPropertySelect
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.{SummaryPage, UKPropertyPage}
import viewmodels.summary.{TaskListItem, TaskListTag}

import java.time.LocalDate

class SummaryPageSpec extends SpecBase {

  "SummaryPageSpec createUkPropertyRows" - {
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
      SummaryPage.createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear).head should be(TaskListItem(
        "summary.about",
        controllers.propertyrentals.routes.PropertyRentalsStartController.onPageLoad(taxYear),
        TaskListTag.NotStarted,
        "about_link"))

    }
    "createUkPropertyRows return all row when ClaimPropertyIncomeAllowancePage exist in the user data" in {
      val userAnswersWithPropertyRentals = emptyUserAnswers.set(
        UKPropertyPage,
        Set[UKPropertySelect](UKPropertySelect.PropertyRentals)
      ).success.value.set(ClaimPropertyIncomeAllowancePage, true).success.value

      val res = Seq(TaskListItem(
        "summary.about",
        controllers.propertyrentals.routes.PropertyRentalsStartController.onPageLoad(taxYear),
        TaskListTag.NotStarted,
        "about_link"
      ), TaskListItem(
        "summary.income",
        controllers.propertyrentals.routes.PropertyIncomeStartController.onPageLoad(taxYear),
         TaskListTag.InProgress,
        "income_link"
      ), TaskListItem("summary.adjustments",
        controllers.routes.SummaryController.show(taxYear), //to change to adjustments page
        TaskListTag.NotStarted, ///update based on first page
        "adjustments_link"
      ))

      SummaryPage.createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear).length should be(3)
      SummaryPage.createUkPropertyRows(Some(userAnswersWithPropertyRentals), taxYear) should be(res)

    }

  }
}
