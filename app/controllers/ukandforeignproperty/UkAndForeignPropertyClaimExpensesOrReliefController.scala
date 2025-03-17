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

package controllers.ukandforeignproperty

import controllers.actions._
import forms.ukandforeignproperty.UkAndForeignPropertyClaimExpensesOrReliefFormProvider
import models.{Mode, UkAndForeignPropertyClaimExpensesOrRelief}
import navigation.UkAndForeignPropertyNavigator
import pages.ukandforeignproperty.UkAndForeignPropertyClaimExpensesOrReliefPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ukandforeignproperty.UkAndForeignPropertyClaimExpensesOrReliefView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UkAndForeignPropertyClaimExpensesOrReliefController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: UkAndForeignPropertyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: UkAndForeignPropertyClaimExpensesOrReliefFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: UkAndForeignPropertyClaimExpensesOrReliefView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      val preparedForm = request.userAnswers.get(UkAndForeignPropertyClaimExpensesOrReliefPage) match {
        case None =>
          form
        case Some(value) =>
          form.fill(value)
      }
      Future.successful(
        Ok(view(preparedForm, taxYear, mode, request.user.isAgentMessageKey))
      )
    }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, taxYear, mode, request.user.isAgentMessageKey))),
          (value: UkAndForeignPropertyClaimExpensesOrRelief) =>
            for {
              updatedAnswers <-
                Future.fromTry(request.userAnswers.set(UkAndForeignPropertyClaimExpensesOrReliefPage, value))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator.nextPage(
                UkAndForeignPropertyClaimExpensesOrReliefPage,
                taxYear,
                mode,
                request.userAnswers,
                updatedAnswers
              )
            )
        )
    }

}
