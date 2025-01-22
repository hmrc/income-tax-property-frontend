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

package controllers.foreign.adjustments

import audit.{AuditModel, AuditService}
import controllers.actions._
import controllers.exceptions.{NotFoundException, SaveJourneyAnswersFailed}
import models.AuditPropertyType.ForeignProperty
import models.requests.DataRequest
import models.{AccountingMethod, ForeignPropertyAdjustments, JourneyContext, JourneyName, JourneyPath, ReadForeignPropertyAdjustments, SectionName}
import pages.foreign.ClaimPropertyIncomeAllowanceOrExpensesPage
import pages.foreign.adjustments.ForeignUnusedLossesPreviousYearsPage
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.foreign.adjustments._
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.adjustments.ForeignAdjustmentsCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignAdjustmentsCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  propertySubmissionService: PropertySubmissionService,
  val controllerComponents: MessagesControllerComponents,
  audit: AuditService,
  view: ForeignAdjustmentsCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val claimPIA: Boolean = request.userAnswers.get(ClaimPropertyIncomeAllowanceOrExpensesPage).contains(true)
      val hasUnusedLosses: Boolean = request.userAnswers
        .get(ForeignUnusedLossesPreviousYearsPage(countryCode))
        .exists(_.unusedLossesPreviousYearsYesNo)
      val summaryListRows = Seq(
        ForeignPrivateUseAdjustmentSummary.row(taxYear, countryCode, request.userAnswers),
        ForeignBalancingChargeSummary.row(taxYear, countryCode, request.userAnswers)
      ).flatten
      val residentialFinanceOrPIARows =
        if (claimPIA) {
          PropertyIncomeAllowanceClaimSummary.row(request.userAnswers, taxYear, countryCode)
        } else {
          Seq(
            ForeignResidentialFinanceCostsSummary.row(taxYear, countryCode, request.userAnswers),
            ForeignUnusedResidentialFinanceCostSummary.row(taxYear, countryCode, request.userAnswers)
          ).flatten
        }
      val foreignLossesPrevYearsRows =
        if (hasUnusedLosses) {
          Seq(
            ForeignUnusedLossesPreviousYearsSummary.row(taxYear, countryCode, request.userAnswers),
            ForeignWhenYouReportedTheLossSummary.row(taxYear, countryCode, request.userAnswers)
          ).flatten
        } else {
          ForeignUnusedLossesPreviousYearsSummary.row(taxYear, countryCode, request.userAnswers)
        }

      val list = SummaryListViewModel(
        rows = summaryListRows
          .appendedAll(residentialFinanceOrPIARows)
          .appendedAll(foreignLossesPrevYearsRows)
      )
      Ok(view(list, taxYear, countryCode))
    }

  def onSubmit(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers
        .get(ReadForeignPropertyAdjustments(countryCode))
        .map(foreignPropertyAdjustments =>
          saveForeignPropertyAdjustments(taxYear, request, foreignPropertyAdjustments, countryCode)
        )
        .getOrElse {
          logger.error(
            s"Foreign property adjustments section is not present in userAnswers for userId: ${request.userId} "
          )
          Future.failed(
            NotFoundException("Foreign property adjustments section is not present in userAnswers")
          )
        }
    }

  private def saveForeignPropertyAdjustments(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignPropertyAdjustments: ForeignPropertyAdjustments,
    countryCode: String
  )(implicit hc: HeaderCarrier): Future[Result] = {
    val context =
      JourneyContext(taxYear, request.user.mtditid, request.user.nino, JourneyPath.ForeignPropertyAdjustments)
    propertySubmissionService.saveForeignPropertyJourneyAnswers(context, foreignPropertyAdjustments).flatMap {
      case Right(_) =>
        auditCYA(taxYear, request, foreignPropertyAdjustments, isFailed = false, AccountingMethod.Traditional)
        Future.successful(Redirect(routes.ForeignAdjustmentsCompleteController.onPageLoad(taxYear, countryCode)))
      case Left(error) =>
        logger.error(s"Failed to save Foreign Adjustments section : ${error.toString}")
        auditCYA(taxYear, request, foreignPropertyAdjustments, isFailed = true, AccountingMethod.Traditional)
        Future.failed(SaveJourneyAnswersFailed("Failed to save Foreign Adjustments section"))
    }
  }

  private def auditCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignPropertyAdjustments: ForeignPropertyAdjustments,
    isFailed: Boolean,
    accountingMethod: AccountingMethod
  )(implicit
    hc: HeaderCarrier
  ): Unit = {
    val auditModel = AuditModel(
      request.user.nino,
      request.user.affinityGroup,
      request.user.mtditid,
      request.user.agentRef,
      taxYear,
      isUpdate = false,
      sectionName = SectionName.ForeignPropertyAdjustments,
      propertyType = ForeignProperty,
      journeyName = JourneyName.ForeignProperty,
      accountingMethod = accountingMethod,
      isFailed = isFailed,
      foreignPropertyAdjustments
    )
    audit.sendAuditEvent(auditModel)
  }
}
