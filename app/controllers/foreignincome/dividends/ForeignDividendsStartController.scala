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

package controllers.foreignincome.dividends

import controllers.foreignincome.dividends.routes.{YourForeignDividendsByCountryController, CountryReceiveDividendIncomeController}
import controllers.actions._
import models.{NormalMode, UserAnswers}
import pages.foreign.Country
import pages.foreignincome.dividends.{ClaimForeignTaxCreditReliefPage, ForeignTaxDeductedFromDividendIncomePage, HowMuchForeignTaxDeductedFromDividendIncomePage}
import pages.foreignincome.{DividendIncomeSourceCountries, IncomeBeforeForeignTaxDeductedPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.foreignincome.dividends.ForeignDividendsStartView

import javax.inject.Inject

class ForeignDividendsStartController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  val controllerComponents: MessagesControllerComponents,
  view: ForeignDividendsStartView
) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData) { implicit request =>
    val countryArr: Array[Country] = request.userAnswers.flatMap(_.get(DividendIncomeSourceCountries)).getOrElse(Array.empty)
    val nextIndex = getNextIndex(countryArr, request.userAnswers)
    val nextPageLink: String = if (nextIndex > 0 && nextIndex == countryArr.length){
      YourForeignDividendsByCountryController.onPageLoad(taxYear, NormalMode).url
    } else {
      CountryReceiveDividendIncomeController.onPageLoad(taxYear, nextIndex, NormalMode).url
    }
    Ok(view(taxYear, nextPageLink, request.user.isAgentMessageKey))
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
}
