/*
 * Copyright 2025 HM Revenue & Customs
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
import forms.ukandforeignproperty.income.UkAndForeignPropertyForeignOtherIncomeFromPropertyFormProvider
import models.Mode
import navigation.UkAndForeignPropertyNavigator
import pages.ukandforeignproperty.ForeignOtherIncomeFromForeignPropertyPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ukandforeignproperty.UkAndForeignPropertyForeignOtherIncomeFromPropertyView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UkAndForeignPropertyForeignOtherIncomeFromPropertyController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: UkAndForeignPropertyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: UkAndForeignPropertyForeignOtherIncomeFromPropertyFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: UkAndForeignPropertyForeignOtherIncomeFromPropertyView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val isAgent = request.user.isAgentMessageKey
      val form: Form[BigDecimal] = formProvider(isAgent)
      val preparedForm = request.userAnswers.get(ForeignOtherIncomeFromForeignPropertyPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, taxYear, mode, isAgent))
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val isAgent = request.user.isAgentMessageKey
      val form: Form[BigDecimal] = formProvider(isAgent)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, taxYear, mode, isAgent))),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(request.userAnswers.set(ForeignOtherIncomeFromForeignPropertyPage, value))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator
                .nextPage(ForeignOtherIncomeFromForeignPropertyPage, taxYear, mode, request.userAnswers, updatedAnswers)
            )
        )
  }
}
