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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.ukandforeignproperty.ClaimPropertyIncomeAllowanceOrExpensesFormProvider
import models.{Mode, PropertyType}
import navigation.UkAndForeignPropertyNavigator
import pages.ukandforeignproperty.ClaimPropertyIncomeAllowanceOrExpensesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ukandforeignproperty.ClaimPropertyIncomeAllowanceOrExpensesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClaimPropertyIncomeAllowanceOrExpensesController @Inject()(
                                                                  override val messagesApi: MessagesApi,
                                                                  sessionRepository: SessionRepository,
                                                                  navigator: UkAndForeignPropertyNavigator,
                                                                  identify: IdentifierAction,
                                                                  getData: DataRetrievalAction,
                                                                  requireData: DataRequiredAction,
                                                                  formProvider: ClaimPropertyIncomeAllowanceOrExpensesFormProvider,
                                                                  val controllerComponents: MessagesControllerComponents,
                                                                  view: ClaimPropertyIncomeAllowanceOrExpensesView
)(implicit ec: ExecutionContext)
extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, mode: Mode, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form = formProvider()
      val preparedForm = request.userAnswers.get(ClaimPropertyIncomeAllowanceOrExpensesPage(propertyType)) match {
        case None        =>
          form
        case Some(value) =>
          form.fill(value)
      }
      Future.successful(
        Ok(view(preparedForm, taxYear, mode, request.user.isAgentMessageKey))
      )
    }

  def onSubmit(taxYear: Int, mode: Mode, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form = formProvider()
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, taxYear, mode, request.user.isAgentMessageKey))),
          (value) => {
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ClaimPropertyIncomeAllowanceOrExpensesPage(propertyType), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator.nextIndex(
                ClaimPropertyIncomeAllowanceOrExpensesPage(propertyType), taxYear, mode, request.userAnswers, updatedAnswers,0)
            )
          }
        )
    }
}
