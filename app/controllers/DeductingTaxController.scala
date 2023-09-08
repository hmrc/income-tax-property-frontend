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

package controllers

import controllers.actions._
import forms.{DeductingTax, DeductingTaxFormProvider}

import javax.inject.Inject
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.DeductingTaxPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import service.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DeductingTaxView

import scala.concurrent.{ExecutionContext, Future}

class DeductingTaxController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: DeductingTaxFormProvider,
                                         sessionService: SessionService,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: DeductingTaxView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData) {
    implicit request =>
      if (request.userAnswers.isEmpty) {sessionService.createNewEmptySession(request.userId)}
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(DeductingTaxPage) match {
        case None => form
        case Some(value) => form.fill(DeductingTax(value, None))
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(DeductingTaxPage, value.yesNo))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(DeductingTaxPage, mode, updatedAnswers))
      )
  }
}
