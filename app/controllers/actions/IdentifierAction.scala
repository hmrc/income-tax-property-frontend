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
import models.requests.IdentifierRequest
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import scala.concurrent.{ExecutionContext, Future}
import play.api.Logging

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject()(
                                               override val authConnector: AuthConnector,
                                               config: FrontendAppConfig,
                                               val parser: BodyParsers.Default
                                             )
                                             (implicit val executionContext: ExecutionContext) extends IdentifierAction with AuthorisedFunctions with Logging {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised()
      .retrieve(
        Retrievals.affinityGroup and
          Retrievals.internalId and
          Retrievals.confidenceLevel
      ) {
        case Some(Individual) ~ _ ~ confidenceLevel if confidenceLevel < ConfidenceLevel.L250  =>
          upliftIv
        case Some(Individual) ~ Some(internalId) ~ _ =>
          block(IdentifierRequest(request, internalId, isAgent = false))
        case Some(Agent) ~ Some(internalId) ~ _ =>
          block(IdentifierRequest(request, internalId, isAgent = true))
      }.recoverWith {
      case _: NoActiveSession =>
        Future.successful(Redirect(config.loginUrl))
      case _: BearerTokenExpired =>
        Future.successful(Redirect(config.loginUrl))
      case _: AuthorisationException =>
        Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))
    }
  }

  private def upliftIv: Future[Result] = {
    val logMessage = "[AuthorisedAction][individualAuthentication] User has confidence level below 250, routing user to IV uplift."
    logger.info(logMessage)
    Future(Redirect(config.incomeTaxSubmissionIvRedirect))
  }
}

class SessionIdentifierAction @Inject()(val authConnector: AuthConnector,
                                        val parser: BodyParsers.Default
                                       )
                                       (implicit val executionContext: ExecutionContext) extends IdentifierAction with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    authorised()
        .retrieve(
          Retrievals.affinityGroup
        ) {
          case Some(affinityGroup) =>
            (affinityGroup, hc.sessionId) match {
              case (Agent, Some(session)) =>
                block(IdentifierRequest(request, session.value, isAgent = true))
              case (Individual, Some(session)) =>
                block(IdentifierRequest(request, session.value, isAgent = false))
              case (_, None) =>
                Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
            }
          case _ => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        }(hc, executionContext)
  }
}
