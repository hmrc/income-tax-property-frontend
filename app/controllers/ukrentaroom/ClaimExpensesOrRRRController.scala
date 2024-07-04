/*
 * Copyright 2024 HM Revenue & Customs
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

import controllers.actions._
import forms.ukrentaroom.ClaimExpensesOrRRRFormProvider
import models.{Mode, RentARoom}
import navigation.Navigator
import pages.ukrentaroom.{ClaimExpensesOrRRRPage, UkRentARoomJointlyLetPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ukrentaroom.ClaimExpensesOrRRRView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClaimExpensesOrRRRController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ClaimExpensesOrRRRFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ClaimExpensesOrRRRView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      val preparedForm = request.userAnswers.get(ClaimExpensesOrRRRPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      val jointlyLet = request.userAnswers.get(UkRentARoomJointlyLetPage(RentARoom)) match {
        case Some(true)  => "jointlyLet"
        case Some(false) => "notJointlyLet"
      }
      Ok(view(preparedForm, taxYear, mode, request.user.isAgentMessageKey, jointlyLet))
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      val jointlyLet = request.userAnswers.get(UkRentARoomJointlyLetPage(RentARoom)) match {
        case Some(true)  => "jointlyLet"
        case Some(false) => "notJointlyLet"
      }
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future
              .successful(BadRequest(view(formWithErrors, taxYear, mode, request.user.isAgentMessageKey, jointlyLet))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ClaimExpensesOrRRRPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator.nextPage(ClaimExpensesOrRRRPage, taxYear, mode, request.userAnswers, updatedAnswers)
            )
        )
  }
}
