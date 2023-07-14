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

package controllers.auth

import config.FrontendAppConfig
import controllers.actions.IdentifierAction
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext


class AuthController @Inject()(
                                val controllerComponents: MessagesControllerComponents,
                                config: FrontendAppConfig,
                                sessionRepository: SessionRepository,
                                identify: IdentifierAction
                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def signOut(): Action[AnyContent] = identify.async {
    implicit request =>
      sessionRepository
        .clear(request.userId)
        .map {
          _ =>
            Redirect(config.signOutUrl, Map("continue" -> Seq(config.exitSurveyUrl)))
      }
  }

  def signOutNoSurvey(): Action[AnyContent] = identify.async {
    implicit request =>
    sessionRepository
      .clear(request.userId)
      .map {
        _ =>
        Redirect(config.signOutUrl, Map("continue" -> Seq(routes.SignedOutController.onPageLoad.url)))
      }
  }
}
