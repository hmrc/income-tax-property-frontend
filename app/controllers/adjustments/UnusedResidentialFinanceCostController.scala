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

package controllers.adjustments

import controllers.actions._
import forms.adjustments.UnusedResidentialFinanceCostFormProvider
import models.{Mode, PropertyType, UserAnswers}
import navigation.Navigator
import pages.adjustments.UnusedResidentialFinanceCostPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import service.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.adjustments.UnusedResidentialFinanceCostView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UnusedResidentialFinanceCostController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: UnusedResidentialFinanceCostFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: UnusedResidentialFinanceCostView,
  sessionService: SessionService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, mode: Mode, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData) { implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      if (request.userAnswers.isEmpty) {
        sessionService.createNewEmptySession(request.userId)
      }
      val preparedForm =
        request.userAnswers
          .getOrElse(UserAnswers(request.userId))
          .get(UnusedResidentialFinanceCostPage(propertyType)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

      Ok(view(preparedForm, taxYear, mode, propertyType))
    }

  def onSubmit(taxYear: Int, mode: Mode, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, taxYear, mode, propertyType))),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(request.userAnswers.set(UnusedResidentialFinanceCostPage(propertyType), value))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator.nextPage(
                UnusedResidentialFinanceCostPage(propertyType),
                taxYear,
                mode,
                request.userAnswers,
                updatedAnswers
              )
            )
        )
    }
}
