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

package controllers.actions

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.routes
import models.authorisation.{DelegatedAuthRules, SessionValues, Enrolment => EnrolmentKeys}
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.ConfidenceLevel.L250
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction
  extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject()(
                                               override val authConnector: AuthConnector,
                                               config: FrontendAppConfig,
                                               val parser: BodyParsers.Default
                                             )(implicit val executionContext: ExecutionContext)
  extends IdentifierAction with AuthorisedFunctions with Logging {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised().retrieve(Retrievals.internalId and Retrievals.affinityGroup) {
      case Some(internalId) ~ Some(AffinityGroup.Agent) =>
        agentAuthentication(block, internalId)(request, hc)
      case Some(internalId) ~ Some(affinityGroup) =>
        individualAuthentication(block, internalId, affinityGroup)(request, hc)
      case _ =>
        logger.info("[AuthenticatedIdentifierAction][invokeBlock] - No internal Id retrieved from auth")
        Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))
    } recover {
      case _: NoActiveSession =>
        Redirect(config.loginUrl)
      case _ =>
        Redirect(routes.UnauthorisedController.onPageLoad)
    }
  }

  private[actions] def individualAuthentication[A](block: IdentifierRequest[A] => Future[Result],
                                                   internalId: String,
                                                   affinityGroup: AffinityGroup)(implicit request: Request[A], hc: HeaderCarrier): Future[Result] =
    authorised().retrieve(allEnrolments and Retrievals.confidenceLevel) {
      case enrolments ~ confidenceLevel if confidenceLevel >= L250 =>
        val optionalMtdItId: Option[String] =
          enrolmentGetIdentifierValue(
            EnrolmentKeys.Individual.key,
            EnrolmentKeys.Individual.value,
            enrolments
          )
        val optionalNino: Option[String] =
          enrolmentGetIdentifierValue(
            EnrolmentKeys.Nino.key,
            EnrolmentKeys.Nino.value,
            enrolments
          )

        (optionalMtdItId, optionalNino) match {
          case (Some(mtdItId), Some(nino)) =>
            block(IdentifierRequest(
              request,
              internalId,
              models.User(
                mtditid = mtdItId,
                nino = nino,
                affinityGroup = affinityGroup.toString,
                agentRef = None
              )
            ))
          case (mtditid, nino) =>
            logger.info(s"[AuthorisedAction][individualAuthentication] - User has missing MTDITID and/or NINO." +
              s"Redirecting to ${config.loginUrl}. MTDITID missing:${mtditid.isEmpty}, NINO missing:${nino.isEmpty}")
            Future.successful(Redirect(config.loginUrl))
        }
      case _ ~ confidenceLevel =>
        logger.info(s"[AuthenticatedIdentifierAction][invokeBlock] - Insufficient confidence level of $confidenceLevel returned from auth." +
          s" Redirecting to IV uplift.")
        upliftIv
    }

  private[actions] def agentAuthPredicate(mtdId: String): Predicate =
    Enrolment(EnrolmentKeys.Individual.key)
      .withIdentifier(EnrolmentKeys.Individual.value, mtdId)
      .withDelegatedAuthRule(DelegatedAuthRules.agentDelegatedAuthRule)

  private[actions] def secondaryAgentPredicate(mtdId: String): Predicate =
    Enrolment(EnrolmentKeys.SupportingAgent.key)
      .withIdentifier(EnrolmentKeys.SupportingAgent.value, mtdId)
      .withDelegatedAuthRule(DelegatedAuthRules.supportingAgentDelegatedAuthRule)

  private[actions] def agentAuthentication[A](block: IdentifierRequest[A] => Future[Result], internalId: String)(implicit
                                                                                                                 request: Request[A],
                                                                                                                 hc: HeaderCarrier): Future[Result] =
    (request.session.get(SessionValues.ClientMtdid), request.session.get(SessionValues.ClientNino)) match {
      case (Some(mtdItId), Some(nino)) =>
        authorised(agentAuthPredicate(mtdItId))
          .retrieve(allEnrolments) {
            populateAgent(block, internalId, mtdItId, nino, _, isSupportingAgent = false)
          }
          .recoverWith(agentRecovery(block, internalId, mtdItId, nino))
      case (mtditid, nino) =>
        logger.info(
          s"[AuthenticatedIdentifierAction][agentAuthentication] - Agent does not have session key values." +
            s"Redirecting to view & change. MTDITID missing:${mtditid.isEmpty}, NINO missing:${nino.isEmpty}")
        Future.successful(Redirect(config.viewAndChangeEnterUtrUrl))
    }

  private def agentRecovery[A](block: IdentifierRequest[A] => Future[Result], internalId: String, mtdItId: String, nino: String)(implicit
                                                                                                                                 request: Request[A],hc: HeaderCarrier): PartialFunction[Throwable, Future[Result]] = {
    case _: AuthorisationException if config.emaSupportingAgentsEnabled =>
      authorised(secondaryAgentPredicate(mtdItId))
        .retrieve(allEnrolments) {
          populateAgent(block, internalId, mtdItId, nino, _, isSupportingAgent = true)
        }
        .recover { case _ =>
          logger.info(s"[AuthorisedAction][agentAuthentication] - Agent does not have secondary delegated authority for Client.")
          Redirect(routes.UnauthorisedController.onPageLoad)
        }
    case _: AuthorisationException =>
      logger.info(s"[AuthorisedAction][agentAuthentication] - Agent does not have delegated authority for Client.")
      Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))
  }

  private def populateAgent[A](block: IdentifierRequest[A] => Future[Result],
                               internalId: String,
                               mtdItId: String,
                               nino: String,
                               enrolments: Enrolments,
                               isSupportingAgent: Boolean)(implicit request: Request[A]): Future[Result] =
    enrolmentGetIdentifierValue(EnrolmentKeys.Agent.key, EnrolmentKeys.Agent.value, enrolments) match {
      case Some(arn) =>
        block(IdentifierRequest(request, internalId, models.User(mtdItId, nino, AffinityGroup.Agent.toString, Some(arn), isSupportingAgent)))
      case None =>
        logger.info("[AuthorisedAction][agentAuthentication] Agent with no HMRC-AS-AGENT enrolment. Rendering unauthorised view.")
        Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))
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
    val logMessage =
      "[AuthorisedAction][individualAuthentication] User has confidence level below 250, routing user to IV uplift."
    logger.info(logMessage)
    Future(Redirect(config.incomeTaxSubmissionIvRedirect))
  }
}
