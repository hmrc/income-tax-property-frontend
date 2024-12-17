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
import pages.{Page, UkAndForeignPropertyRentalTypeUkPage}
import pages.ukandforeignproperty._
import play.api.mvc.Call

@Singleton
class UkAndForeignPropertyNavigator {
  private val normalRoutes: Page => Int => UserAnswers => UserAnswers => Call = {
    case TotalPropertyIncomePage =>
      taxYear => _ => userAnswers => totalIncomeNavigation(taxYear, userAnswers, NormalMode)
    case ReportIncomePage =>
      taxYear => _ => userAnswers => reportIncomeNavigation(taxYear, userAnswers, NormalMode)
    case UkAndForeignPropertyRentalTypeUkPage =>
      taxYear => _ => _ => ukAndForeignPropertyRentalTypeUkNavigation(taxYear, NormalMode)
    case ForeignCountriesRentedPage =>
      taxYear => _ => userAnswers => foreignCountriesRentedNavigation(taxYear, userAnswers)
    case SelectCountryPage(_) =>
      taxYear => _ => _ => controllers.ukandforeignproperty.routes.ForeignCountriesRentedController.onPageLoad(taxYear, NormalMode)
    case _ => _ => _ => _ => controllers.routes.IndexController.onPageLoad
  }

  private val checkRouteMap: Page => Int => UserAnswers => UserAnswers => Call =
    (_ => _ => _ => _ => controllers.routes.IndexController.onPageLoad) //TODO CYA page

  def nextPage(page: Page, taxYear: Int, mode: Mode, previousUserAnswers: UserAnswers, userAnswers: UserAnswers): Call = {
    mode match {
      case NormalMode =>
        normalRoutes(page)(taxYear)(previousUserAnswers)(userAnswers)
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

  private def ukAndForeignPropertyRentalTypeUkNavigation(taxYear: Int, mode: Mode): Call =
    routes.SelectCountryController.onPageLoad(taxYear, Index(0), mode)

  private def foreignCountriesRentedNavigation(taxYear: Int, userAnswers: UserAnswers): Call =
    userAnswers.get(ForeignCountriesRentedPage) match {
      case Some(true) =>
        //TODO TRUE map redirect to:
        //Redirect to previous page to add another country when branch merged
        //controllers.ukandforeignproperty.SelectCountryController.onPageLoad(taxYear:Int, index:Index, mode: Mode = NormalMode)
        //val nextIndex = userAnswers.get(IncomeSourceCountries).map(_.length).getOrElse(0)
        controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear: Int)
        //TODO FALSE redirect to:
        //Redirect to next page /property/uk-foreign-property/select-country/pia-yes-no
      case Some(false) =>  ???
    }
}
