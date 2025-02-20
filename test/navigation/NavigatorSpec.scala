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

package navigation

import base.SpecBase
import controllers.adjustments.routes._
import controllers.allowances.routes._
import controllers.premiumlease.routes._
import controllers.propertyrentals.expenses.routes._
import controllers.routes
import models.TotalIncome.Under
import models._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import pages._
import pages.adjustments._
import pages.allowances._
import pages.enhancedstructuresbuildingallowance._
import pages.premiumlease._
import pages.propertyrentals.ClaimPropertyIncomeAllowancePage
import pages.propertyrentals.expenses._
import pages.propertyrentals.income._
import pages.rentalsandrentaroom.adjustments._
import pages.structurebuildingallowance._
import pages.ukrentaroom.adjustments.RaRUnusedResidentialCostsPage
import pages.ukrentaroom.allowances._
import pages.ukrentaroom.expenses._
import pages.ukrentaroom.{AboutSectionCompletePage, ClaimExpensesOrReliefPage, JointlyLetPage, TotalIncomeAmountPage}
import service.CYADiversionService

import java.time.LocalDate

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator(new CYADiversionService())
  private val taxYear = LocalDate.now.getYear
  private val index = 0

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          taxYear,
          NormalMode,
          UserAnswers("id"),
          UserAnswers("id")
        ) mustBe routes.IndexController.onPageLoad
      }

      "must go from UKPropertyDetailsPage to Total Income" in {
        navigator.nextPage(
          UKPropertyDetailsPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.about.routes.TotalIncomeController.onPageLoad(taxYear, NormalMode)
      }

      "must go from TotalIncomePage to the UK property select page" in {
        navigator.nextPage(
          TotalIncomePage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.about.routes.UKPropertySelectController.onPageLoad(taxYear, NormalMode)
      }
      "must go from TotalIncomePage to Report property income page if total income is under" in {
        navigator.nextPage(
          TotalIncomePage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test").set(TotalIncomePage, Under).get
        ) mustBe controllers.about.routes.ReportPropertyIncomeController.onPageLoad(taxYear, NormalMode)
      }

      "most go from UKPropertySelectPage to the summary page" in {
        navigator.nextPage(
          UKPropertySelectPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe routes.SummaryController.show(taxYear)
      }

      "must go from UKPropertyPage to Check Your Answers" in {
        navigator.nextPage(
          UKPropertyPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.about.routes.CheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from ReportPropertyIncomePage to Check Your Answers" in {
        navigator.nextPage(
          ReportPropertyIncomePage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.about.routes.CheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from ReportPropertyIncomePage to Property select page if user want to report income" in {
        navigator.nextPage(
          ReportPropertyIncomePage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test").set(ReportPropertyIncomePage, true).get
        ) mustBe controllers.about.routes.UKPropertySelectController.onPageLoad(taxYear, NormalMode)
      }

      "must go from PremiumForLeasePage to CalculateFigureYourselfPage when user selects yes" in {
        val testUserAnswer = UserAnswers("test").set(PremiumForLeasePage(Rentals), true).get

        navigator.nextPage(
          PremiumForLeasePage(Rentals),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          testUserAnswer
        ) mustBe controllers.premiumlease.routes.CalculatedFigureYourselfController
          .onPageLoad(taxYear, NormalMode, Rentals)
      }

      "must go from PremiumForLeasePage to reversePremiumReceivedPage when user selects no" in {
        val testUserAnswer = UserAnswers("test").set(PremiumForLeasePage(Rentals), false).get

        navigator.nextPage(
          PremiumForLeasePage(Rentals),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          testUserAnswer
        ) mustBe controllers.propertyrentals.income.routes.ReversePremiumsReceivedController
          .onPageLoad(taxYear, NormalMode, Rentals)
      }

      s"must go from BusinessPremisesRenovationAllowancesBalancingChargePage to RentalsAndRentARoomAdjustmentsCheckYourAnswersPage for " in {
        navigator.nextPage(
          BusinessPremisesRenovationAllowanceBalancingChargePage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe ResidentialFinanceCostController.onPageLoad(taxYear, NormalMode, RentalsRentARoom)
      }

      val scenarios = Table[PropertyType, String](
        ("property type", "type definition"),
        (RentalsRentARoom, "rentalsAndRaR"),
        (Rentals, "rentals")
      )

      forAll(scenarios) { (propertyType: PropertyType, propertyTypeDefinition: String) =>
        s"must go from PremiumForLeasePage to CalculateFigureYourselfPage when user selects yes for $propertyTypeDefinition" in {
          val testUserAnswer = UserAnswers("test").set(PremiumForLeasePage(propertyType), true).get

          navigator.nextPage(
            PremiumForLeasePage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            testUserAnswer
          ) mustBe controllers.premiumlease.routes.CalculatedFigureYourselfController
            .onPageLoad(taxYear, NormalMode, propertyType)
        }

        s"must go from PremiumForLeasePage to reversePremiumReceivedPage when user selects no for $propertyTypeDefinition" in {
          val testUserAnswer = UserAnswers("test").set(PremiumForLeasePage(propertyType), false).get

          navigator.nextPage(
            PremiumForLeasePage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            testUserAnswer
          ) mustBe controllers.propertyrentals.income.routes.ReversePremiumsReceivedController
            .onPageLoad(taxYear, NormalMode, propertyType)
        }

        s"must go from PremiumForLeasePage to CalculateFigureYourselfPage when user selects yes and the previous " +
          s"answer was no for for $propertyTypeDefinition" in {
            val previousUserAnswers = UserAnswers("test").set(PremiumForLeasePage(propertyType), false).get
            val userAnswers = UserAnswers("test").set(PremiumForLeasePage(propertyType), true).get
            navigator.nextPage(
              PremiumForLeasePage(propertyType),
              taxYear,
              CheckMode,
              previousUserAnswers,
              userAnswers
            ) mustBe CalculatedFigureYourselfController.onPageLoad(taxYear, CheckMode, propertyType)
          }

        s"must go from PremiumForLeasePage to CheckYourAnswers when user selects yes and the previous answer was yes for $propertyTypeDefinition" in {
          val previousUserAnswers = UserAnswers("test").set(PremiumForLeasePage(propertyType), true).get
          val userAnswers = UserAnswers("test").set(PremiumForLeasePage(propertyType), true).get
          navigator.nextPage(
            PremiumForLeasePage(propertyType),
            taxYear,
            CheckMode,
            previousUserAnswers,
            userAnswers
          ) mustBe
            (propertyType match {
              case Rentals =>
                controllers.propertyrentals.income.routes.PropertyIncomeCheckYourAnswersController
                  .onPageLoad(taxYear)
              case RentalsRentARoom =>
                controllers.rentalsandrentaroom.income.routes.RentalsAndRentARoomIncomeCheckYourAnswersController
                  .onPageLoad(taxYear)
            })
        }

        s"must go from PremiumForLeasePage to CheckYourAnswers when user selects no for $propertyTypeDefinition" in {
          val previousUserAnswers = UserAnswers("test").set(PremiumForLeasePage(propertyType), true).get
          val userAnswers = UserAnswers("test").set(PremiumForLeasePage(propertyType), false).get
          navigator.nextPage(
            PremiumForLeasePage(propertyType),
            taxYear,
            CheckMode,
            previousUserAnswers,
            userAnswers
          ) mustBe (propertyType match {
            case Rentals =>
              controllers.propertyrentals.income.routes.PropertyIncomeCheckYourAnswersController
                .onPageLoad(taxYear)
            case RentalsRentARoom =>
              controllers.rentalsandrentaroom.income.routes.RentalsAndRentARoomIncomeCheckYourAnswersController
                .onPageLoad(taxYear)
          })
        }

        s"must go from CalculatedFigureYourselfPage to RecievedGrantLeaseAmountPage when user selects no for $propertyTypeDefinition" in {
          val testUserAnswer =
            UserAnswers("test")
              .set(
                CalculatedFigureYourselfPage(propertyType),
                CalculatedFigureYourself(calculatedFigureYourself = false, None)
              )
              .get

          navigator.nextPage(
            CalculatedFigureYourselfPage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            testUserAnswer
          ) mustBe controllers.premiumlease.routes.ReceivedGrantLeaseAmountController
            .onPageLoad(taxYear, NormalMode, propertyType)
        }

        s"must go from CalculatedFigureYourselfPage to ReversePremiumReceivedPage when user selects yes for $propertyTypeDefinition" in {
          val testUserAnswer =
            UserAnswers("test")
              .set(
                CalculatedFigureYourselfPage(propertyType),
                CalculatedFigureYourself(calculatedFigureYourself = true, Some(100))
              )
              .get

          navigator.nextPage(
            CalculatedFigureYourselfPage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            testUserAnswer
          ) mustBe controllers.propertyrentals.income.routes.ReversePremiumsReceivedController
            .onPageLoad(taxYear, NormalMode, propertyType)
        }

        s"must go from RecievedGrantLeaseAmountPage to YearLeaseAmountPage for $propertyTypeDefinition" in {
          navigator.nextPage(
            premiumlease.ReceivedGrantLeaseAmountPage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe controllers.premiumlease.routes.YearLeaseAmountController
            .onPageLoad(taxYear, NormalMode, propertyType)
        }

        s"must go from YearLeaseAmountPage to PremiumsGrantLeasePage for $propertyTypeDefinition" in {
          navigator.nextPage(
            premiumlease.YearLeaseAmountPage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe controllers.premiumlease.routes.PremiumsGrantLeaseController
            .onPageLoad(taxYear, NormalMode, propertyType)
        }

        s"must go from reverse to OtherIncomeFromPropertyPage for $propertyTypeDefinition" in {
          navigator.nextPage(
            ReversePremiumsReceivedPage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe controllers.propertyrentals.income.routes.OtherPropertyRentalIncomeController
            .onPageLoad(taxYear, NormalMode, propertyType)
        }

        s"must go from ClaimPropertyIncomeAllowancePage to CheckYourAnswersPage for $propertyTypeDefinition" in {
          navigator.nextPage(
            ClaimPropertyIncomeAllowancePage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe
            (propertyType match {
              case Rentals =>
                controllers.propertyrentals.routes.PropertyRentalsCheckYourAnswersController
                  .onPageLoad(taxYear)
              case RentalsRentARoom =>
                controllers.rentalsandrentaroom.routes.RentalsAndRaRCheckYourAnswersController
                  .onPageLoad(taxYear)
            })
        }

        s"must go from DeductingTax to CheckYourAnswers for $propertyTypeDefinition" in {
          navigator.nextPage(
            DeductingTaxPage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe
            (propertyType match {
              case Rentals =>
                controllers.propertyrentals.income.routes.PropertyRentalIncomeController
                  .onPageLoad(taxYear, NormalMode, propertyType)
              case RentalsRentARoom =>
                controllers.premiumlease.routes.PremiumForLeaseController
                  .onPageLoad(taxYear, NormalMode, propertyType)
            })
        }

        s"must go from IncomeFromPropertyRentalsPage to PremiumForLeasePage for Rentals OR Claim Property Income for RentalsRAR for $propertyType" in {
          navigator.nextPage(
            PropertyRentalIncomePage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe
            (propertyType match {
              case Rentals =>
                controllers.premiumlease.routes.PremiumForLeaseController
                  .onPageLoad(taxYear, NormalMode, propertyType)
              case RentalsRentARoom =>
                controllers.propertyrentals.routes.ClaimPropertyIncomeAllowanceController
                  .onPageLoad(taxYear, NormalMode, propertyType)
            })
        }

        s"must go from IsNonUKLandlordPage to DeductingTaxPage when answer is yes for $propertyTypeDefinition" in {
          val userAnswers = UserAnswers("test").set(IsNonUKLandlordPage(propertyType), true).get
          navigator.nextPage(
            IsNonUKLandlordPage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            userAnswers
          ) mustBe controllers.propertyrentals.income.routes.DeductingTaxController
            .onPageLoad(taxYear, NormalMode, propertyType)
        }

        s"must go from IsNonUKLandlordPage to IncomeFromPropertyRentalsPage when answer is no for $propertyTypeDefinition" in {
          val userAnswers = UserAnswers("test").set(IsNonUKLandlordPage(propertyType), false).get
          val nextPage = navigator.nextPage(
            IsNonUKLandlordPage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            userAnswers
          )

          if (propertyType == Rentals) {
            nextPage mustBe controllers.propertyrentals.income.routes.PropertyRentalIncomeController
              .onPageLoad(taxYear, NormalMode, propertyType)
          } else if (propertyType == RentalsRentARoom) {
            nextPage mustBe controllers.premiumlease.routes.PremiumForLeaseController
              .onPageLoad(taxYear, NormalMode, propertyType)
          }
        }

        s"must go from OtherIncomeFromPropertyPage to PropertyIncomeCheckYourAnswersPage for $propertyTypeDefinition" in {
          navigator.nextPage(
            OtherIncomeFromPropertyPage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe (propertyType match {
            case Rentals =>
              controllers.propertyrentals.income.routes.PropertyIncomeCheckYourAnswersController
                .onPageLoad(taxYear)
            case RentalsRentARoom =>
              controllers.rentalsandrentaroom.income.routes.RentalsAndRentARoomIncomeCheckYourAnswersController
                .onPageLoad(taxYear)
          })
        }

        s"must go from ConsolidatedExpensesPage to RentsRatesAndInsurancePage when user selects no for $propertyTypeDefinition" in {
          val testUserAnswer =
            UserAnswers("test")
              .set(
                ConsolidatedExpensesPage(propertyType),
                ConsolidatedExpenses(consolidatedExpensesYesOrNo = false, None)
              )
              .get

          navigator.nextPage(
            ConsolidatedExpensesPage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            testUserAnswer
          ) mustBe controllers.propertyrentals.expenses.routes.RentsRatesAndInsuranceController
            .onPageLoad(taxYear, NormalMode, propertyType)
        }

        s"must go from ConsolidatedExpensesPage to ReversePremiumReceivedPage when user selects yes for $propertyTypeDefinition" in {
          val testUserAnswer =
            UserAnswers("test")
              .set(
                ConsolidatedExpensesPage(propertyType),
                ConsolidatedExpenses(consolidatedExpensesYesOrNo = true, Some(100))
              )
              .get

          navigator.nextPage(
            ConsolidatedExpensesPage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            testUserAnswer
          ) mustBe (propertyType match {
            case Rentals =>
              controllers.propertyrentals.expenses.routes.ExpensesCheckYourAnswersController.onPageLoad(taxYear)
            case RentalsRentARoom =>
              controllers.rentalsandrentaroom.expenses.routes.RentalsAndRaRExpensesCheckYourAnswersController
                .onPageLoad(taxYear)
          })

        }

        s"must go from RentsRatesAndInsurancePage to RepairsAndMaintenanceCostsPage for $propertyTypeDefinition" in {
          navigator.nextPage(
            RentsRatesAndInsurancePage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe RepairsAndMaintenanceCostsController.onPageLoad(taxYear, NormalMode, propertyType)
        }

        s"must go from RepairsAndMaintenanceCostsPage to LoanInterestPage for $propertyTypeDefinition" in {
          navigator.nextPage(
            RepairsAndMaintenanceCostsPage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe controllers.propertyrentals.expenses.routes.LoanInterestController
            .onPageLoad(taxYear, NormalMode, propertyType)
        }

        s"must go from LoanInterestPage to OtherProfessionalFeesPage for $propertyTypeDefinition" in {
          navigator.nextPage(
            LoanInterestPage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe controllers.propertyrentals.expenses.routes.OtherProfessionalFeesController
            .onPageLoad(taxYear, NormalMode, propertyType)
        }
        s"must go from OtherProfessionalFeesPage to CostsOfServicesProvidedPage for $propertyTypeDefinition" in {
          navigator.nextPage(
            OtherProfessionalFeesPage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe controllers.propertyrentals.expenses.routes.CostsOfServicesProvidedController
            .onPageLoad(taxYear, NormalMode, propertyType)
        }
        s"must go from CostsOfServicesProvidedPage to PropertyBusinessTravelCostsPage for $propertyTypeDefinition" in {
          navigator.nextPage(
            CostsOfServicesProvidedPage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe controllers.propertyrentals.expenses.routes.PropertyBusinessTravelCostsController
            .onPageLoad(taxYear, NormalMode, propertyType)
        }

        s"must go from PropertyBusinessTravelCostsPage to OtherAllowablePropertyExpensesPage for $propertyTypeDefinition" in {
          navigator.nextPage(
            PropertyBusinessTravelCostsPage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe controllers.propertyrentals.expenses.routes.OtherAllowablePropertyExpensesController
            .onPageLoad(taxYear, NormalMode, propertyType)
        }

        s"must go from CapitalAllowancesForACarPage to AnnualInvestmentAllowancePage for $propertyTypeDefinition" in {
          navigator.nextPage(
            CapitalAllowancesForACarPage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe (propertyType match {
            case Rentals =>
              controllers.allowances.routes.AllowancesCheckYourAnswersController
                .onPageLoad(taxYear)
            case RentalsRentARoom =>
              controllers.rentalsandrentaroom.allowances.routes.RentalsAndRentARoomAllowancesCheckYourAnswersController
                .onPageLoad(taxYear)
          })
        }

        s"must go from AnnualInvestmentAllowancePage to ZeroEmissionCarAllowancePage for $propertyTypeDefinition" in {
          navigator.nextPage(
            AnnualInvestmentAllowancePage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe ZeroEmissionCarAllowanceController.onPageLoad(taxYear, NormalMode, propertyType)
        }

        s"must go from ZeroEmissionCarAllowancePage to ZeroEmissionGoodsVehicleAllowancePage for $propertyTypeDefinition" in {
          navigator.nextPage(
            ZeroEmissionCarAllowancePage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe ZeroEmissionGoodsVehicleAllowanceController.onPageLoad(taxYear, NormalMode, propertyType)
        }
        s"must go from ZeroEmissionGoodsVehicleAllowancePage to BusinessPremisesRenovationPage for $propertyTypeDefinition" in {
          navigator.nextPage(
            ZeroEmissionGoodsVehicleAllowancePage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe BusinessPremisesRenovationController.onPageLoad(taxYear, NormalMode, propertyType)
        }

        s"must go from ReplacementOfDomesticGoodsPage to OtherCapitalAllowancePage for $propertyTypeDefinition" in {
          navigator.nextPage(
            ReplacementOfDomesticGoodsPage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe OtherCapitalAllowanceController.onPageLoad(taxYear, NormalMode, propertyType)
        }
        s"must go from OtherCapitalAllowancePage to AllowancesCheckYourAnswersPage for $propertyTypeDefinition" in {
          navigator.nextPage(
            OtherCapitalAllowancePage(propertyType),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe (propertyType match {
            case Rentals =>
              controllers.allowances.routes.AllowancesCheckYourAnswersController
                .onPageLoad(taxYear)
            case RentalsRentARoom =>
              controllers.rentalsandrentaroom.allowances.routes.RentalsAndRentARoomAllowancesCheckYourAnswersController
                .onPageLoad(taxYear)
          })
        }

      }
      s"must go from BusinessPremisesRenovationPage to ReplacementOfDomesticGoodsPage for Rentals" in {
        navigator.nextPage(
          BusinessPremisesRenovationPage(Rentals),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe ReplacementOfDomesticGoodsController.onPageLoad(taxYear, NormalMode, Rentals)
      }

      "must go from PrivateUseAdjustmentPage to BalancingChargePage" in {
        navigator.nextPage(
          PrivateUseAdjustmentPage(Rentals),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe BalancingChargeController.onPageLoad(taxYear, NormalMode, Rentals)
      }

      "must go from BalancingChargePage to PropertyIncomeAllowancePage if the user has selected Yes, claim property income allowance " +
        "on the Claiming Property Income Allowance' page" in {
          navigator.nextPage(
            BalancingChargePage(Rentals),
            taxYear,
            NormalMode,
            UserAnswers("test"),
            UserAnswers("test").set(ClaimPropertyIncomeAllowancePage(Rentals), true).get
          ) mustBe PropertyIncomeAllowanceController.onPageLoad(taxYear, NormalMode, Rentals)
        }

      "must go from BalancingChargePage to RenovationAllowanceBalancingCharge" in {
        navigator.nextPage(
          BalancingChargePage(Rentals),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test").set(ClaimPropertyIncomeAllowancePage(Rentals), false).get
        ) mustBe RenovationAllowanceBalancingChargeController.onPageLoad(taxYear, NormalMode, Rentals)
      }

      "must go from PropertyIncomeAllowancePage to RenovationAllowanceBalancingChargePage" in {
        navigator.nextPage(
          PropertyIncomeAllowancePage(Rentals),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe RenovationAllowanceBalancingChargeController.onPageLoad(taxYear, NormalMode, Rentals)
      }

      "must go from RenovationAllowanceBalancingChargePage to ResidentialFinanceCostPage" in {
        navigator.nextPage(
          RenovationAllowanceBalancingChargePage(Rentals),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe ResidentialFinanceCostController.onPageLoad(taxYear, NormalMode, Rentals)
      }

      "must go from ResidentialFinanceCostPage to UnusedResidentialFinanceCostPage" in {
        navigator.nextPage(
          ResidentialFinanceCostPage(Rentals),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe UnusedResidentialFinanceCostController.onPageLoad(taxYear, NormalMode, Rentals)
      }

      "must go from UnusedResidentialFinanceCostPage to UnusedLossesBroughtForwardPage" in {
        navigator.nextPage(
          UnusedResidentialFinanceCostPage(Rentals),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe UnusedLossesBroughtForwardController.onPageLoad(taxYear, NormalMode, Rentals)
      }
      "must go from UnusedLossesBroughtForwardPage to WhenYouReportedTheLossPage" in {
        val userAnswers = UserAnswers("test")
          .set(UnusedLossesBroughtForwardPage(Rentals), UnusedLossesBroughtForward(unusedLossesBroughtForwardYesOrNo = true, Some(BigDecimal(10))))
          .get
        navigator.nextPage(
          UnusedLossesBroughtForwardPage(Rentals),
          taxYear,
          NormalMode,
          userAnswers,
          userAnswers
        ) mustBe WhenYouReportedTheLossController.onPageLoad(taxYear, NormalMode, Rentals)
      }
      "must go from WhenYouReportedTheLossPage to AdjustmentsCheckYourAnswersPage" in {
        val userAnswers = UserAnswers("test")
          .set(WhenYouReportedTheLossPage(Rentals), WhenYouReportedTheLoss.y2018to2019)
          .get
        navigator.nextPage(
          WhenYouReportedTheLossPage(Rentals),
          taxYear,
          NormalMode,
          userAnswers,
          userAnswers
        ) mustBe AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
      }
      "must go from StructureBuildingQualifyingDatePage to StructureBuildingQualifyingAmountPage" in {
        navigator.sbaNextPage(
          StructureBuildingQualifyingDatePage(index, Rentals),
          taxYear,
          NormalMode,
          0,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.structuresbuildingallowance.routes.StructureBuildingQualifyingAmountController
          .onPageLoad(taxYear, NormalMode, 0, Rentals)
      }

      "must go from StructureBuildingQualifyingAmountPage to StructureBuildingAllowanceClaimPage" in {
        navigator.sbaNextPage(
          StructureBuildingQualifyingAmountPage(index, Rentals),
          taxYear,
          NormalMode,
          0,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.structuresbuildingallowance.routes.StructureBuildingAllowanceClaimController
          .onPageLoad(taxYear, NormalMode, 0, Rentals)
      }

      "must go from EsbaQualifyingDatePage to EsbaQualifyingAmountPage" in {
        navigator.esbaNextPage(
          EsbaQualifyingDatePage(index, Rentals),
          taxYear,
          NormalMode,
          index,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.enhancedstructuresbuildingallowance.routes.EsbaQualifyingAmountController
          .onPageLoad(taxYear, index, NormalMode, Rentals)
      }

      "must go from EsbaQualifyingAmountPage to EsbaClaimPage" in {
        navigator.esbaNextPage(
          EsbaQualifyingAmountPage(index, Rentals),
          taxYear,
          NormalMode,
          index,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.enhancedstructuresbuildingallowance.routes.EsbaClaimController
          .onPageLoad(taxYear, NormalMode, index, Rentals)
      }

      "must go from EsbaClaimPage to EsbaAddressPage" in {
        navigator.esbaNextPage(
          EsbaQualifyingAmountPage(index, Rentals),
          taxYear,
          NormalMode,
          index,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.enhancedstructuresbuildingallowance.routes.EsbaClaimController
          .onPageLoad(taxYear, NormalMode, index, Rentals)
      }

      "must go from RentsRatesAndInsuranceRRPage to RepairsAndMaintenanceCostsRRPage" in {
        navigator.nextPage(
          RentsRatesAndInsuranceRRPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.ukrentaroom.expenses.routes.RepairsAndMaintenanceCostsRRController
          .onPageLoad(taxYear, NormalMode)
      }

    }

    "in Check mode" - {

      "must go from TotalIncomePage to CheckYourAnswersPage if no change in user answers" in {
        navigator.nextPage(
          TotalIncomePage,
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.about.routes.CheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from TotalIncomePage to ReportPropertyIncomePage if income changes from between to under" in {
        val previousAnswers = UserAnswers("test").set(TotalIncomePage, TotalIncome.Between).get
        val userAnswers = UserAnswers("test").set(TotalIncomePage, TotalIncome.Under).get
        navigator.nextPage(
          TotalIncomePage,
          taxYear,
          CheckMode,
          previousAnswers,
          userAnswers
        ) mustBe controllers.about.routes.ReportPropertyIncomeController.onPageLoad(taxYear, NormalMode)
      }

      "must go from TotalIncomePage to Property Select page if income changes from under to over" in {
        val previousAnswers = UserAnswers("test").set(TotalIncomePage, TotalIncome.Under).get
        val userAnswers = UserAnswers("test").set(TotalIncomePage, TotalIncome.Over).get
        navigator.nextPage(
          TotalIncomePage,
          taxYear,
          CheckMode,
          previousAnswers,
          userAnswers
        ) mustBe controllers.about.routes.UKPropertySelectController.onPageLoad(taxYear, NormalMode)
      }

      "must go from TotalIncomePage to check your answers page if no change in answers" in {
        val previousAnswers = UserAnswers("test").set(TotalIncomePage, TotalIncome.Under).get
        val userAnswers = UserAnswers("test").set(TotalIncomePage, TotalIncome.Under).get
        navigator.nextPage(
          TotalIncomePage,
          taxYear,
          CheckMode,
          previousAnswers,
          userAnswers
        ) mustBe controllers.about.routes.CheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from ClaimPropertyIncomeAllowancePage to CheckYourAnswersPage" in {
        navigator.nextPage(
          ClaimPropertyIncomeAllowancePage(Rentals),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.propertyrentals.routes.PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
      }
      val scenarios = Table[PropertyType, String](
        ("property type", "type definition"),
        (Rentals, "rentals"),
        (RentalsRentARoom, "rentalsAndRaR")
      )

      forAll(scenarios) { (propertyType: PropertyType, propertyTypeDefinition: String) =>
        s"must go from IsNonUKLandlordPage to the CheckYourAnswers page when answer is no for $propertyTypeDefinition" in {
          val previousUserAnswers = UserAnswers("test").set(IsNonUKLandlordPage(propertyType), false).get
          val userAnswers = UserAnswers("test").set(IsNonUKLandlordPage(propertyType), false).get
          val nextPage = navigator.nextPage(
            IsNonUKLandlordPage(propertyType),
            taxYear,
            CheckMode,
            previousUserAnswers,
            userAnswers
          )
          if (propertyType == Rentals) {
            nextPage mustBe checkCYARouteForPropertyType(propertyType, taxYear)
          } else if (propertyType == RentalsRentARoom) {
            nextPage mustBe controllers.premiumlease.routes.PremiumForLeaseController
              .onPageLoad(taxYear, CheckMode, propertyType)
          }

        }

        s"must go from IsNonUKLandlordPage to the CheckYourAnswers page when answer is yes and the previous answer was yes for $propertyTypeDefinition" in {
          val previousUserAnswers = UserAnswers("test").set(IsNonUKLandlordPage(propertyType), true).get
          val userAnswers = UserAnswers("test").set(IsNonUKLandlordPage(propertyType), true).get
          val nextPage = navigator
            .nextPage(
              IsNonUKLandlordPage(propertyType),
              taxYear,
              CheckMode,
              previousUserAnswers,
              userAnswers
            )
          if (propertyType == Rentals) {
            nextPage mustBe checkCYARouteForPropertyType(propertyType, taxYear)
          } else if (propertyType == RentalsRentARoom) {
            nextPage mustBe controllers.premiumlease.routes.PremiumForLeaseController
              .onPageLoad(taxYear, CheckMode, propertyType)
          }
        }

        s"must go from IsNonUKLandlordPage to the DeductingTax page when answer is yes and the previous answer was no for $propertyTypeDefinition" in {
          val previousUserAnswers = UserAnswers("test").set(IsNonUKLandlordPage(propertyType), false).get
          val userAnswers = UserAnswers("test").set(IsNonUKLandlordPage(propertyType), true).get
          navigator.nextPage(
            IsNonUKLandlordPage(propertyType),
            taxYear,
            CheckMode,
            previousUserAnswers,
            userAnswers
          ) mustBe controllers.propertyrentals.income.routes.DeductingTaxController
            .onPageLoad(taxYear, CheckMode, propertyType)
        }

        s"must go from DeductingTax to CheckYourAnswers for $propertyTypeDefinition" in {
          navigator.nextPage(
            DeductingTaxPage(propertyType),
            taxYear,
            CheckMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe checkCYARouteForPropertyType(propertyType, taxYear)
        }

        s"must go from PremiumForLeasePage to CalculateFigureYourselfPage when user selects yes and the previous answer was no for $propertyTypeDefinition" in {
          val previousUserAnswers = UserAnswers("test").set(PremiumForLeasePage(propertyType), false).get
          val userAnswers = UserAnswers("test").set(PremiumForLeasePage(propertyType), true).get
          navigator.nextPage(
            PremiumForLeasePage(propertyType),
            taxYear,
            CheckMode,
            previousUserAnswers,
            userAnswers
          ) mustBe CalculatedFigureYourselfController.onPageLoad(taxYear, CheckMode, propertyType)
        }

        s"must go from PremiumForLeasePage to CheckYourAnswers when user selects yes and the previous answer was yes for $propertyTypeDefinition" in {
          val previousUserAnswers = UserAnswers("test").set(PremiumForLeasePage(propertyType), true).get
          val userAnswers = UserAnswers("test").set(PremiumForLeasePage(propertyType), true).get
          navigator.nextPage(
            PremiumForLeasePage(propertyType),
            taxYear,
            CheckMode,
            previousUserAnswers,
            userAnswers
          ) mustBe checkCYARouteForPropertyType(propertyType, taxYear)
        }

        s"must go from PremiumForLeasePage to CheckYourAnswers when user selects no for $propertyTypeDefinition" in {
          val previousUserAnswers = UserAnswers("test").set(PremiumForLeasePage(propertyType), true).get
          val userAnswers = UserAnswers("test").set(PremiumForLeasePage(propertyType), false).get
          navigator.nextPage(
            PremiumForLeasePage(propertyType),
            taxYear,
            CheckMode,
            previousUserAnswers,
            userAnswers
          ) mustBe checkCYARouteForPropertyType(propertyType, taxYear)
        }

        s"must go from CalculatedFigureYourselfPage to RecievedGrantLeaseAmount when user selects " +
          s"no and the previous answer was yes for $propertyTypeDefinition" in {
            val previousUserAnswers =
              UserAnswers("test")
                .set(
                  CalculatedFigureYourselfPage(propertyType),
                  CalculatedFigureYourself(calculatedFigureYourself = true, None)
                )
                .get
            val userAnswers =
              UserAnswers("test")
                .set(
                  CalculatedFigureYourselfPage(propertyType),
                  CalculatedFigureYourself(calculatedFigureYourself = false, None)
                )
                .get
            navigator.nextPage(
              CalculatedFigureYourselfPage(propertyType),
              taxYear,
              CheckMode,
              previousUserAnswers,
              userAnswers
            ) mustBe ReceivedGrantLeaseAmountController.onPageLoad(taxYear, CheckMode, propertyType)
          }

        s"must go from CalculatedFigureYourselfPage to CheckYourAnswers when user selects no and the previous answer was no for $propertyTypeDefinition" in {
          val previousUserAnswers =
            UserAnswers("test")
              .set(
                CalculatedFigureYourselfPage(propertyType),
                CalculatedFigureYourself(calculatedFigureYourself = false, None)
              )
              .get
          val userAnswers =
            UserAnswers("test")
              .set(
                CalculatedFigureYourselfPage(propertyType),
                CalculatedFigureYourself(calculatedFigureYourself = false, None)
              )
              .get
          navigator.nextPage(
            CalculatedFigureYourselfPage(propertyType),
            taxYear,
            CheckMode,
            previousUserAnswers,
            userAnswers
          ) mustBe checkCYARouteForPropertyType(propertyType, taxYear)
        }

        s"must go from CalculatedFigureYourselfPage to CheckYourAnswers when user selects for $propertyTypeDefinition" in {
          val previousUserAnswers =
            UserAnswers("test")
              .set(
                CalculatedFigureYourselfPage(propertyType),
                CalculatedFigureYourself(calculatedFigureYourself = false, None)
              )
              .get
          val userAnswers =
            UserAnswers("test")
              .set(
                CalculatedFigureYourselfPage(propertyType),
                CalculatedFigureYourself(calculatedFigureYourself = true, Some(100))
              )
              .get
          navigator.nextPage(
            CalculatedFigureYourselfPage(propertyType),
            taxYear,
            CheckMode,
            previousUserAnswers,
            userAnswers
          ) mustBe checkCYARouteForPropertyType(propertyType, taxYear)
        }

        s"must go from RecievedGrantLeaseAmountPage to YearLeaseAmount for $propertyTypeDefinition" in {
          navigator.nextPage(
            ReceivedGrantLeaseAmountPage(propertyType),
            taxYear,
            CheckMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe YearLeaseAmountController.onPageLoad(taxYear, CheckMode, propertyType)
        }

        s"must go from YearLeaseAmountPage to PremiumsGrantLease for $propertyTypeDefinition" in {
          navigator.nextPage(
            YearLeaseAmountPage(propertyType),
            taxYear,
            CheckMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe PremiumsGrantLeaseController.onPageLoad(taxYear, CheckMode, propertyType)
        }

        s"must go from PremiumsGrantLeasePage to CheckYourAnswers for $propertyTypeDefinition" in {
          navigator.nextPage(
            PremiumsGrantLeasePage(propertyType),
            taxYear,
            CheckMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe checkCYARouteForPropertyType(propertyType, taxYear)
        }

        s"must go from ReversePremiumsReceivedPage to CheckYourAnswers for $propertyTypeDefinition" in {
          navigator.nextPage(
            ReversePremiumsReceivedPage(propertyType),
            taxYear,
            CheckMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe checkCYARouteForPropertyType(propertyType, taxYear)
        }

        s"must go from OtherIncomeFromPropertyPage to CheckYourAnswers for $propertyTypeDefinition" in {
          navigator.nextPage(
            OtherIncomeFromPropertyPage(propertyType),
            taxYear,
            CheckMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe checkCYARouteForPropertyType(propertyType, taxYear)
        }

        s"must go from ClaimPropertyIncomeAllowancePage to CheckYourAnswersPage for $propertyTypeDefinition" in {
          navigator.nextPage(
            ClaimPropertyIncomeAllowancePage(propertyType),
            taxYear,
            CheckMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe
            (propertyType match {
              case Rentals =>
                controllers.propertyrentals.routes.PropertyRentalsCheckYourAnswersController
                  .onPageLoad(taxYear)
              case RentalsRentARoom =>
                controllers.rentalsandrentaroom.routes.RentalsAndRaRCheckYourAnswersController
                  .onPageLoad(taxYear)
            })
        }

        s"must go from CapitalAllowancesForACarPage to AllowancesCheckYourAnswersPage for $propertyTypeDefinition" in {
          navigator.nextPage(
            CapitalAllowancesForACarPage(propertyType),
            taxYear,
            CheckMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe
            (propertyType match {
              case Rentals =>
                AllowancesCheckYourAnswersController
                  .onPageLoad(taxYear)
              case RentalsRentARoom =>
                controllers.rentalsandrentaroom.allowances.routes.RentalsAndRentARoomAllowancesCheckYourAnswersController
                  .onPageLoad(taxYear)
            })
        }

        s"must go from AnnualInvestmentAllowancePage to AllowancesCheckYourAnswersPage for $propertyTypeDefinition" in {
          navigator.nextPage(
            AnnualInvestmentAllowancePage(propertyType),
            taxYear,
            CheckMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe
            (propertyType match {
              case Rentals =>
                AllowancesCheckYourAnswersController
                  .onPageLoad(taxYear)
              case RentalsRentARoom =>
                controllers.rentalsandrentaroom.allowances.routes.RentalsAndRentARoomAllowancesCheckYourAnswersController
                  .onPageLoad(taxYear)
            })
        }
        s"must go from ZeroEmissionCarAllowancePage to AllowancesCheckYourAnswersPage for $propertyTypeDefinition" in {
          navigator.nextPage(
            ZeroEmissionCarAllowancePage(propertyType),
            taxYear,
            CheckMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe (propertyType match {
            case Rentals =>
              AllowancesCheckYourAnswersController
                .onPageLoad(taxYear)
            case RentalsRentARoom =>
              controllers.rentalsandrentaroom.allowances.routes.RentalsAndRentARoomAllowancesCheckYourAnswersController
                .onPageLoad(taxYear)
          })
        }
        s"must go from ZeroEmissionGoodsVehicleAllowancePage to AllowancesCheckYourAnswersPage for $propertyTypeDefinition" in {
          navigator.nextPage(
            ZeroEmissionGoodsVehicleAllowancePage(propertyType),
            taxYear,
            CheckMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe (propertyType match {
            case Rentals =>
              AllowancesCheckYourAnswersController
                .onPageLoad(taxYear)
            case RentalsRentARoom =>
              controllers.rentalsandrentaroom.allowances.routes.RentalsAndRentARoomAllowancesCheckYourAnswersController
                .onPageLoad(taxYear)
          })
        }
        s"must go from ReplacementOfDomesticGoodsPage to AllowancesCheckYourAnswersPage for $propertyTypeDefinition" in {
          navigator.nextPage(
            ReplacementOfDomesticGoodsPage(propertyType),
            taxYear,
            CheckMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe (propertyType match {
            case Rentals =>
              AllowancesCheckYourAnswersController
                .onPageLoad(taxYear)
            case RentalsRentARoom =>
              controllers.rentalsandrentaroom.allowances.routes.RentalsAndRentARoomAllowancesCheckYourAnswersController
                .onPageLoad(taxYear)
          })
        }
        s"must go from OtherCapitalAllowancePage to AllowancesCheckYourAnswersPage for $propertyTypeDefinition" in {
          navigator.nextPage(
            OtherCapitalAllowancePage(propertyType),
            taxYear,
            CheckMode,
            UserAnswers("test"),
            UserAnswers("test")
          ) mustBe (propertyType match {
            case Rentals =>
              AllowancesCheckYourAnswersController
                .onPageLoad(taxYear)
            case RentalsRentARoom =>
              controllers.rentalsandrentaroom.allowances.routes.RentalsAndRentARoomAllowancesCheckYourAnswersController
                .onPageLoad(taxYear)
          })
        }

      }

      "must go from ClaimExpensesOrReliefPage to CheckYourAnswersController" in {
        navigator.nextPage(
          ClaimExpensesOrReliefPage(RentARoom),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.ukrentaroom.routes.CheckYourAnswersController
          .onPageLoad(taxYear)
      }

      "must go from AboutSectionCompletePage to AboutSectionCompleteController" in {
        navigator.nextPage(
          AboutSectionCompletePage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.ukrentaroom.routes.AboutSectionCompleteController
          .onPageLoad(taxYear)
      }

      "must go from OtherAllowablePropertyExpensesPage to ExpensesCheckYourAnswersController" in {
        navigator.nextPage(
          OtherAllowablePropertyExpensesPage(Rentals),
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.propertyrentals.expenses.routes.ExpensesCheckYourAnswersController
          .onPageLoad(taxYear)
      }

      "must go from RaRZeroEmissionCarAllowancePage to RaRReplacementsOfDomesticGoodsController" in {
        navigator.nextPage(
          RaRZeroEmissionCarAllowancePage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.ukrentaroom.allowances.routes.RaRReplacementsOfDomesticGoodsController
          .onPageLoad(taxYear, NormalMode)
      }

      "must go from LegalManagementOtherFeeRRPage to CostOfServicesProvidedRRController" in {
        navigator.nextPage(
          LegalManagementOtherFeeRRPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.ukrentaroom.expenses.routes.CostOfServicesProvidedRRController
          .onPageLoad(taxYear, NormalMode)
      }

      "must go from RentsRatesAndInsuranceRRPage to RepairsAndMaintenanceCostsRRController" in {
        navigator.nextPage(
          RentsRatesAndInsuranceRRPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.ukrentaroom.expenses.routes.RepairsAndMaintenanceCostsRRController
          .onPageLoad(taxYear, NormalMode)
      }

      "must go from RepairsAndMaintenanceCostsRRPage to LegalManagementOtherFeeRRController" in {
        navigator.nextPage(
          RepairsAndMaintenanceCostsRRPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.ukrentaroom.expenses.routes.LegalManagementOtherFeeRRController
          .onPageLoad(taxYear, NormalMode)
      }

      "must go from CostOfServicesProvidedRRPage to ResidentialPropertyFinanceCostsRRController" in {
        navigator.nextPage(
          CostOfServicesProvidedRRPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.ukrentaroom.expenses.routes.OtherPropertyExpensesRRController
          .onPageLoad(taxYear, NormalMode)
      }

      "must go from RaRElectricChargePointAllowanceForAnEVPage to RaRZeroEmissionCarAllowanceController" in {
        navigator.nextPage(
          RaRElectricChargePointAllowanceForAnEVPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.ukrentaroom.allowances.routes.RaRZeroEmissionCarAllowanceController
          .onPageLoad(taxYear, NormalMode)
      }

      "must go from RaRReplacementsOfDomesticGoodsPage to RaRAllowancesCheckYourAnswersController" in {
        navigator.nextPage(
          RaRReplacementsOfDomesticGoodsPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.ukrentaroom.allowances.routes.RaROtherCapitalAllowancesController
          .onPageLoad(taxYear, NormalMode)
      }

      "must go from RaROtherCapitalAllowancesPage to RaRAllowancesCheckYourAnswersController" in {
        navigator.nextPage(
          RaROtherCapitalAllowancesPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.ukrentaroom.allowances.routes.RaRAllowancesCheckYourAnswersController
          .onPageLoad(taxYear)
      }

      "must go from RaRCapitalAllowancesForACarPage to RaRAllowancesCheckYourAnswersController" in {
        navigator.nextPage(
          RaRCapitalAllowancesForACarPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.ukrentaroom.allowances.routes.RaRAllowancesCheckYourAnswersController
          .onPageLoad(taxYear)
      }

      "must go from ResidentialPropertyFinanceCostsRRPage to ExpensesCheckYourAnswersRRController" in {
        navigator.nextPage(
          OtherPropertyExpensesRRPage,
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.ukrentaroom.expenses.routes.ExpensesCheckYourAnswersRRController
          .onPageLoad(taxYear)
      }

      "must go from RaRUnusedResidentialCostsPage to RaRUnusedLossesBroughtForwardController" in {
        navigator.nextPage(
          RaRUnusedResidentialCostsPage,
          taxYear,
          NormalMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.ukrentaroom.adjustments.routes.RaRUnusedLossesBroughtForwardController
          .onPageLoad(taxYear, NormalMode)
      }

      "must go from OtherPropertyExpensesRRPage to ExpensesCheckYourAnswersRRController" in {
        navigator.nextPage(
          OtherPropertyExpensesRRPage,
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.ukrentaroom.expenses.routes.ExpensesCheckYourAnswersRRController.onPageLoad(taxYear)
      }

      "must go from RaRCapitalAllowancesForACarPage to RaRAllowancesCheckYourAnswers" in {
        navigator.nextPage(
          RaRCapitalAllowancesForACarPage,
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.ukrentaroom.allowances.routes.RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from RaRElectricChargePointAllowanceForAnEVPage to RaRAllowancesCheckYourAnswers" in {
        navigator.nextPage(
          RaRElectricChargePointAllowanceForAnEVPage,
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.ukrentaroom.allowances.routes.RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from RaROtherCapitalAllowancesPage to RaRAllowancesCheckYourAnswers" in {
        navigator.nextPage(
          RaROtherCapitalAllowancesPage,
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.ukrentaroom.allowances.routes.RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from RaRReplacementsOfDomesticGoodsPage to RaRAllowancesCheckYourAnswers" in {
        navigator.nextPage(
          RaRReplacementsOfDomesticGoodsPage,
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.ukrentaroom.allowances.routes.RaRAllowancesCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, taxYear, CheckMode, UserAnswers("test"), UserAnswers("id")) mustBe
          controllers.about.routes.CheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from PrivateUseAdjustmentPage to AdjustmentsCheckYourAnswersPage" in {
        navigator.nextPage(
          PrivateUseAdjustmentPage(Rentals),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from BalancingChargePage to AdjustmentsCheckYourAnswersPage if no change in user-answers" in {
        val userAnswers = UserAnswers("test")
          .set(BalancingChargePage(Rentals), BalancingCharge(balancingChargeYesNo = true, Some(BigDecimal(10))))
          .get
        navigator.nextPage(
          BalancingChargePage(Rentals),
          taxYear,
          CheckMode,
          userAnswers,
          userAnswers
        ) mustBe AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from BalancingChargePage to PropertyIncomeAllowancePage if change in user-answers" in {
        val previousUserAnswers = UserAnswers("test")
          .set(BalancingChargePage(Rentals), BalancingCharge(balancingChargeYesNo = true, Some(BigDecimal(10))))
          .get
        val userAnswers =
          UserAnswers("test")
            .set(ClaimPropertyIncomeAllowancePage(Rentals), true)
            .get
        val updateUserAnswers =
          userAnswers.set(BalancingChargePage(Rentals), BalancingCharge(balancingChargeYesNo = false, None)).get
        navigator.nextPage(
          BalancingChargePage(Rentals),
          taxYear,
          CheckMode,
          previousUserAnswers,
          updateUserAnswers
        ) mustBe PropertyIncomeAllowanceController.onPageLoad(taxYear, CheckMode, Rentals)
      }

      "must go from PropertyIncomeAllowancePage to AdjustmentsCheckYourAnswersPage" in {
        navigator.nextPage(
          PropertyIncomeAllowancePage(Rentals),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from RenovationAllowanceBalancingChargePage to AdjustmentsCheckYourAnswersPage" in {
        navigator.nextPage(
          RenovationAllowanceBalancingChargePage(Rentals),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from ResidentialFinanceCostPage to AdjustmentsCheckYourAnswersPage" in {
        navigator.nextPage(
          ResidentialFinanceCostPage(Rentals),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from UnusedResidentialFinanceCostPage to AdjustmentsCheckYourAnswersPage" in {
        navigator.nextPage(
          UnusedResidentialFinanceCostPage(Rentals),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe AdjustmentsCheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from StructureBuildingQualifyingDatePage to StructureBuildingQualifyingAmountPage" in {
        navigator.sbaNextPage(
          StructureBuildingQualifyingDatePage(index, Rentals),
          taxYear,
          CheckMode,
          0,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.structuresbuildingallowance.routes.SbaCheckYourAnswersController
          .onPageLoad(taxYear, 0, Rentals)
      }

      "must go from StructureBuildingQualifyingAmountPage to StructureBuildingAllowanceClaimPage" in {
        navigator.sbaNextPage(
          StructureBuildingQualifyingAmountPage(index, Rentals),
          taxYear,
          CheckMode,
          0,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.structuresbuildingallowance.routes.SbaCheckYourAnswersController
          .onPageLoad(taxYear, 0, Rentals)
      }

      "must go from EsbaQualifyingDatePage to EsbaQualifyingAmountPage" in {
        navigator.nextPage(
          EsbaQualifyingDatePage(index, Rentals),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.enhancedstructuresbuildingallowance.routes.EsbaQualifyingAmountController
          .onPageLoad(taxYear, index, CheckMode, Rentals)
      }

      "must go from EsbaQualifyingAmountPage to EsbaClaimPage" in {
        navigator.nextPage(
          EsbaQualifyingAmountPage(index, Rentals),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.enhancedstructuresbuildingallowance.routes.EsbaClaimController
          .onPageLoad(taxYear, CheckMode, index, Rentals)
      }

      "must go from EsbaClaimPage to EsbaAddressPage" in {
        navigator.nextPage(
          EsbaClaimPage(index, Rentals),
          taxYear,
          CheckMode,
          UserAnswers("test"),
          UserAnswers("test")
        ) mustBe controllers.enhancedstructuresbuildingallowance.routes.EsbaAddressController
          .onPageLoad(taxYear, CheckMode, index, Rentals)
      }

      "must go from JointlyLetIncomePage to TotalIncomePage if user answers were changed" in {
        val previousUserAnswers = UserAnswers("test")
          .set(JointlyLetPage(RentARoom), true)
          .get
        val userAnswers =
          UserAnswers("test")
            .set(JointlyLetPage(RentARoom), false)
            .get

        navigator.nextPage(
          JointlyLetPage(RentARoom),
          taxYear,
          CheckMode,
          previousUserAnswers,
          userAnswers
        ) mustBe controllers.ukrentaroom.routes.TotalIncomeAmountController.onPageLoad(taxYear, NormalMode, RentARoom)
      }

      "must go from JointlyLetIncomePage to CYA Page if user answers were not changed" in {
        val previousUserAnswers = UserAnswers("test")
          .set(JointlyLetPage(RentARoom), true)
          .get
        val userAnswers =
          UserAnswers("test")
            .set(JointlyLetPage(RentARoom), true)
            .get

        navigator.nextPage(
          JointlyLetPage(RentARoom),
          taxYear,
          CheckMode,
          previousUserAnswers,
          userAnswers
        ) mustBe controllers.ukrentaroom.routes.CheckYourAnswersController.onPageLoad(taxYear)
      }

      "must go from TotalIncomeAmountPage to ClaimExpensesOrRelief if user answers were changed" in {
        val previousUserAnswers = UserAnswers("test")
          .set(TotalIncomeAmountPage(RentARoom), BigDecimal(100))
          .get
        val userAnswers =
          UserAnswers("test")
            .set(TotalIncomeAmountPage(RentARoom), BigDecimal(200))
            .get

        navigator.nextPage(
          TotalIncomeAmountPage(RentARoom),
          taxYear,
          CheckMode,
          previousUserAnswers,
          userAnswers
        ) mustBe controllers.ukrentaroom.routes.ClaimExpensesOrReliefController
          .onPageLoad(taxYear, NormalMode, RentARoom)
      }

      "must go from TotalIncomeAmountPage to CYA Page if user answers were not changed" in {
        val previousUserAnswers = UserAnswers("test")
          .set(TotalIncomeAmountPage(RentARoom), BigDecimal(100))
          .get
        val userAnswers =
          UserAnswers("test")
            .set(TotalIncomeAmountPage(RentARoom), BigDecimal(100))
            .get

        navigator.nextPage(
          TotalIncomeAmountPage(RentARoom),
          taxYear,
          CheckMode,
          previousUserAnswers,
          userAnswers
        ) mustBe controllers.ukrentaroom.routes.CheckYourAnswersController.onPageLoad(taxYear)
      }

    }
  }

  private def checkCYARouteForPropertyType(propertyType: PropertyType, taxYear: Int) =
    propertyType match {
      case Rentals =>
        controllers.propertyrentals.income.routes.PropertyIncomeCheckYourAnswersController
          .onPageLoad(taxYear)
      case RentalsRentARoom =>
        controllers.rentalsandrentaroom.income.routes.RentalsAndRentARoomIncomeCheckYourAnswersController
          .onPageLoad(taxYear)
    }
}
