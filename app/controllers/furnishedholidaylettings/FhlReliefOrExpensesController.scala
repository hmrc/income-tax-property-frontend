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

package controllers.furnishedholidaylettings

import controllers.actions._
import forms.furnishedholidaylettings.FhlReliefOrExpensesFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.Navigator
import pages.furnishedholidaylettings.{FhlJointlyLetPage, FhlReliefOrExpensesPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.furnishedholidaylettings.FhlReliefOrExpensesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FhlReliefOrExpensesController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: FhlReliefOrExpensesFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: FhlReliefOrExpensesView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val RELIEF_JOINTLY_LET = "£3,750"
  private val RELIEF_NOT_JOINTLY_LET = "£7,500"

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      withRelief { relief =>
        val form = formProvider(request.user.isAgentMessageKey)
        val preparedForm = request.userAnswers.get(FhlReliefOrExpensesPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }

        Future.successful(Ok(view(preparedForm, taxYear, mode, relief)))
      }
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      withRelief { relief =>
        val form = formProvider(request.user.isAgentMessageKey)
        form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, taxYear, mode, relief))),

          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(FhlReliefOrExpensesPage, value))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(FhlReliefOrExpensesPage, taxYear, mode, request.userAnswers, updatedAnswers))
        )
      }
  }

  private def withRelief(block: String => Future[Result])
                            (implicit request: DataRequest[AnyContent]): Future[Result] = {
    val isJointlyLet: Boolean = request.userAnswers.get(FhlJointlyLetPage).get
    val relief = if (isJointlyLet) RELIEF_JOINTLY_LET else RELIEF_NOT_JOINTLY_LET
    block(relief)
  }
}
