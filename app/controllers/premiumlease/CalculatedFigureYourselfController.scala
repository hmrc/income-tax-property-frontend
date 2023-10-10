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

package controllers.premiumlease

import controllers.actions._
import forms.premiumlease.CalculatedFigureYourselfFormProvider
import models.Mode
import navigation.Navigator
import pages.CalculatedFigureYourselfPage
import pages.premiumlease.{PremiumsGrantLeasePage, RecievedGrantLeaseAmountPage, YearLeaseAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.premiumlease.CalculatedFigureYourselfView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CalculatedFigureYourselfController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: CalculatedFigureYourselfFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: CalculatedFigureYourselfView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form = formProvider(request.isAgentMessageKey)
      val preparedForm = request.userAnswers.get(CalculatedFigureYourselfPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, taxYear, mode, request.isAgentMessageKey))
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider(request.isAgentMessageKey)
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, taxYear, mode, request.isAgentMessageKey))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(CalculatedFigureYourselfPage, value))
            _              <- sessionRepository.set(updatedAnswers)
            _              = if (value.calculatedFigureYourself) clearData
          } yield Redirect(navigator.nextPage(CalculatedFigureYourselfPage, taxYear, mode, updatedAnswers))
      )
  }

  private def clearData: Future[Unit] = {
    for {
      _ <- sessionRepository.clear(RecievedGrantLeaseAmountPage)
      _ <- sessionRepository.clear(YearLeaseAmountPage)
      _ <- sessionRepository.clear(PremiumsGrantLeasePage)
    } yield ()
  }
}
