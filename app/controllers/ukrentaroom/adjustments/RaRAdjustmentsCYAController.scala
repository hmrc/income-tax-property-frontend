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

import audit.RentARoomAdjustments._
import audit.{AuditService, RentARoomAdjustments, RentARoomAuditModel}
import controllers.actions._
import models.JourneyContext
import models.requests.DataRequest
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.ukrentaroom.adjustments.RaRBalancingChargeSummary
import viewmodels.govuk.summarylist._
import views.html.ukrentaroom.adjustments.RaRAdjustmentsCYAView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RaRAdjustmentsCYAController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  propertySubmissionService: PropertySubmissionService,
  audit: AuditService,
  view: RaRAdjustmentsCYAView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          RaRBalancingChargeSummary.row(taxYear, request.userAnswers, request.user.isAgentMessageKey)
        ).flatten
      )

      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, "rent-a-room-adjustments")

      request.userAnswers.get(RentARoomAdjustments) match {
        case Some(adjustments) =>
          propertySubmissionService.saveJourneyAnswers(context, adjustments).map {
            case Right(_) =>
              auditCYA(taxYear, request, adjustments)
              Redirect(controllers.routes.SummaryController.show(taxYear))
            case Left(_) => InternalServerError
          }
        case None =>
          logger.error("Adjustments Section is not present in userAnswers")
          Future.successful(Redirect(controllers.routes.SummaryController.show(taxYear)))
      }
  }

  private def auditCYA(taxYear: Int, request: DataRequest[AnyContent], adjustments: RentARoomAdjustments)(implicit
    hc: HeaderCarrier
  ): Unit = {
    val auditModel = RentARoomAuditModel(
      request.user.nino,
      request.user.affinityGroup,
      request.user.mtditid,
      request.user.agentRef,
      taxYear,
      isUpdate = false,
      "PropertyRentARoomAdjustments",
      adjustments
    )
    audit.sendRentARoomAuditEvent(auditModel)
  }
}
