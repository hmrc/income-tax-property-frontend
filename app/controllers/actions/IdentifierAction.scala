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
import models.authorisation.SessionValues
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
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

    authorised()
      .retrieve(
        Retrievals.affinityGroup and
          Retrievals.internalId and
          Retrievals.confidenceLevel and
          Retrievals.allEnrolments
      ) {
        case Some(Individual) ~ _ ~ confidenceLevel ~ _ if confidenceLevel < ConfidenceLevel.L250 =>
          upliftIv
        case Some(Individual) ~ Some(internalId) ~ _ ~ enrolments =>
          identifyIndividual(request, block, internalId, enrolments)
        case Some(Agent) ~ Some(internalId) ~ _ ~ enrolments =>
          identifyAgent(request, block, internalId, enrolments)
      }
      .recoverWith {
        case _: NoActiveSession =>
          Future.successful(Redirect(config.loginUrl))
        case _: BearerTokenExpired =>
          Future.successful(Redirect(config.loginUrl))
        case _: AuthorisationException =>
          Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))
      }
  }

  private def identifyIndividual[A](
                                     request: Request[A],
                                     block: IdentifierRequest[A] => Future[Result],
                                     internalId: String,
                                     enrolments: Enrolments
                                   ): Future[Result] = {
    val optionalMtdItId: Option[String] =
      enrolmentGetIdentifierValue(
        models.authorisation.Enrolment.Individual.key,
        models.authorisation.Enrolment.Individual.value,
        enrolments
      )
    val optionalNino: Option[String] =
      enrolmentGetIdentifierValue(
        models.authorisation.Enrolment.Nino.key,
        models.authorisation.Enrolment.Nino.value,
        enrolments
      )
    (optionalMtdItId, optionalNino) match {
      case (Some(mtdItId), Some(nino)) =>
        block(
          IdentifierRequest(
            request,
            internalId,
            models.User(
              mtditid = mtdItId,
              nino = nino,
              affinityGroup = Individual.toString,
              isAgent = false,
              agentRef = None
            )
          )
        )
      case _ => Future.successful(Redirect(config.loginUrl))
    }
  }

  private def identifyAgent[A](
                                request: Request[A],
                                block: IdentifierRequest[A] => Future[Result],
                                internalId: String,
                                enrolments: Enrolments
                              ): Future[Result] = {
    val optionalNino = request.session.get(SessionValues.ClientNino)
    val optionalMtdItId = request.session.get(SessionValues.ClientMtdid)
    val optionalAgentRef: Option[String] =
      enrolmentGetIdentifierValue(
        models.authorisation.Enrolment.Agent.key,
        models.authorisation.Enrolment.Agent.value,
        enrolments
      )
    (optionalMtdItId, optionalNino, optionalAgentRef) match {
      case (Some(mtdItId), Some(nino), Some(_)) =>
        block(
          IdentifierRequest(
            request,
            internalId,
            models.User(
              mtditid = mtdItId,
              nino = nino,
              affinityGroup = Agent.toString,
              isAgent = true,
              agentRef = optionalAgentRef
            )
          )
        )
      case (Some(_), Some(_), None) =>
        val logMessage =
          s"Agent has ${models.authorisation.Enrolment.Agent.key} enrolment but does not have ${models.authorisation.Enrolment.Agent.value} identifier"
        logger.warn(logMessage)
        Future.failed(InsufficientEnrolments(logMessage))
      case _ => Future.successful(Redirect(config.loginUrl))
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
    val logMessage =
      "[AuthorisedAction][individualAuthentication] User has confidence level below 250, routing user to IV uplift."
    logger.info(logMessage)
    Future(Redirect(config.incomeTaxSubmissionIvRedirect))
  }
}
