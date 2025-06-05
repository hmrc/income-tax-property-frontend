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

package controllers.actions

import com.google.inject.Inject
import common.{EnrolmentIdentifiers, EnrolmentKeys}
import config.FrontendAppConfig
import controllers.routes
import handlers.ErrorHandler
import models.errors.MissingAgentClientDetails
import models.requests.IdentifierRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results._
import play.api.mvc._
import service.SessionDataService
import uk.gov.hmrc.auth.core.ConfidenceLevel.L250
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.EnrolmentHelper._
import utils.{EnrolmentHelper, SessionHelper}

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction
  extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject() (
                                                override val authConnector: AuthConnector,
                                                errorHandler: ErrorHandler,
                                                sessionDataService: SessionDataService,
                                                val parser: BodyParsers.Default
                                              )(
                                              val appConfig: FrontendAppConfig,
                                              val mcc: MessagesControllerComponents
                                              ) extends IdentifierAction with AuthorisedFunctions with I18nSupport with SessionHelper {

  implicit val executionContext: ExecutionContext = mcc.executionContext
  implicit val messagesApi: MessagesApi = mcc.messagesApi

  private val minimumConfidenceLevel: L250.type = ConfidenceLevel.L250

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
    implicit val req: Request[A] = request
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    withSessionId { sessionId =>
      authorised().retrieve(Retrievals.internalId and Retrievals.affinityGroup) {
        case Some(internalId) ~ Some(AffinityGroup.Agent) => agentAuthentication(block, internalId, sessionId)(request, hc)
        case Some(internalId) ~ Some(individualUser) => individualAuthentication(block, individualUser, internalId, sessionId)(request, hc)
      } recoverWith {
        case _: NoActiveSession =>
          logger.error(s"[IdentifierAction][invokeBlock] - No active session found for User")
          Future(Redirect(appConfig.loginUrl))
        case _: AuthorisationException =>
          logger.warn(s"[IdentifierAction][invokeBlock] - User failed to authenticate")
          Future(Redirect(routes.UnauthorisedController.onPageLoad))
        case e =>
          logger.error(s"[IdentifierAction][invokeBlock] - Unexpected exception of type '${e.getClass.getSimpleName}' was caught.")
          errorHandler.internalServerError()(request)
      }
    }
  }

  private[actions] def individualAuthentication[A](
    block: IdentifierRequest[A] => Future[Result],
    affinityGroup: AffinityGroup,
    internalId: String,
    sessionId: String
  )(implicit request: Request[A], hc: HeaderCarrier): Future[Result] =
    authorised().retrieve(allEnrolments and Retrievals.confidenceLevel) {
      case enrolments ~ confidenceLevel if confidenceLevel >= minimumConfidenceLevel =>
        (
          EnrolmentHelper.getEnrolmentValueOpt(EnrolmentKeys.Individual, EnrolmentIdentifiers.individualId, enrolments),
          EnrolmentHelper.getEnrolmentValueOpt(EnrolmentKeys.nino, EnrolmentIdentifiers.nino, enrolments)
        ) match {
          case (Some(mtdItId), Some(nino)) =>
            block(
              IdentifierRequest(
                request,
                internalId,
                models.User(
                  mtditid = mtdItId,
                  nino = nino,
                  affinityGroup = affinityGroup.toString,
                  sessionId = sessionId,
                  agentRef = None
                )
              )
            )
          case (mtditid, nino) =>
            logger.warn(
              s"[AuthorisedAction][individualAuthentication] - User has missing MTDITID and/or NINO." +
                s"Redirecting to ${appConfig.loginUrl}. MTDITID missing:${mtditid.isEmpty}, NINO missing:${nino.isEmpty}"
            )
            Future.successful(Redirect(appConfig.loginUrl))
        }
      case _ ~ confidenceLevel =>
        logger.warn(
          s"[AuthenticatedIdentifierAction][invokeBlock] - Insufficient confidence level of $confidenceLevel returned from auth." +
            s" Redirecting to IV uplift."
        )
        upliftIv
    }

  private val agentAuthLogString: String = "[AuthorisedAction][agentAuthentication]"

  private[actions] def agentAuthentication[A](
                                               block: IdentifierRequest[A] => Future[Result],
                                               internalId: String,
                                               sessionId: String
                                             )(implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {
    sessionDataService.getSessionData(sessionId).flatMap { sessionData =>
      authorised(agentAuthPredicate(sessionData.mtditid))
        .retrieve(allEnrolments) {
          populateAgent(block, internalId, sessionData.mtditid, sessionData.nino, _, isSupportingAgent = false, sessionId)
        }
        .recoverWith(agentRecovery(block, internalId, sessionData.mtditid, sessionData.nino, sessionId))
    }.recover {
      case _: MissingAgentClientDetails =>
        Redirect(appConfig.viewAndChangeEnterUtrUrl)
    }
  }

  private def agentRecovery[A](block: IdentifierRequest[A] => Future[Result], internalId: String, mtdItId: String, nino: String, sessionId: String)(implicit
                                                                                                                                 request: Request[A],hc: HeaderCarrier): PartialFunction[Throwable, Future[Result]] = {
    case _: AuthorisationException =>
      authorised(secondaryAgentPredicate(mtdItId))
        .retrieve(allEnrolments) {
          populateAgent(block, internalId, mtdItId, nino, _, isSupportingAgent = true, sessionId)
        }
        .recoverWith {
          case _: AuthorisationException =>
            logger.warn(s"$agentAuthLogString - Agent does not have delegated authority for Client.")
            Future(Redirect(routes.UnauthorisedController.onPageLoad))
          case e =>
            logger.error(s"$agentAuthLogString - Unexpected exception of type '${e.getClass.getSimpleName}' was caught.")
            errorHandler.internalServerError()
        }
    case e =>
      logger.error(s"$agentAuthLogString - Unexpected exception of type '${e.getClass.getSimpleName}' was caught.")
      errorHandler.internalServerError()
  }

  private def populateAgent[A](block: IdentifierRequest[A] => Future[Result],
                               internalId: String,
                               mtdItId: String,
                               nino: String,
                               enrolments: Enrolments,
                               isSupportingAgent: Boolean,
                               sessionId: String)(implicit request: Request[A]): Future[Result] =
    if (isSupportingAgent) {
      logger.warn(s"$agentAuthLogString - Secondary agent unauthorised")
      Future.successful(Redirect(controllers.routes.SupportingAgentAuthErrorController.show))
    } else {
      enrolmentGetIdentifierValue(EnrolmentKeys.Agent, EnrolmentIdentifiers.agentReference, enrolments) match {
        case Some(arn) =>
          block(IdentifierRequest(request, internalId, models.User(mtdItId, nino, AffinityGroup.Agent.toString, sessionId, Some(arn), isSupportingAgent)))
        case None =>
          logger.warn(s"$agentAuthLogString Agent with no HMRC-AS-AGENT enrolment. Rendering unauthorised view.")
          Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))
      }
    }

  private[actions] def enrolmentGetIdentifierValue(
    checkedKey: String,
    checkedIdentifier: String,
    enrolments: Enrolments
  ): Option[String] = enrolments.enrolments.collectFirst { case Enrolment(`checkedKey`, enrolmentIdentifiers, _, _) =>
    enrolmentIdentifiers.collectFirst { case EnrolmentIdentifier(`checkedIdentifier`, identifierValue) =>
      identifierValue
    }
  }.flatten

  private def upliftIv: Future[Result] = {
    logger.info("[AuthorisedAction][individualAuthentication] User has confidence level below 250, routing user to IV uplift.")
    Future(Redirect(appConfig.incomeTaxSubmissionIvRedirect))
  }
}
