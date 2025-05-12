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

import controllers.actions._
import models.{NormalMode, UserAnswers}
import navigation.ForeignIncomeNavigator
import pages.foreign.Country
import pages.foreignincome.DividendIncomeSourceCountries
import pages.foreignincome.dividends.DividendsSectionCheckYourAnswersPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.CountryNamesDataSource
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.language.LanguageUtils
import viewmodels.checkAnswers.foreignincome.dividends._
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreignincome.dividends.DividendsSectionCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DividendsSectionCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  navigator: ForeignIncomeNavigator,
  val controllerComponents: MessagesControllerComponents,
  view: DividendsSectionCheckYourAnswersView,
  languageUtils: LanguageUtils
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val lang = languageUtils.getCurrentLang.locale.toString
      val summaryRows = getIndexedDividendCountry(countryCode, lang, request.userAnswers).map {
        case (index, country) =>
          Seq(
            CountryReceiveDividendIncomeSummary
              .row(taxYear, index, request.userAnswers, languageUtils.getCurrentLang.locale.toString, request.user.isAgentMessageKey),
            IncomeBeforeForeignTaxDeductedSummary.row(taxYear, country, request.userAnswers, request.user.isAgentMessageKey ),
            ForeignTaxDeductedFromDividendIncomeSummary
              .row(taxYear, country, request.user.isAgentMessageKey, request.userAnswers),
            HowMuchForeignTaxDeductedFromDividendIncomeSummary.row(taxYear, country, request.userAnswers, request.user.isAgentMessageKey),
            ClaimForeignTaxCreditReliefSummary
              .row(taxYear, country.code, request.user.isAgentMessageKey, request.userAnswers)
          ).flatten
      }.getOrElse(Seq.empty)

      val summaryList = SummaryListViewModel(
        rows = summaryRows
      )
      Future.successful(Ok(view(summaryList, taxYear)))
    }

  def onSubmit(taxYear: Int): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      Future(
        Redirect(
          navigator.nextPage(DividendsSectionCheckYourAnswersPage, taxYear, NormalMode, request.userAnswers, request.userAnswers)
        )
      )
    }

  private def getIndexedDividendCountry(countryCode: String, lang: String, userAnswers: UserAnswers): Option[(Int, Country)] = {
    val countries = userAnswers.get(DividendIncomeSourceCountries).getOrElse(Array.empty)
    countries.find(_.code == countryCode).flatMap { incomeCountry =>
      val index = countries.indexOf(incomeCountry)
      CountryNamesDataSource.getCountry(countryCode, lang).map { country =>
        (index, country)
      }
    }
  }
}
