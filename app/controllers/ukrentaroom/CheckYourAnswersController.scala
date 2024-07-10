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

import audit.{AuditService, RentARoomAuditModel, RentalsAndRentARoomAuditModel}
import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.requests.DataRequest
import models.{JourneyContext, PropertyType, RaRAbout, RentARoom, RentalsAndRaRAbout, RentalsAndRentARoom}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.ukrentaroom.{ClaimExpensesOrRRRSummary, TotalIncomeAmountSummary, UkRentARoomJointlyLetSummary}
import viewmodels.govuk.summarylist._
import views.html.ukrentaroom.CheckYourAnswersView
import RentARoomAuditModel._
import RentalsAndRentARoomAuditModel._
import play.api.libs.json.Writes

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

  def onPageLoad(taxYear: Int, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val ukRentARoomJointlyLetSummary =
        UkRentARoomJointlyLetSummary.row(taxYear, request.userAnswers, request.user.isAgentMessageKey, RentARoom)
      val totalIncomeAmountSummary =
        TotalIncomeAmountSummary.row(taxYear, request.userAnswers, request.user.isAgentMessageKey, RentARoom)
      val claimExpensesOrRRRSummary =
        ClaimExpensesOrRRRSummary.rows(taxYear, request.user.isAgentMessageKey, request.userAnswers)

      val list = SummaryListViewModel(
        rows = (Seq(ukRentARoomJointlyLetSummary, totalIncomeAmountSummary) ++ claimExpensesOrRRRSummary).flatten
      )

      Ok(view(list, taxYear, propertyType))
    }

  def onSubmit(taxYear: Int, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      propertyType match {
        case RentARoom =>
          val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, "uk-rent-a-room-about")

          val rarAboutMaybe = request.userAnswers.get(RaRAbout)
          sendAbout(taxYear, request, context, rarAboutMaybe)
        case RentalsAndRentARoom =>
          val context =
            JourneyContext(taxYear, request.user.mtditid, request.user.nino, "rentals-and-rent-a-room-about")

          val rentalsAndRaRAboutMaybe = request.userAnswers.get(RentalsAndRaRAbout)
          sendAbout(taxYear, request, context, rentalsAndRaRAboutMaybe)
      }

    }

  private def sendAbout[T](
    taxYear: Int,
    request: DataRequest[AnyContent],
    context: JourneyContext,
    aboutMaybe: Option[T]
  )(implicit
    hc: HeaderCarrier,
    writes: Writes[T],
    writesRentARoom: Writes[RentARoomAuditModel[T]],
    writesRentalsAndRentARoom: Writes[RentalsAndRentARoomAuditModel[T]]
  ): Future[Result] =
    aboutMaybe.fold[Future[Result]] {
      logger.error("UK Rent A Room Section is not present in userAnswers")
      Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    } { about =>
      propertySubmissionService
        .saveJourneyAnswers[T](context, about)
        .map {
          case Left(_) =>
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          case Right(_) =>
            auditCYA(taxYear, request, about)
            Redirect(controllers.ukrentaroom.routes.AboutSectionCompleteController.onPageLoad(taxYear))
        }
    }

  private def auditCYA[T](taxYear: Int, request: DataRequest[AnyContent], about: T)(implicit
    hc: HeaderCarrier,
    writes: Writes[T],
    writesRentARoom: Writes[RentARoomAuditModel[T]],
    writesRentalsAndRentARoom: Writes[RentalsAndRentARoomAuditModel[T]]
  ): Unit =
    about match {
      case RaRAbout(_, _, _) =>
        val auditModel = RentARoomAuditModel(
          request.user.nino,
          request.user.affinityGroup,
          request.user.mtditid,
          request.user.agentRef,
          taxYear,
          isUpdate = false,
          "PropertyRentARoomAbout",
          about
        )

        audit.sendRentARoomAuditEvent(auditModel)
      case RentalsAndRaRAbout(_, _, _, _) =>
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
