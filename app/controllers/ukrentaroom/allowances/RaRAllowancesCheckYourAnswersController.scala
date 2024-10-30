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

package controllers.ukrentaroom.allowances

import audit.{AuditService, RentARoomAllowance, RentARoomAuditModel}
import controllers.actions._
import controllers.exceptions.SaveJourneyAnswersFailed
import models.JourneyContext
import models.JourneyPath.RentARoomAllowances
import models.backend.ServiceError
import models.requests.DataRequest
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.ukrentaroom.allowances._
import viewmodels.govuk.summarylist._
import views.html.ukrentaroom.allowances.RaRAllowancesCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RaRAllowancesCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: RaRAllowancesCheckYourAnswersView,
  auditService: AuditService,
  propertySubmissionService: PropertySubmissionService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          RaRZeroEmissionCarAllowanceSummary.row(taxYear, request.userAnswers, request.user.isAgentMessageKey),
          RaRReplacementsOfDomesticGoodsSummary.row(taxYear, request.userAnswers),
          RaRCapitalAllowancesForACarSummary.row(taxYear, request.userAnswers),
          RaROtherCapitalAllowancesSummary.row(taxYear, request.userAnswers)
        ).flatten
      )

      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(RentARoomAllowance) match {
        case Some(allowance) => saveAllowances(taxYear, request, allowance)
        case None =>
          logger.error("Allowance in rent a room is not present in userAnswers")
          Future.failed(NotFoundException)
      }
  }

  private def saveAllowances(taxYear: Int, request: DataRequest[AnyContent], allowance: RentARoomAllowance)(implicit
    hc: HeaderCarrier
  ) =
    saveAllowanceForRentARoom(taxYear, request, allowance).flatMap {
      case Right(_: Unit) =>
        auditAllowanceCYA(taxYear, request, allowance)
        Future.successful(
          Redirect(controllers.ukrentaroom.allowances.routes.RaRAllowancesCompleteController.onPageLoad(taxYear))
        )
      case Left(_) =>
        logger.error("Failed to save rent a room allowances section")
        Future.failed(SaveJourneyAnswersFailed("Failed to save rent a room allowances section"))
    }

  private def saveAllowanceForRentARoom(
    taxYear: Int,
    request: DataRequest[AnyContent],
    allowance: RentARoomAllowance
  )(implicit
    hc: HeaderCarrier
  ): Future[Either[ServiceError, Unit]] = {
    val context = JourneyContext(
      taxYear = taxYear,
      mtditid = request.user.mtditid,
      nino = request.user.nino,
      journeyPath = RentARoomAllowances
    )
    propertySubmissionService.saveJourneyAnswers[RentARoomAllowance](context, allowance)
  }

  private def auditAllowanceCYA(taxYear: Int, request: DataRequest[AnyContent], allowance: RentARoomAllowance)(implicit
    hc: HeaderCarrier
  ): Unit = {
    val event = RentARoomAuditModel[RentARoomAllowance](
      nino = request.user.nino,
      userType = request.user.affinityGroup,
      mtdItId = request.user.mtditid,
      agentReferenceNumber = request.user.agentRef,
      taxYear = taxYear,
      isUpdate = false,
      sectionName = "PropertyRentARoomAllowances",
      userEnteredRentARoomDetails = allowance
    )
    auditService.sendRentARoomAuditEvent(event)
  }
}

case object NotFoundException extends Exception("Allowance in rent a room is not present in userAnswers")
