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

package controllers.about

import controllers.actions._
import forms.about.TotalIncomeFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.TotalIncomePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.about.TotalIncomeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TotalIncomeController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: TotalIncomeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  sessionService: SessionService,
  navigator: Navigator,
  view: TotalIncomeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData) { implicit request =>
    if (request.userAnswers.isEmpty) { sessionService.createNewEmptySession(request.userId) }
    val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(TotalIncomePage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, taxYear, mode, request.user.isAgentMessageKey))
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, taxYear, mode, request.user.isAgentMessageKey))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(TotalIncomePage, value))
              _              <- sessionService.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(TotalIncomePage, taxYear, mode, request.userAnswers, updatedAnswers))
        )
  }
}
