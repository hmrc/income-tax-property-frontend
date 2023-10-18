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
import forms.UKPropertyFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.UKPropertyPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.UKPropertySelectView

import scala.concurrent.{ExecutionContext, Future}

class UKPropertySelectController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: UKPropertyFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: UKPropertySelectView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(UKPropertyPage) match {
          case None => form
          case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, taxYear, mode, request.isAgentMessageKey))
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, taxYear, mode, request.isAgentMessageKey))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(UKPropertyPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(UKPropertyPage, taxYear, mode, request.userAnswers, updatedAnswers))
      )
  }
}
