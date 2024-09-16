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

package controllers.adjustments

import audit.RentalsAdjustment._
import audit.{AuditService, RentalsAdjustment, RentalsAuditModel}
import controllers.actions._
import controllers.exceptions.{InternalErrorFailure, SaveJourneyAnswersFailed}
import models.requests.DataRequest
import models.{JourneyContext, Rentals}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.adjustments._
import viewmodels.govuk.summarylist._
import views.html.adjustments.AdjustmentsCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdjustmentsCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  propertySubmissionService: PropertySubmissionService,
  audit: AuditService,
  view: AdjustmentsCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          BalancingChargeSummary.row(taxYear, request.userAnswers, Rentals),
          PrivateUseAdjustmentSummary.row(taxYear, request.userAnswers, Rentals),
          PropertyIncomeAllowanceSummary.row(taxYear, request.userAnswers, Rentals),
          RenovationAllowanceBalancingChargeSummary.row(taxYear, request.userAnswers, Rentals),
          ResidentialFinanceCostSummary.row(taxYear, request.userAnswers, Rentals),
          UnusedResidentialFinanceCostSummary.row(taxYear, request.userAnswers, Rentals)
        ).flatten
      )

      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(RentalsAdjustment) match {
        case Some(adjustments) => saveAdjustments(taxYear, request, adjustments)
        case None =>
          logger.error("Adjustments Section is not present in userAnswers")
          Future.failed(InternalErrorFailure("Adjustments Section is not present in userAnswers"))
      }
  }

  private def saveAdjustments(taxYear: Int, request: DataRequest[AnyContent], adjustments: RentalsAdjustment)(implicit
    hc: HeaderCarrier
  ) = {
    val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, "property-rental-adjustments")
    propertySubmissionService.saveJourneyAnswers(context, adjustments).flatMap {
      case Right(_) =>
        auditCYA(taxYear, request, adjustments)
        Future
          .successful(Redirect(controllers.adjustments.routes.RentalsAdjustmentsCompleteController.onPageLoad(taxYear)))
      case Left(_) =>
        logger.error("Failed to save adjustments section")
        Future.failed(SaveJourneyAnswersFailed("Failed to save adjustments section"))
    }
  }

  private def auditCYA(taxYear: Int, request: DataRequest[AnyContent], adjustments: RentalsAdjustment)(implicit
    hc: HeaderCarrier
  ): Unit = {
    val auditModel = RentalsAuditModel(
      request.user.nino,
      request.user.affinityGroup,
      request.user.mtditid,
      request.user.agentRef,
      taxYear,
      isUpdate = false,
      "PropertyRentalsAdjustments",
      adjustments
    )

    audit.sendRentalsAuditEvent(auditModel)
  }
}
