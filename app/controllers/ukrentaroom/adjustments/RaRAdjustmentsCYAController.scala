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

package controllers.ukrentaroom.adjustments

import audit.{AuditService, RentARoomAdjustments, RentARoomAuditModel}
import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.{JourneyContext, JourneyPath}
import models.requests.DataRequest
import pages.ukrentaroom.adjustments.RaRUnusedLossesBroughtForwardPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.PropertySubmissionService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.ukrentaroom.adjustments.{RaRBalancingChargeSummary, RaRUnusedLossesBroughtForwardSummary, RarWhenYouReportedTheLossSummary, UnusedResidentialPropertyFinanceCostsBroughtFwdSummary}
import viewmodels.govuk.summarylist._
import views.html.ukrentaroom.adjustments.RaRAdjustmentsCYAView

import scala.concurrent.{ExecutionContext, Future}

class RaRAdjustmentsCYAController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: RaRAdjustmentsCYAView,
  propertySubmissionService: PropertySubmissionService,
  audit: AuditService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val hasLossesBroughtForward: Boolean = request.userAnswers
        .get(RaRUnusedLossesBroughtForwardPage)
        .exists(_.unusedLossesBroughtForwardYesOrNo)
      val rows: Seq[SummaryListRow] =
        Seq(
          RaRBalancingChargeSummary.row(taxYear, request.userAnswers, request.user.isAgentMessageKey),
          UnusedResidentialPropertyFinanceCostsBroughtFwdSummary
            .row(taxYear, request.userAnswers, request.user.isAgentMessageKey)
        ).flatten
      val unusedLossesBroughtForwardRows =
        if (hasLossesBroughtForward) {
          Seq(
            RaRUnusedLossesBroughtForwardSummary.row(taxYear, request.userAnswers, request.user.isAgentMessageKey),
            RarWhenYouReportedTheLossSummary.row(
              taxYear,
              request.userAnswers,
              request.user.isAgentMessageKey,
              request.userAnswers
                .get(RaRUnusedLossesBroughtForwardPage)
                .flatMap(_.unusedLossesBroughtForwardAmount)
                .getOrElse(BigDecimal(0))
            )
          ).flatten
        } else {
          RaRUnusedLossesBroughtForwardSummary.row(taxYear, request.userAnswers, request.user.isAgentMessageKey)
        }
      val list = SummaryListViewModel(
        rows = rows
          .appendedAll(unusedLossesBroughtForwardRows)
      )

      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers
        .get(RentARoomAdjustments)
        .map(rentARoomAdjustments => saveRentARoomAdjustments(taxYear, request, rentARoomAdjustments))
        .getOrElse {
          logger.error("Rent a room adjustments section is not present in userAnswers")
          Future.failed(AdjustmentsNotFoundException)
        }
  }

  private def saveRentARoomAdjustments(
    taxYear: Int,
    request: DataRequest[AnyContent],
    rentARoomAdjustments: RentARoomAdjustments
  )(implicit
    hc: HeaderCarrier
  ): Future[Result] = {
    val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, JourneyPath.RentARoomAdjustments)
    propertySubmissionService.saveJourneyAnswers(context, rentARoomAdjustments).flatMap {
      case Right(_) =>
        auditCYA(taxYear, request, rentARoomAdjustments)
        Future.successful(
          Redirect(controllers.ukrentaroom.adjustments.routes.RaRAdjustmentsCompleteController.onPageLoad(taxYear))
        )
      case Left(_) => Future.failed(AdjustmentsSaveFailed)
    }
  }

  private def auditCYA(taxYear: Int, request: DataRequest[AnyContent], rentARoomAdjustments: RentARoomAdjustments)(
    implicit hc: HeaderCarrier
  ): Unit = {
    val auditModel = RentARoomAuditModel(
      request.user.nino,
      request.user.affinityGroup,
      request.user.mtditid,
      agentReferenceNumber = request.user.agentRef,
      taxYear,
      isUpdate = false,
      "PropertyRentARoomAdjustments",
      rentARoomAdjustments
    )
    audit.sendRentARoomAuditEvent(auditModel)
  }

  private case object AdjustmentsNotFoundException
      extends Exception("Adjustments Section is not present in userAnswers")

  private case object AdjustmentsSaveFailed extends Exception("Unable to save Adjustments")
}
