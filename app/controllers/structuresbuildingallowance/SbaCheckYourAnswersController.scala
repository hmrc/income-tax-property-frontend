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

package controllers.structuresbuildingallowance

import audit.{AuditService, RentalsAuditModel}
import controllers.actions._
import models.requests.DataRequest
import models.{JourneyContext, SbasWithSupportingQuestions}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.structurebuildingallowance._
import viewmodels.govuk.summarylist._
import views.html.structurebuildingallowance.SbaCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SbaCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  propertySubmissionService: PropertySubmissionService,
  val controllerComponents: MessagesControllerComponents,
  audit: AuditService,
  view: SbaCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          StructureBuildingQualifyingDateSummary.row(taxYear, index, request.userAnswers),
          StructureBuildingQualifyingAmountSummary.row(taxYear, index, request.userAnswers),
          StructureBuildingAllowanceClaimSummary.row(taxYear, index, request.userAnswers),
          StructuredBuildingAllowanceAddressSummary.row(taxYear, index, request.userAnswers)
        ).flatten
      )
      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val sbasWithSupportingQuestions: Option[SbasWithSupportingQuestions] =
        request.userAnswers.get(SbasWithSupportingQuestions)
      saveSba(taxYear, request, sbasWithSupportingQuestions)

  }

  private def saveSba(
    taxYear: Int,
    request: DataRequest[AnyContent],
    sbasWithSupportingQuestions: Option[SbasWithSupportingQuestions]
  )(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Result] = {
    val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, "sba")

    sbasWithSupportingQuestions match {
      case Some(e) =>
        propertySubmissionService
          .saveJourneyAnswers(context, e.copy(sbaClaims = Some(e.sbaClaims.getOrElse(false))))
          .map {
            case Right(_) =>
              auditCYA(taxYear, request, e)
              Redirect(
                controllers.structuresbuildingallowance.routes.SbaSectionFinishedController.onPageLoad(taxYear)
              )
            case Left(_) => InternalServerError
          }
      case None =>
        Future.successful(
          Redirect(
            controllers.structuresbuildingallowance.routes.SbaSectionFinishedController.onPageLoad(taxYear)
          )
        )
    }

  }

  private def auditCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    sbasWithSupportingQuestions: SbasWithSupportingQuestions
  )(implicit
    hc: HeaderCarrier
  ): Unit = {
    val auditModel = RentalsAuditModel(
      request.user.nino,
      request.user.affinityGroup,
      request.user.mtditid,
      agentReferenceNumber = request.user.agentRef,
      taxYear,
      isUpdate = false,
      "Sba",
      sbasWithSupportingQuestions
    )

    audit.sendRentalsAuditEvent(auditModel)
  }
}
