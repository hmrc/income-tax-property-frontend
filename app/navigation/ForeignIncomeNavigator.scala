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
import controllers.foreignincome.dividends.routes._
import controllers.routes.SummaryController
import models._
import pages.Page
import pages.foreign.Country
import pages.foreignincome._
import pages.foreignincome.dividends._
import play.api.mvc.Call
import service.ForeignIncomeCYADiversionService

import javax.inject.Inject

@Singleton
class ForeignIncomeNavigator @Inject() (foreignIncomeCYADiversionService: ForeignIncomeCYADiversionService) {
  private val normalRoutes: Page => Int => UserAnswers => UserAnswers => Call = {
    case CountryReceiveDividendIncomePage(index) =>
      taxYear => _ => userAnswers => dividendIncomeCountryNavigation(taxYear, index, userAnswers)
    case IncomeBeforeForeignTaxDeductedPage(countryCode) =>
      taxYear => _ => _ => ForeignTaxDeductedFromDividendIncomeController.onPageLoad(taxYear, countryCode, NormalMode)
    case ForeignTaxDeductedFromDividendIncomePage(countryCode) =>
      taxYear => previousAnswers => answers => foreignTaxDeductedNavigation(taxYear, countryCode, previousAnswers, answers, NormalMode)
    case HowMuchForeignTaxDeductedFromDividendIncomePage(countryCode) =>
      taxYear => _ => _ => ClaimForeignTaxCreditReliefController.onPageLoad(taxYear, countryCode, NormalMode)
    case ClaimForeignTaxCreditReliefPage(countryCode) =>
      taxYear => _ => _ => DividendsSectionCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case YourForeignDividendsByCountryPage =>
      taxYear => _ => userAnswers => yourForeignDividendsByCountryNavigation(taxYear, userAnswers)
    case DividendsSectionFinishedPage =>
      taxYear => _ => _ => SummaryController.show(taxYear)
    case RemoveForeignDividendPage =>
      taxYear => _ => userAnswers => removeForeignDividendNavigation(taxYear, userAnswers)
    case _ => _ => _ => _ => controllers.routes.IndexController.onPageLoad

  }

  private val checkRouteMap: Page => Int => UserAnswers => UserAnswers => Call = {
    case CountryReceiveDividendIncomePage(index) =>
      taxYear => _ => userAnswers => dividendIncomeCountryNavigation(taxYear, index, userAnswers)
    case IncomeBeforeForeignTaxDeductedPage(countryCode) =>
      taxYear => _ => _ => DividendsSectionCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ForeignTaxDeductedFromDividendIncomePage(countryCode) =>
      taxYear => previousAnswers => answers => foreignTaxDeductedNavigation(taxYear, countryCode, previousAnswers, answers, CheckMode)
    case HowMuchForeignTaxDeductedFromDividendIncomePage(countryCode) =>
      taxYear => _ => _ => DividendsSectionCheckYourAnswersController.onPageLoad(taxYear, countryCode)
    case ClaimForeignTaxCreditReliefPage(countryCode) =>
      taxYear => _ => _ => DividendsSectionCheckYourAnswersController.onPageLoad(taxYear, countryCode)


    case _ => _ => _ => _ => controllers.routes.IndexController.onPageLoad
  }

  def nextPage(page: Page, taxYear: Int, mode: Mode, previousUserAnswers: UserAnswers, userAnswers: UserAnswers): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(taxYear)(previousUserAnswers)(userAnswers)
      case CheckMode =>
        checkRouteMap(page)(taxYear)(previousUserAnswers)(userAnswers)
    }


  private def dividendIncomeCountryNavigation(taxYear: Int, index: Int, userAnswers: UserAnswers): Call = {
    userAnswers.get(CountryReceiveDividendIncomePage(index)) match {
      case Some(country) => IncomeBeforeForeignTaxDeductedController.onPageLoad(taxYear, country.code, NormalMode)
      case None => controllers.routes.IndexController.onPageLoad
    }
  }

  private def foreignTaxDeductedNavigation(taxYear: Int, countryCode: String, previousAnswers: UserAnswers, userAnswers: UserAnswers, mode: Mode): Call = {
    mode match {
      case NormalMode =>
        userAnswers.get(ForeignTaxDeductedFromDividendIncomePage(countryCode)) match {
          case Some(true) => HowMuchForeignTaxDeductedFromDividendIncomeController.onPageLoad(taxYear, countryCode, mode)
          case Some(false) => DividendsSectionCheckYourAnswersController.onPageLoad(taxYear, countryCode)
          case None => controllers.routes.IndexController.onPageLoad
        }
      case CheckMode =>
        (
          previousAnswers.get(ForeignTaxDeductedFromDividendIncomePage(countryCode)),
          userAnswers.get(ForeignTaxDeductedFromDividendIncomePage(countryCode))
        ) match {
          case (_, Some(false)) =>DividendsSectionCheckYourAnswersController.onPageLoad(taxYear, countryCode)
          case (Some(true), Some(true)) => DividendsSectionCheckYourAnswersController.onPageLoad(taxYear, countryCode)
          case (_, Some(true)) => HowMuchForeignTaxDeductedFromDividendIncomeController.onPageLoad(taxYear, countryCode, NormalMode)
          case _ => controllers.routes.IndexController.onPageLoad
        }
    }
  }

  private def getNextIndex(countryArr: Array[Country], userAnswers: Option[UserAnswers]): Int =
    userAnswers.map { userAnswers =>
        countryArr.foldLeft(countryArr.length) { (acc, country) =>
          (
            userAnswers.get(IncomeBeforeForeignTaxDeductedPage(country.code)),
            userAnswers.get(ForeignTaxDeductedFromDividendIncomePage(country.code)),
            userAnswers.get(HowMuchForeignTaxDeductedFromDividendIncomePage(country.code)),
            userAnswers.get(ClaimForeignTaxCreditReliefPage(country.code))
          ) match {
            case (Some(_), Some(true), Some(_), Some(_)) => acc
            case (Some(_), Some(false), _, _)            => acc
            case _                                       => countryArr.indexOf(country) min acc
          }
        }
      }
      .getOrElse(0)

  private def yourForeignDividendsByCountryNavigation(taxYear: Int, userAnswers: UserAnswers): Call = {
    userAnswers.get(YourForeignDividendsByCountryPage) match {
      case Some(true) =>
        val countries = userAnswers.get(DividendIncomeSourceCountries).getOrElse(Array.empty)
        val index = getNextIndex(countries, Some(userAnswers))
        CountryReceiveDividendIncomeController.onPageLoad(taxYear, index, NormalMode)
      case Some(false) =>
        DividendsSectionFinishedController.onPageLoad(taxYear)
    }
  }

  private def removeForeignDividendNavigation(taxYear: Int, userAnswers: UserAnswers): Call = {
    val index = userAnswers.get(DividendIncomeSourceCountries).map(_.length).getOrElse(0)
    userAnswers.get(RemoveForeignDividendPage) match {
      case Some(true) if index >= 1 => YourForeignDividendsByCountryController.onPageLoad(taxYear, NormalMode)
      case Some(true) => CountryReceiveDividendIncomeController.onPageLoad(taxYear, index, NormalMode)
      case _ => YourForeignDividendsByCountryController.onPageLoad(taxYear, NormalMode)
    }
  }
}
