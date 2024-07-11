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

import audit.RentARoomAuditModel._
import audit.RentalsAndRentARoomAuditModel._
import audit.{AuditService, RentARoomAuditModel, RentalsAndRentARoomAuditModel}
import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.requests.DataRequest
import models.{JourneyContext, PropertyType, RaRAbout, RentARoom, RentalsAndRaRAbout, RentalsAndRentARoom}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Writes
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.adjustments.BalancingChargeSummary
import viewmodels.checkAnswers.ukrentaroom.{ClaimExpensesOrRRRSummary, TotalIncomeAmountSummary, UkRentARoomJointlyLetSummary}
import viewmodels.govuk.summarylist._
import views.html.ukrentaroom.{CheckYourAnswersView, RentalsAndRaRCheckYourAnswersView}

import scala.concurrent.{ExecutionContext, Future}

class RentalsAndRaRCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  propertySubmissionService: PropertySubmissionService,
  view: RentalsAndRaRCheckYourAnswersView,
  audit: AuditService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val ukRentARoomJointlyLetSummary =
        UkRentARoomJointlyLetSummary.row(
          taxYear,
          request.userAnswers,
          request.user.isAgentMessageKey,
          RentalsAndRentARoom
        )
      val totalIncomeAmountSummary =
        TotalIncomeAmountSummary.row(taxYear, request.userAnswers, request.user.isAgentMessageKey, RentalsAndRentARoom)
      val claimExpensesOrRRRSummary =
        ClaimExpensesOrRRRSummary.rows(taxYear, request.user.isAgentMessageKey, request.userAnswers) //Todo: Update in related ticket to parameterise

      val balancingChargeSummary =
        BalancingChargeSummary.row(taxYear, request.userAnswers) //Todo: Update in related ticket to parameterise
      val list = SummaryListViewModel(
        rows = (Seq(
          ukRentARoomJointlyLetSummary,
          totalIncomeAmountSummary
        ) ++ claimExpensesOrRRRSummary ++ Seq(balancingChargeSummary)).flatten
      )

      Ok(view(list, taxYear))
    }

  def onSubmit(taxYear: Int): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val context =
        JourneyContext(taxYear, request.user.mtditid, request.user.nino, "rentals-and-rent-a-room-about")

      val rentalsAndRaRAboutMaybe = request.userAnswers.get(RentalsAndRaRAbout)
      sendRentalsAndRaRAbout(taxYear, request, context, rentalsAndRaRAboutMaybe)

    }

  private def sendRentalsAndRaRAbout(
    taxYear: Int,
    request: DataRequest[AnyContent],
    context: JourneyContext,
    aboutMaybe: Option[RentalsAndRaRAbout]
  )(implicit
    hc: HeaderCarrier
  ): Future[Result] =
    aboutMaybe.fold[Future[Result]] {
      logger.error("Rentals and Rent A Room Section is not present in userAnswers")
      Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    } { about =>
      propertySubmissionService
        .saveJourneyAnswers[RentalsAndRaRAbout](context, about)
        .map {
          case Left(_) =>
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          case Right(_) =>
            auditCYA(taxYear, request, about)

            Redirect(controllers.ukrentaroom.routes.AboutSectionCompleteController.onPageLoad(taxYear))
        }
    }

  private def auditCYA(taxYear: Int, request: DataRequest[AnyContent], about: RentalsAndRaRAbout)(implicit
    hc: HeaderCarrier,
    writesRentalsAndRentARoom: Writes[RentalsAndRentARoomAuditModel[RentalsAndRaRAbout]]
  ): Unit = {

    val auditModel = RentalsAndRentARoomAuditModel(
      request.user.nino,
      request.user.affinityGroup,
      request.user.mtditid,
      request.user.agentRef,
      taxYear,
      isUpdate = false,
      "PropertyRentalsRentARoom",
      about
    )

    audit.sendRentalsAndRentARoomAuditEvent(auditModel)
  }

}
