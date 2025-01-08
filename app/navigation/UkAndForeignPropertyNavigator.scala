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

import com.google.inject.Singleton
import controllers.ukandforeignproperty.routes
import models._
import pages.ukandforeignproperty.{UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage, ForeignCountriesRentedPage, ReportIncomePage, SelectCountryPage, TotalPropertyIncomePage, UkAndForeignPropertyClaimExpensesOrReliefPage}
import pages.ukandforeignproperty._
import pages.{Page, UkAndForeignPropertyRentalTypeUkPage}
import play.api.mvc.Call

@Singleton
class UkAndForeignPropertyNavigator {
  private val normalRoutes: Page => Int => UserAnswers => UserAnswers => Call = {
    case TotalPropertyIncomePage =>
      taxYear => _ => userAnswers => totalIncomeNavigation(taxYear, userAnswers, NormalMode)
    case ReportIncomePage =>
      taxYear => _ => userAnswers => reportIncomeNavigation(taxYear, userAnswers, NormalMode)
    case UkAndForeignPropertyRentalTypeUkPage =>
      taxYear => _ => _ => routes.SelectCountryController.onPageLoad(taxYear, Index(1), NormalMode)
    case SelectCountryPage =>
      taxYear => _ => _ => routes.ForeignCountriesRentedController.onPageLoad(taxYear, NormalMode)
    case NonResidentLandlordUKPage =>
      taxYear => _ => userAnswers => nonResidentLandlordNavigation(taxYear, userAnswers, NormalMode)
    case _ => _ => _ => _ => controllers.routes.IndexController.onPageLoad
  }

  private val indexableRoutes: Page => Int => UserAnswers => UserAnswers => Int => Call = {
    case ForeignCountriesRentedPage =>
      taxYear => _ => userAnswers => index => foreignCountriesRentedNavigation(taxYear, userAnswers, index)
    case UkAndForeignPropertyClaimExpensesOrReliefPage =>
      taxYear => _ => userAnswers => index => claimExpensesOrReliefPageNavigation(taxYear, userAnswers, index)
    case UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage =>
      taxYear => _ => userAnswers => index => claimPropertyIncomeAllowanceOrExpensesPageNavigation(taxYear, userAnswers, index)
    case _ =>
      _ => _ => _ => _ => controllers.routes.IndexController.onPageLoad

  }

  private val checkRouteMap: Page => Int => UserAnswers => UserAnswers => Call =
    _ => _ => _ => _ => controllers.routes.IndexController.onPageLoad //TODO CYA page

  def nextPage(page: Page, taxYear: Int, mode: Mode, previousUserAnswers: UserAnswers, userAnswers: UserAnswers): Call = {
    mode match {
      case NormalMode =>
        normalRoutes(page)(taxYear)(previousUserAnswers)(userAnswers)
      case CheckMode =>
        checkRouteMap(page)(taxYear)(previousUserAnswers)(userAnswers)
    }
  }

  def nextIndex(page: Page, taxYear: Int, mode: Mode, previousUserAnswers: UserAnswers, userAnswers: UserAnswers, index: Int): Call = {
    mode match {
      case NormalMode =>
        indexableRoutes(page)(taxYear)(previousUserAnswers)(userAnswers)(index)
      case CheckMode =>
        checkRouteMap(page)(taxYear)(previousUserAnswers)(userAnswers)
    }
  }

  private def totalIncomeNavigation(taxYear: Int, userAnswers: UserAnswers, mode: Mode): Call = {
    userAnswers.get(TotalPropertyIncomePage) match {
      case Some(TotalPropertyIncome.Maximum) => routes.UkAndForeignPropertyRentalTypeUkController.onPageLoad(taxYear, mode)
      case Some(TotalPropertyIncome.LessThan) => routes.ReportIncomeController.onPageLoad(taxYear, mode)
    }
  }

  private def reportIncomeNavigation(taxYear: Int, userAnswers: UserAnswers, mode: Mode): Call = {
    userAnswers.get(ReportIncomePage) match {
      case Some(ReportIncome.WantToReport) => routes.UkAndForeignPropertyRentalTypeUkController.onPageLoad(taxYear, mode)
      case Some(ReportIncome.DoNoWantToReport) => controllers.routes.IndexController.onPageLoad // TODO: route to CYA page when created
    }
  }

  private def foreignCountriesRentedNavigation(taxYear: Int, userAnswers: UserAnswers, index: Int): Call = {

    ( userAnswers.get(ForeignCountriesRentedPage), userAnswers.get(UkAndForeignPropertyRentalTypeUkPage).map(_.toSeq)) match {
      case (Some(true),_) =>
        routes.SelectCountryController.onPageLoad(taxYear, Index(index + 1), NormalMode)
      case (Some(false), None) =>
        throw new RuntimeException("No rental type selected")  // should never happen
      case (Some(false), Some(Seq(UkAndForeignPropertyRentalTypeUk.PropertyRentals))) =>
        routes.UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesController.onPageLoad(taxYear, NormalMode)
      case _ =>
        routes.UkAndForeignPropertyClaimExpensesOrReliefController.onPageLoad(taxYear, NormalMode)
    }
  }

  private def claimExpensesOrReliefPageNavigation(taxYear: Int, userAnswers: UserAnswers, index: Int): Call =
    userAnswers.get(UkAndForeignPropertyClaimExpensesOrReliefPage) match {
      case Some(UkAndForeignPropertyClaimExpensesOrRelief(_)) =>
        routes.UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesController.onPageLoad(taxYear, NormalMode)
    }

  private def claimPropertyIncomeAllowanceOrExpensesPageNavigation(taxYear: Int, userAnswers: UserAnswers, index: Int): Call = {

    (userAnswers.get(UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage), userAnswers.get(UkAndForeignPropertyRentalTypeUkPage).map(_.toSeq)) match {
      case (Some(UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(true)), Some(Seq(UkAndForeignPropertyRentalTypeUk.RentARoom))) =>
        //TODO replace 'How much income did you get from your foreign property rentals' Controller
        ???
      case (Some(UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(true)), Some(_)) =>
        routes.NonResidentLandlordUKController.onPageLoad(taxYear, NormalMode)
      case (Some(UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(false)), _) =>
        //TODO replace 'How much income did you get from your foreign property rentals' Controller
        ???
      case _ =>
        // Should not happen
        ???
    }
  }


  //TODO add the next pages to navigate when they are available
  private def nonResidentLandlordNavigation(taxYear: Int, userAnswers: UserAnswers, mode: Mode): Call =
    (userAnswers.get(NonResidentLandlordUKPage), mode) match {
      case (Some(true), NormalMode) => ???
      case (Some(false), NormalMode) => ???
      case (_, CheckMode) => ??? //TODO CYA page
      case _ => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

}
