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

import audit.{ForeignDividends, ForeignDividendsByCountry, ReadForeignDividendsByCountry}
import controllers.actions._
import controllers.exceptions.SaveJourneyAnswersFailed
import service.PropertySubmissionService
import models.{JourneyPath, JourneyContext}
import models.requests.DataRequest
import pages.foreign.Country
import pages.foreignincome.CountryReceiveDividendIncomePage
import play.api.i18n.Lang.logger

import javax.inject.Inject
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.language.LanguageUtils
import viewmodels.checkAnswers.foreignincome.dividends._
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreignincome.dividends.DividendsSectionCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class DividendsSectionCheckYourAnswersController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: DividendsSectionCheckYourAnswersView,
                                       languageUtils: LanguageUtils,
                                       propertySubmissionService: PropertySubmissionService,
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val country = request.userAnswers.get(CountryReceiveDividendIncomePage(0)).getOrElse(Country("", ""))
      val summaryListRows = SummaryListViewModel(
        rows = Seq(
        CountryReceiveDividendIncomeSummary.row(taxYear, 0, request.userAnswers, languageUtils.getCurrentLang.locale.toString),
        IncomeBeforeForeignTaxDeductedSummary.row(taxYear, country.code, request.userAnswers),
        ForeignTaxDeductedFromDividendIncomeSummary.row(taxYear, country.code, request.user.isAgentMessageKey, country, request.userAnswers),
        HowMuchForeignTaxDeductedFromDividendIncomeSummary.row(taxYear, country.code, request.userAnswers),
        ClaimForeignTaxCreditReliefSummary.row(taxYear, country.code, request.user.isAgentMessageKey, request.userAnswers)
      ).flatten
    )
      Ok(view(summaryListRows, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val country = request.userAnswers.get(CountryReceiveDividendIncomePage(0)).getOrElse(Country("", ""))
      request.userAnswers
        .get(ForeignDividends)
      // TO DO - Save functionality plus backend work and remove redirect
//      .fold {
//      val errorMsg =
//      s"Foreign dividends section is missing for userId: ${request.userId}, taxYear: $taxYear"
//      logger.error(errorMsg)
//      Future.successful(NotFound(errorMsg))
//      } { foreignDividends =>
//        saveDividends(taxYear, request, Some(foreignDividends), None)
//      }
      request.userAnswers
        .get(ReadForeignDividendsByCountry(country.code))
//        .fold {
//          val errorMsg =
//            s"Foreign dividends by country section is missing for userId: ${request.userId}, taxYear: $taxYear"
//            logger.error(errorMsg)
//            Future.successful(NotFound(errorMsg))
//          } { foreignDividendsByCountry =>
//            saveDividends(taxYear, request, None, Some(foreignDividendsByCountry))
//          }
      Future(Redirect(controllers.foreignincome.dividends.routes.DividendsSectionFinishedController.onPageLoad(taxYear)))
        }

  private def saveDividends(
  taxYear: Int,
  request: DataRequest[AnyContent],
  dividends: Option[ForeignDividends] ,
  dividendsByCountry: Option[ForeignDividendsByCountry]
  )(implicit hc: HeaderCarrier): Future[Result] = {
    val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, JourneyPath.ForeignDividends)
    propertySubmissionService.saveForeignDividendsJourneyAnswers(context,dividends).flatMap {
      case Right(_) =>
        propertySubmissionService.saveForeignDividendsJourneyAnswers(context,dividendsByCountry).flatMap {
          case Right(_) =>
            Future
              .successful(Redirect(controllers.foreignincome.dividends.routes.DividendsSectionFinishedController.onPageLoad(taxYear)))
          case Left(error) =>
            logger.error(s"Failed to save Dividends section: ${error.toString}")
            Future.failed(SaveJourneyAnswersFailed("Failed to save Dividends section"))
        }
      case Left(error) =>
        logger.error(s"Failed to save Dividends section: ${error.toString}")
        Future.failed(SaveJourneyAnswersFailed("Failed to save Dividends section"))
    }
  }
}
