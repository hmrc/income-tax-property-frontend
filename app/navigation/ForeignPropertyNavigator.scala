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
import controllers.foreign.routes._
import controllers.routes.{IndexController, SummaryController}
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.foreign._
import pages.{ForeignSelectCountriesCompletePage, Page}
import play.api.mvc.Call

@Singleton
class ForeignPropertyNavigator {
  private val normalRoutes: Page => Int => UserAnswers => UserAnswers => Call = {
    case pages.foreign.TotalIncomePage =>
      taxYear => _ => _ => SelectIncomeCountryController.onPageLoad(taxYear, 0, NormalMode)
    case SelectIncomeCountryPage(_) =>
      taxYear => _ => _ => CountriesRentedPropertyController.onPageLoad(taxYear, NormalMode)
    case AddCountriesRentedPage =>
      taxYear =>
        _ =>
          userAnswers =>
            userAnswers.get(AddCountriesRentedPage) match {
              case Some(true) =>
                val nextIndex = userAnswers.get(IncomeSourceCountries).map(_.length).getOrElse(0)
                SelectIncomeCountryController.onPageLoad(taxYear, nextIndex, NormalMode)
              case Some(false) => ClaimPropertyIncomeAllowanceOrExpensesController.onPageLoad(taxYear, NormalMode)
              case _           => IndexController.onPageLoad
            }
    case ClaimPropertyIncomeAllowanceOrExpensesPage =>
      taxYear => _ => _ => ForeignCountriesCheckYourAnswersController.onPageLoad(taxYear)
    case ForeignSelectCountriesCompletePage => taxYear => _ => _ => SummaryController.show(taxYear)
    case _                                  => _ => _ => _ => controllers.routes.IndexController.onPageLoad
  }

  private val checkRouteMap: Page => Int => UserAnswers => UserAnswers => Call = {
    case pages.foreign.TotalIncomePage =>
      taxYear => _ => _ => ForeignCountriesCheckYourAnswersController.onPageLoad(taxYear)
    case SelectIncomeCountryPage(_) =>
      taxYear => _ => _ => ForeignCountriesCheckYourAnswersController.onPageLoad(taxYear)
    case AddCountriesRentedPage =>
      taxYear =>
        _ =>
          userAnswers =>
            userAnswers.get(AddCountriesRentedPage) match {
              case Some(true) =>
                val nextIndex = userAnswers.get(IncomeSourceCountries).map(_.length).getOrElse(0)
                SelectIncomeCountryController.onPageLoad(taxYear, nextIndex, CheckMode)
              case Some(false) =>
                ForeignCountriesCheckYourAnswersController.onPageLoad(taxYear)
              case _ => IndexController.onPageLoad
            }
    case ClaimPropertyIncomeAllowanceOrExpensesPage =>
      taxYear => _ => _ => ForeignCountriesCheckYourAnswersController.onPageLoad(taxYear)
    case _ => _ => _ => _ => controllers.routes.IndexController.onPageLoad
  }

  def nextPage(page: Page, taxYear: Int, mode: Mode, previousUserAnswers: UserAnswers, userAnswers: UserAnswers): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(taxYear)(previousUserAnswers)(userAnswers)
      case CheckMode =>
        checkRouteMap(page)(taxYear)(previousUserAnswers)(userAnswers)
    }

  private def addAnotherCountryNavigation(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(AddCountriesRentedPage) match {
      case Some(true) =>
        val nextIndex = userAnswers.get(IncomeSourceCountries).map(_.length).getOrElse(0)
        SelectIncomeCountryController.onPageLoad(taxYear, nextIndex, NormalMode)

      case _ => CountriesRentedPropertyController.onPageLoad(taxYear, NormalMode)
    }
}
