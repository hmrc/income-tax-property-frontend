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

package controllers.ukrentaroom.adjustments

import controllers.actions._
import forms.ukrentaroom.adjustments.RaRBalancingChargeFormProvider
import models.BalancingCharge.format
import models.Mode
import navigation.Navigator
import pages.ukrentaroom.adjustments.RaRBalancingChargePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ukrentaroom.adjustments.RaRBalancingChargeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RaRBalancingChargeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: RaRBalancingChargeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RaRBalancingChargeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      val preparedForm = request.userAnswers.get(RaRBalancingChargePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, taxYear, mode, request.user.isAgentMessageKey))
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, taxYear, mode, request.user.isAgentMessageKey))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(RaRBalancingChargePage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator.nextPage(RaRBalancingChargePage, taxYear, mode, request.userAnswers, updatedAnswers)
            )
        )
  }
}
