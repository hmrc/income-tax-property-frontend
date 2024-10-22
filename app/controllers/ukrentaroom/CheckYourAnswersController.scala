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

package controllers.ukrentaroom

import audit.{AuditService, RentARoomAuditModel}
import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.exceptions.SaveJourneyAnswersFailed
import models.JourneyPath.RentARoomAbout
import models.requests.DataRequest
import models.{JourneyContext, RaRAbout, RentARoom}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.ukrentaroom.{ClaimExpensesOrReliefSummary, JointlyLetSummary, TotalIncomeAmountSummary}
import viewmodels.govuk.summarylist._
import views.html.ukrentaroom.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  propertySubmissionService: PropertySubmissionService,
  view: CheckYourAnswersView,
  audit: AuditService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val ukRentARoomJointlyLetSummary =
        JointlyLetSummary.row(taxYear, request.userAnswers, request.user.isAgentMessageKey, RentARoom)
      val totalIncomeAmountSummary =
        TotalIncomeAmountSummary.row(taxYear, request.userAnswers, request.user.isAgentMessageKey, RentARoom)
      val claimExpensesOrReliefSummary =
        ClaimExpensesOrReliefSummary.rows(taxYear, request.user.isAgentMessageKey, request.userAnswers, RentARoom)

      val list = SummaryListViewModel(
        rows = (Seq(ukRentARoomJointlyLetSummary, totalIncomeAmountSummary) ++ claimExpensesOrReliefSummary).flatten
      )

      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, RentARoomAbout)

      val rarAboutMaybe: Option[RaRAbout] = request.userAnswers.get(RaRAbout)

      rarAboutMaybe.fold[Future[Result]] {
        logger.error("UK Rent A Room Section is not present in userAnswers")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      } { rarAbout =>
        propertySubmissionService
          .saveJourneyAnswers[RaRAbout](context, rarAbout)
          .flatMap {
            case Right(_) =>
              auditCYA(taxYear, request, rarAbout)
              Future.successful(
                Redirect(controllers.ukrentaroom.routes.AboutSectionCompleteController.onPageLoad(taxYear))
              )
            case Left(_) =>
              logger.error("Failed to save rent a room about section")
              Future.failed(SaveJourneyAnswersFailed("Failed to save rent a room about section"))
          }
      }
  }

  private def auditCYA(taxYear: Int, request: DataRequest[AnyContent], rarAbout: RaRAbout)(implicit
    hc: HeaderCarrier
  ): Unit = {
    val auditModel = RentARoomAuditModel(
      request.user.nino,
      request.user.affinityGroup,
      request.user.mtditid,
      request.user.agentRef,
      taxYear,
      isUpdate = false,
      "PropertyRentARoomAbout",
      rarAbout
    )

    audit.sendRentARoomAuditEvent(auditModel)
  }
}
