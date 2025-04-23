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

import audit.{ForeignDividends, AuditModel}
import controllers.actions._
import controllers.exceptions.SaveJourneyAnswersFailed
import service.PropertySubmissionService
import models.{AuditPropertyType, JourneyPath, JourneyContext, SectionName, AccountingMethod}
import models.requests.DataRequest
import pages.foreign.Country
import play.api.i18n.Lang.logger

import javax.inject.Inject
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.language.LanguageUtils
import viewmodels.checkAnswers.foreignincome.dividends.{ClaimForeignTaxCreditReliefSummary, IncomeBeforeForeignTaxDeductedSummary, CountryReceiveDividendIncomeSummary}
import viewmodels.govuk.all.SummaryListViewModel
import views.html.DividendsSectionCheckYourAnswersView

import scala.concurrent.{Future, ExecutionContext}

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

  def onPageLoad(taxYear: Int, countryCode: String): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val summaryListRows = SummaryListViewModel(
        rows = Seq(
        CountryReceiveDividendIncomeSummary.row(taxYear, 0, request.userAnswers, languageUtils.getCurrentLang.locale.toString),
        IncomeBeforeForeignTaxDeductedSummary.row(taxYear, countryCode, request.userAnswers),
        //Was foreign tax deducted?,
        //How much foreign tax was deducted?,
        ClaimForeignTaxCreditReliefSummary.row(taxYear, countryCode, request.user.isAgentMessageKey, request.userAnswers)
      ).flatten
    )
      Ok(view(summaryListRows, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers
        .get(ForeignDividends)
      .fold {
      val errorMsg =
      s"Foreign dividends section is missing for userId: ${request.userId}, taxYear: $taxYear"
      logger.error(errorMsg)
      Future.successful(NotFound(errorMsg))
      } { foreignDividends =>
      saveDividends(taxYear, request, foreignDividends)
      }
  }

  private def saveDividends(
  taxYear: Int,
  request: DataRequest[AnyContent],
  dividends: ForeignDividends
  )(implicit hc: HeaderCarrier): Future[Result] = {
    val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, JourneyPath.ForeignDividends)
    propertySubmissionService.saveForeignDividendsJourneyAnswers(context,dividends).flatMap {
      case Right(_) =>
        Future
          .successful(Redirect(controllers.foreignincome.dividends.routes.DividendsSectionFinishedController.onPageLoad(taxYear)))
      case Left(error) =>
        logger.error(s"Failed to save Dividends section: ${error.toString}")
        Future.failed(SaveJourneyAnswersFailed("Failed to save Dividends section"))
    }
  }
}
