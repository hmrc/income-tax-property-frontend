package navigation.foreign

import base.SpecBase
import controllers.foreign.routes
import models.{CheckMode, NormalMode, UserAnswers}
import navigation.ForeignPropertyNavigator
import pages.Page
import pages.foreign.{AddCountriesRentedPage, Country,SelectIncomeCountryPage}

import java.time.LocalDate

class ForeignNavigatorSpec extends SpecBase {

  private val navigator = new ForeignPropertyNavigator
  private val taxYear = LocalDate.now.getYear

  "ForeignPropertyNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page

        navigator.nextPage(
          UnknownPage,
          taxYear,
          NormalMode,
          UserAnswers("id"),
          UserAnswers("id")
        ) mustBe controllers.routes.IndexController.onPageLoad
      }

      "must go from SelectIncomeCountryPage to CountriesRentedPropertyController" in {
        navigator.nextPage(
          SelectIncomeCountryPage(0),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe routes.CountriesRentedPropertyController.onPageLoad(taxYear, NormalMode)
      }

      "must go from AddCountriesRentedPage to SelectIncomeCountryController if AddCountriesRentedPage is true" in {
        val userAnswersWithAddCountry = UserAnswers("test").set(AddCountriesRentedPage, true).get

        val updatedUserAnswers = userAnswersWithAddCountry.set(SelectIncomeCountryPage(0), Country("Spain", "ESP")).get
        navigator.nextPage(
          AddCountriesRentedPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          updatedUserAnswers
        ) mustBe routes.SelectIncomeCountryController.onPageLoad(taxYear, 1, NormalMode)
      }

      "must go from AddCountriesRentedPage to CountriesRentedPropertyController if AddCountriesRentedPage is false" in {
        val userAnswersWithoutAddCountry = UserAnswers("test").set(AddCountriesRentedPage, false).get

        navigator.nextPage(
          AddCountriesRentedPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          userAnswersWithoutAddCountry
        ) mustBe routes.CountriesRentedPropertyController.onPageLoad(taxYear, NormalMode)
      }
    }

    "in Check mode" - {

      "must go from SelectIncomeCountryPage to CountriesRentedPropertyController in CheckMode" in {
        navigator.nextPage(
          SelectIncomeCountryPage(0),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe routes.CountriesRentedPropertyController.onPageLoad(taxYear, CheckMode)
      }

      "must go from a page that doesn't exist in the route map to Index in CheckMode" in {
        case object UnknownPage extends Page

        navigator.nextPage(
          UnknownPage,
          taxYear,
          CheckMode,
          UserAnswers("id"),
          UserAnswers("id")
        ) mustBe controllers.routes.IndexController.onPageLoad
      }
    }
  }

}
