/*
 * Copyright 2023 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import controllers.propertyrentals.routes.{ClaimPropertyIncomeAllowanceController, PropertyRentalsCheckYourAnswersController}
import controllers.premiumlease.routes.{CalculatedFigureYourselfController, PremiumsGrantLeaseController, RecievedGrantLeaseAmountController, YearLeaseAmountController}
import play.api.mvc.Call
import controllers.routes
import pages._
import models._

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => Int => UserAnswers => Call = {
    case UKPropertyDetailsPage => taxYear => _ => routes.TotalIncomeController.onPageLoad(taxYear, NormalMode)
    case TotalIncomePage => taxYear => _ => routes.UKPropertySelectController.onPageLoad(taxYear, NormalMode)
    case UKPropertySelectPage => taxYear => _ => routes.SummaryController.show(taxYear)
    case UKPropertyPage => taxYear => _ => routes.CheckYourAnswersController.onPageLoad
    case premiumlease.LeasePremiumPaymentPage => taxYear => _ => CalculatedFigureYourselfController.onPageLoad(taxYear, NormalMode)
    case CalculatedFigureYourselfPage => taxYear => _ => RecievedGrantLeaseAmountController.onPageLoad(taxYear, NormalMode)
    case premiumlease.RecievedGrantLeaseAmountPage => taxYear => _ => YearLeaseAmountController.onPageLoad(taxYear, NormalMode)
    case premiumlease.YearLeaseAmountPage => taxYear => _ => PremiumsGrantLeaseController.onPageLoad(taxYear, NormalMode)
    case propertyrentals.ExpensesLessThan1000Page => taxYear => _ => ClaimPropertyIncomeAllowanceController.onPageLoad(taxYear, NormalMode)
    case propertyrentals.ClaimPropertyIncomeAllowancePage => taxYear => _ => PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
    case DeductingTaxPage => taxYear => _ => routes.DeductingTaxController.onPageLoad(taxYear, NormalMode)
    case IncomeFromPropertyRentalsPage => taxYear => _ => routes.IncomeFromPropertyRentalsController.onPageLoad(taxYear, NormalMode)
    case _ => _ => _ => routes.IndexController.onPageLoad
  }

  private val checkRouteMap: Page => Int => UserAnswers => Call = {
    case propertyrentals.ExpensesLessThan1000Page => taxYear => _ => PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
    case propertyrentals.ClaimPropertyIncomeAllowancePage => taxYear => _ => PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear)
    case _ => _ => _ => routes.CheckYourAnswersController.onPageLoad
  }

  def nextPage(page: Page, taxYear: Int, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(taxYear)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(taxYear)(userAnswers)
  }
}
