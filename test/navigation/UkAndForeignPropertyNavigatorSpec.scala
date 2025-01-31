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

package navigation

import base.SpecBase
import controllers.ukandforeignproperty.routes
import models.JourneyName.{reads, writes}
import models._
import pages.ukandforeignproperty._
import pages.foreign.Country
import pages.{Page, UkAndForeignPropertyRentalTypeUkPage}
import play.api.libs.json.Format.GenericFormat

import java.time.LocalDate

class UkAndForeignPropertyNavigatorSpec extends SpecBase {

  private val navigator = new UkAndForeignPropertyNavigator
  private val taxYear = LocalDate.now.getYear

  "UkAndForeignPropertyNavigator" - {

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


    "Uk and Foreign property" - {

      "Total Property Income" - {

        "in Normal mode" - {
          "must go from a TotalPropertyIncomePage to ReportIncomePage when the option selected is 'less then £1000'" in {

            val ua = UserAnswers("id")
              .set(TotalPropertyIncomePage, TotalPropertyIncome.LessThan)
              .success
              .value

            navigator.nextPage(
              TotalPropertyIncomePage,
              taxYear,
              NormalMode,
              UserAnswers("id"),
              ua
            ) mustBe routes.ReportIncomeController.onPageLoad(taxYear, NormalMode)
          }

          "must go from a TotalPropertyIncomePage to UkAndForeignPropertyRentalTypeUkPage when the option selected is '£1000 or more'" in {

            val ua = UserAnswers("id")
              .set(TotalPropertyIncomePage, TotalPropertyIncome.Maximum)
              .success
              .value

            navigator.nextPage(
              TotalPropertyIncomePage,
              taxYear,
              NormalMode,
              UserAnswers("id"),
              ua
            ) mustBe routes.UkAndForeignPropertyRentalTypeUkController.onPageLoad(taxYear = taxYear, mode = NormalMode)
          }
        }

        "in Check mode" - {

          "must go from a TotalPropertyIncomePage to ReportIncomePage when the option selected is 'less than £1000'" in {
            val previousAnswers = UserAnswers("id")
              .set(TotalPropertyIncomePage, TotalPropertyIncome.Maximum)
              .success
              .value
            val userAnswers = UserAnswers("id")
              .set(TotalPropertyIncomePage, TotalPropertyIncome.LessThan)
              .success
              .value

            navigator.nextPage(
              TotalPropertyIncomePage,
              taxYear,
              CheckMode,
              previousAnswers,
              userAnswers
            ) mustBe routes.ReportIncomeController.onPageLoad(taxYear, CheckMode)
          }

          "must go from a TotalPropertyIncomePage to UkAndForeignPropertyRentalTypeUkPage when the option selected is '£1000 or more'" in {
            val previousAnswers = UserAnswers("id")
              .set(TotalPropertyIncomePage, TotalPropertyIncome.LessThan)
              .success
              .value
            val userAnswers = UserAnswers("id")
              .set(TotalPropertyIncomePage, TotalPropertyIncome.Maximum)
              .success
              .value

            navigator.nextPage(
              TotalPropertyIncomePage,
              taxYear,
              CheckMode,
              previousAnswers,
              userAnswers
            ) mustBe routes.UkAndForeignPropertyRentalTypeUkController.onPageLoad(taxYear, NormalMode)
          }

          "must redirect to CYA when answer remains the same" in {
            val ua = UserAnswers("id")
              .set(TotalPropertyIncomePage, TotalPropertyIncome.LessThan)
              .success
              .value

            navigator.nextPage(
              TotalPropertyIncomePage,
              taxYear,
              CheckMode,
              UserAnswers("id"),
              ua
            ) mustBe routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
          }
        }
      }

      "Report Income" - {
        "in Normal mode" - {}
        "in Check mode" - {
          "must go from a ReportIncomePage to UkAndForeignPropertyRentalTypeUkPage when the option selected is 'less than £1000'" in {
            val previousAnswers = UserAnswers("id")
              .set(ReportIncomePage, ReportIncome.DoNoWantToReport)
              .success
              .value
            val userAnswers = UserAnswers("id")
              .set(ReportIncomePage, ReportIncome.WantToReport)
              .success
              .value

            navigator.nextPage(
              ReportIncomePage,
              taxYear,
              CheckMode,
              previousAnswers,
              userAnswers
            ) mustBe routes.UkAndForeignPropertyRentalTypeUkController.onPageLoad(taxYear, NormalMode)
          }
          "must go from a ReportIncomePage to Uk And foreign property CYA when answer remains the same" in {
            val ua = UserAnswers("id")
              .set(ReportIncomePage, ReportIncome.DoNoWantToReport)
              .success
              .value

            navigator.nextPage(
              ReportIncomePage,
              taxYear,
              CheckMode,
              UserAnswers("id"),
              ua
            ) mustBe routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
          }
        }
      }

      "Uk And Foreign Property Rental Type Uk" - {
        val testCountry: Country = Country("Greece", "GRC")
        val userAnswersWith0Country = emptyUserAnswers
        val userAnswersWith1Country = emptyUserAnswers.set(SelectCountryPage, List(testCountry)).success.value
        "in Normal mode" - {
          "must go to SelectCountryPage" in {
            navigator.nextPage(
              UkAndForeignPropertyRentalTypeUkPage,
              taxYear,
              NormalMode,
              UserAnswers("id"),
              userAnswersWith0Country
            ) mustBe routes.SelectCountryController.onPageLoad(taxYear, Index(1), NormalMode)
          }
          "must go to ForeignCountriesRented when at least one counter" in {
            navigator.nextPage(
              UkAndForeignPropertyRentalTypeUkPage,
              taxYear,
              NormalMode,
              UserAnswers("id"),
              userAnswersWith1Country
            ) mustBe routes.ForeignCountriesRentedController.onPageLoad(taxYear, NormalMode)
          }
        }
        "in Check mode" - {
          "must go to the CYA page" in {
            navigator.nextPage(
              UkAndForeignPropertyRentalTypeUkPage,
              taxYear,
              CheckMode,
              UserAnswers("id"),
              UserAnswers("id")
            ) mustBe routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
          }
        }

      }

      "Foreign Countries Rented" - {

        "in Normal mode" - {
          "must go from AddCountriesRentedPage to  if AddCountriesRentedPage is true" in {
            val userAnswersWithAddCountry = UserAnswers("id").set(ForeignCountriesRentedPage, true).get

            navigator.nextIndex(
              ForeignCountriesRentedPage,
              taxYear,
              NormalMode,
              UserAnswers("id"),
              userAnswersWithAddCountry,
              1
            ) mustBe
              routes.SelectCountryController.onPageLoad(taxYear, Index(2), NormalMode)
          }

          "must go from AddCountriesRentedPage to next page(ClaimExpensesOrRelief) if AddCountriesRentedPage is false " +
            "and selected property type includes Rent A Room" in {
            val userAnswersWithAddCountry = UserAnswers("id")
              .set(ForeignCountriesRentedPage, false)
              .flatMap(
                _.set(
                  UkAndForeignPropertyRentalTypeUkPage,
                  Set[UkAndForeignPropertyRentalTypeUk](UkAndForeignPropertyRentalTypeUk.RentARoom)
                )
              )
              .get

            navigator.nextIndex(
              ForeignCountriesRentedPage,
              taxYear,
              NormalMode,
              UserAnswers("id"),
              userAnswersWithAddCountry,
              1
            ) mustBe routes.UkAndForeignPropertyClaimExpensesOrReliefController.onPageLoad(taxYear, NormalMode)
          }

          "must go from AddCountriesRentedPage to next page(ClaimPropertyIncomeAllowanceOrExpenses) if AddCountriesRentedPage is false " +
            "and selected property type is Rentals" in {
            val userAnswersWithAddCountry = UserAnswers("id")
              .set(ForeignCountriesRentedPage, false)
              .flatMap(
                _.set(
                  UkAndForeignPropertyRentalTypeUkPage,
                  Set[UkAndForeignPropertyRentalTypeUk](UkAndForeignPropertyRentalTypeUk.PropertyRentals)
                )
              )
              .get

            navigator.nextIndex(
              ForeignCountriesRentedPage,
              taxYear,
              NormalMode,
              UserAnswers("id"),
              userAnswersWithAddCountry,
              1
            ) mustBe routes.UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesController.onPageLoad(taxYear, NormalMode)
          }
        }
        "in Check mode" - {}

      }

      "Claim expenses or rent a room relief" - {
        "in Normal mode" - {
          "must go to Claim property income allowance or expenses Page" - {
            "when the option selected is 'Rent a room relief'" in {
              navigator.nextPage(
                UkAndForeignPropertyClaimExpensesOrReliefPage,
                taxYear,
                NormalMode,
                emptyUserAnswers,
                emptyUserAnswers
                  .set(UkAndForeignPropertyClaimExpensesOrReliefPage, UkAndForeignPropertyClaimExpensesOrRelief(true))
                  .get
              ) mustBe routes.UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesController
                .onPageLoad(taxYear, NormalMode)
            }
            "when the option selected is 'Expenses'" in {
              navigator.nextPage(
                UkAndForeignPropertyClaimExpensesOrReliefPage,
                taxYear,
                NormalMode,
                emptyUserAnswers,
                emptyUserAnswers
                  .set(
                    UkAndForeignPropertyClaimExpensesOrReliefPage,
                    UkAndForeignPropertyClaimExpensesOrRelief(false)
                  )
                  .get
              ) mustBe routes.UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesController
                .onPageLoad(taxYear, NormalMode)
            }
          }
        }
        "in Check mode" - {}
      }

      "Claim property income allowance or expenses" - {
        "in Normal mode" - {
          "must go to 'Non-UK resident landlord'" - {

            "when the option selected is 'Use the property income allowance' " +
              "and 'rent a room relief' was selected " +
              "and UkAndForeignPropertyRentalTypeUkPage option includes Property Rentals" in {
              navigator.nextPage(
                UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
                taxYear,
                NormalMode,
                emptyUserAnswers,
                emptyUserAnswers
                  .set(
                    UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
                    UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(true)
                  )
                  .flatMap(
                    _.set(
                      UkAndForeignPropertyRentalTypeUkPage,
                      Set[UkAndForeignPropertyRentalTypeUk](UkAndForeignPropertyRentalTypeUk.PropertyRentals)
                    )
                  )
                  .flatMap(
                    _.set(
                      UkAndForeignPropertyClaimExpensesOrReliefPage,
                      UkAndForeignPropertyClaimExpensesOrRelief(true)
                    )
                  )
                  .get
              ) mustBe routes.NonResidentLandlordUKController.onPageLoad(taxYear, NormalMode)
            }

            "when the option selected is 'Use the property income allowance' " +
              "and 'claim expenses' was selected" in {
              navigator.nextPage(
                UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
                taxYear,
                NormalMode,
                emptyUserAnswers,
                emptyUserAnswers
                  .set(
                    UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
                    UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(true)
                  )
                  .flatMap(
                    _.set(
                      UkAndForeignPropertyClaimExpensesOrReliefPage,
                      UkAndForeignPropertyClaimExpensesOrRelief(false)
                    )
                  )
                  .flatMap(
                    _.set(
                      UkAndForeignPropertyRentalTypeUkPage,
                      Set[UkAndForeignPropertyRentalTypeUk](UkAndForeignPropertyRentalTypeUk.PropertyRentals)
                    )
                  )
                  .get
              ) mustBe routes.NonResidentLandlordUKController.onPageLoad(taxYear, NormalMode)
            }
          }

          "must go to 'How much income did you get from your foreign property rentals'" - {

            "when the option selected is 'Use the property income allowance' " +
              "and 'rent a room relief' was selected " +
              "and UkAndForeignPropertyRentalTypeUkPage option is Rent a Room" in {
              navigator.nextPage(
                UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
                taxYear,
                NormalMode,
                emptyUserAnswers,
                emptyUserAnswers
                  .set(
                    UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
                    UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(true)
                  )
                  .flatMap(
                    _.set(
                      UkAndForeignPropertyRentalTypeUkPage,
                      Set[UkAndForeignPropertyRentalTypeUk](UkAndForeignPropertyRentalTypeUk.RentARoom)
                    )
                  )
                  .flatMap(
                    _.set(
                      UkAndForeignPropertyClaimExpensesOrReliefPage,
                      UkAndForeignPropertyClaimExpensesOrRelief(true)
                    )
                  )
                  .get
              ) mustBe routes.ForeignRentalPropertyIncomeController.onPageLoad(taxYear, NormalMode)
            }
          }

          "must go to 'Check your answers'" - {
            "when the option selected is 'Declare Expenses' " in {
              val userAnswers = UserAnswers("id")
                .set(
                  UkAndForeignPropertyRentalTypeUkPage,
                  Set[UkAndForeignPropertyRentalTypeUk](UkAndForeignPropertyRentalTypeUk.PropertyRentals)
                )
                .flatMap(_.set(
                  UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
                  UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(false)
                ))
                .get

              navigator.nextPage(
                UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage,
                taxYear,
                NormalMode,
                emptyUserAnswers,
                userAnswers
              ) mustBe routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
            }
          }
        }
        "in Check mode" - {}
      }
    }

    "UK Property" - {

      "Non-UK resident landlord" - {
        "in Normal mode" - {
          "must go from NonResidentLandlordUKPage to 'Deducting tax from non-UK resident landlord' when the option 'true' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, true).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              NormalMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkAndForeignPropertyDeductingTaxFromNonUkResidentLandlordController
              .onPageLoad(taxYear, NormalMode)
          }

          "must go from NonResidentLandlordUKPage to 'How much income did you get from your foreign property rentals?' when the option 'false' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, false).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              NormalMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkRentalPropertyIncomeController.onPageLoad(taxYear, NormalMode)
          }
        }
        "in Check mode" - {
          "must go from NonResidentLandlordUKPage to 'Deducting tax from non-UK resident landlord' page in Check Mode when the option 'true' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, true).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              CheckMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkAndForeignPropertyDeductingTaxFromNonUkResidentLandlordController
              .onPageLoad(taxYear, CheckMode)
          }

          "must go from NonResidentLandlordUKPage to 'CYA page' when the option 'false' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, false).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              CheckMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
          }
        }
      }

      "Deducting tax from non-UK resident landlord" - {
        "in Normal mode" - {}
        "in Check mode" - {}
      }

      "How much income from UK property rentals" - {
        "in Normal mode" - {
          "must go from NonResidentLandlordUKPage to 'Deducting tax from non-UK resident landlord' when the option 'true' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, true).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              NormalMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkAndForeignPropertyDeductingTaxFromNonUkResidentLandlordController
              .onPageLoad(taxYear, NormalMode)
          }

          "must go from NonResidentLandlordUKPage to 'How much income did you get from your foreign property rentals?' when the option 'false' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, false).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              NormalMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkRentalPropertyIncomeController.onPageLoad(taxYear, NormalMode)
          }
        }
        "in Check mode" - {
          "must go from NonResidentLandlordUKPage to 'Deducting tax from non-UK resident landlord' page in Check Mode when the option 'true' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, true).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              CheckMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkAndForeignPropertyDeductingTaxFromNonUkResidentLandlordController
              .onPageLoad(taxYear, CheckMode)
          }

          "must go from NonResidentLandlordUKPage to 'CYA page' when the option 'false' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, false).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              CheckMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
          }
        }
      }

      "Report balancing charge for UK property rentals" - {
        "in Normal mode" - {
          "must go from NonResidentLandlordUKPage to 'Deducting tax from non-UK resident landlord' when the option 'true' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, true).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              NormalMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkAndForeignPropertyDeductingTaxFromNonUkResidentLandlordController
              .onPageLoad(taxYear, NormalMode)
          }

          "must go from NonResidentLandlordUKPage to 'How much income did you get from your foreign property rentals?' when the option 'false' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, false).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              NormalMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkRentalPropertyIncomeController.onPageLoad(taxYear, NormalMode)
          }
        }
        "in Check mode" - {
          "must go from NonResidentLandlordUKPage to 'Deducting tax from non-UK resident landlord' page in Check Mode when the option 'true' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, true).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              CheckMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkAndForeignPropertyDeductingTaxFromNonUkResidentLandlordController
              .onPageLoad(taxYear, CheckMode)
          }

          "must go from NonResidentLandlordUKPage to 'CYA page' when the option 'false' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, false).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              CheckMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
          }
        }
      }

      "Did you receive a premium for granting a lease for UK property" - {
        "in Normal mode" - {
          "must go from NonResidentLandlordUKPage to 'Deducting tax from non-UK resident landlord' when the option 'true' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, true).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              NormalMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkAndForeignPropertyDeductingTaxFromNonUkResidentLandlordController
              .onPageLoad(taxYear, NormalMode)
          }

          "must go from NonResidentLandlordUKPage to 'How much income did you get from your foreign property rentals?' when the option 'false' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, false).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              NormalMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkRentalPropertyIncomeController.onPageLoad(taxYear, NormalMode)
          }
        }
        "in Check mode" - {
          "must go from NonResidentLandlordUKPage to 'Deducting tax from non-UK resident landlord' page in Check Mode when the option 'true' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, true).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              CheckMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkAndForeignPropertyDeductingTaxFromNonUkResidentLandlordController
              .onPageLoad(taxYear, CheckMode)
          }

          "must go from NonResidentLandlordUKPage to 'CYA page' when the option 'false' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, false).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              CheckMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
          }
        }
      }

      "Have you calculated the premium for the grant of a lease taxable amount" - {
        "in Normal mode" - {
          "must go from NonResidentLandlordUKPage to 'Deducting tax from non-UK resident landlord' when the option 'true' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, true).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              NormalMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkAndForeignPropertyDeductingTaxFromNonUkResidentLandlordController
              .onPageLoad(taxYear, NormalMode)
          }

          "must go from NonResidentLandlordUKPage to 'How much income did you get from your foreign property rentals?' when the option 'false' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, false).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              NormalMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkRentalPropertyIncomeController.onPageLoad(taxYear, NormalMode)
          }
        }
        "in Check mode" - {
          "must go from NonResidentLandlordUKPage to 'Deducting tax from non-UK resident landlord' page in Check Mode when the option 'true' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, true).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              CheckMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkAndForeignPropertyDeductingTaxFromNonUkResidentLandlordController
              .onPageLoad(taxYear, CheckMode)
          }

          "must go from NonResidentLandlordUKPage to 'CYA page' when the option 'false' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, false).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              CheckMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
          }
        }
      }

      "How much did you receive for the grant of a lease" - {
        "in Normal mode" - {
          "must go from NonResidentLandlordUKPage to 'Deducting tax from non-UK resident landlord' when the option 'true' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, true).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              NormalMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkAndForeignPropertyDeductingTaxFromNonUkResidentLandlordController
              .onPageLoad(taxYear, NormalMode)
          }

          "must go from NonResidentLandlordUKPage to 'How much income did you get from your foreign property rentals?' when the option 'false' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, false).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              NormalMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkRentalPropertyIncomeController.onPageLoad(taxYear, NormalMode)
          }
        }
        "in Check mode" - {
          "must go from NonResidentLandlordUKPage to 'Deducting tax from non-UK resident landlord' page in Check Mode when the option 'true' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, true).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              CheckMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkAndForeignPropertyDeductingTaxFromNonUkResidentLandlordController
              .onPageLoad(taxYear, CheckMode)
          }

          "must go from NonResidentLandlordUKPage to 'CYA page' when the option 'false' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, false).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              CheckMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
          }
        }
      }

      "How many complete 12 month periods were in the term of the lease" - {
        "in Normal mode" - {
          "must go from NonResidentLandlordUKPage to 'Deducting tax from non-UK resident landlord' when the option 'true' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, true).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              NormalMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkAndForeignPropertyDeductingTaxFromNonUkResidentLandlordController
              .onPageLoad(taxYear, NormalMode)
          }

          "must go from NonResidentLandlordUKPage to 'How much income did you get from your foreign property rentals?' when the option 'false' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, false).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              NormalMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkRentalPropertyIncomeController.onPageLoad(taxYear, NormalMode)
          }
        }
        "in Check mode" - {
          "must go from NonResidentLandlordUKPage to 'Deducting tax from non-UK resident landlord' page in Check Mode when the option 'true' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, true).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              CheckMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkAndForeignPropertyDeductingTaxFromNonUkResidentLandlordController
              .onPageLoad(taxYear, CheckMode)
          }

          "must go from NonResidentLandlordUKPage to 'CYA page' when the option 'false' is selected" in {
            val nonResidentLandlordUKPage = UserAnswers("id").set(UkNonUkResidentLandlordPage, false).get

            navigator.nextPage(
              UkNonUkResidentLandlordPage,
              taxYear,
              CheckMode,
              UserAnswers("id"),
              nonResidentLandlordUKPage
            ) mustBe routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
          }
        }
      }

      "Premiums for the grant of a lease" - {
        "in Normal mode" - {}
        "in Check mode" - {}
      }

      "UK reverse premiums received" - {
        "in Normal mode" - {}
        "in Check mode" - {}
      }

      "Other income from UK property" - {
        "in Normal mode" - {}
        "in Check mode" - {}
      }

      "UK reverse premiums received" - {
        "in Normal mode" - {}
        "in Check mode" - {}
      }
    }

    "Foreign Property" - {
      "How much income from foreign property rentals" - {
        "in Normal mode" - {}
        "in Check mode" - {}
      }

      "Report balancing charge for foreign property rentals" - {
        "in Normal mode" - {}
        "in Check mode" - {}
      }

      "Did you receive a premium for granting a lease for Foreign property" - {
        "in Normal mode" - {}
        "in Check mode" - {}
      }

      "Have you calculated the premium for the grant of a lease taxable amount" - {
        "in Normal mode" - {}
        "in Check mode" - {}
      }

      "How much did you receive for the grant of a lease" - {
        "in Normal mode" - {}
        "in Check mode" - {}
      }

      "How many complete 12 month periods were in the term of the lease" - {
        "in Normal mode" - {}
        "in Check mode" - {}
      }

      "Premiums for the grant of a lease" - {
        "in Normal mode" - {}
        "in Check mode" - {}
      }

      "Other income from foreign property" - {
        "in Normal mode" - {}
        "in Check mode" - {}
      }

      "Your Property Income Allowance claim" - {
        "in Normal mode" - {}
        "in Check mode" - {}
      }
    }
  }
}
