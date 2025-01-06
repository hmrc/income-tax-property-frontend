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

package controllers.foreign.adjustments

import controllers.actions._
import forms.foreign.adjustments.ForeignUnusedResidentialFinanceCostFormProvider
import models.Mode
import navigation.ForeignPropertyNavigator
import pages.foreign.adjustments.ForeignUnusedResidentialFinanceCostPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.foreign.adjustments.ForeignUnusedResidentialFinanceCostView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignUnusedResidentialFinanceCostController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        foreignPropertyNavigator: ForeignPropertyNavigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: ForeignUnusedResidentialFinanceCostFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: ForeignUnusedResidentialFinanceCostView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {



  def onPageLoad(taxYear: Int, countryCode: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      val preparedForm = request.userAnswers.get(ForeignUnusedResidentialFinanceCostPage(countryCode)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, taxYear, countryCode, request.user.isAgentMessageKey, mode))
  }

  def onSubmit(taxYear: Int, countryCode: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, taxYear, countryCode, request.user.isAgentMessageKey, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ForeignUnusedResidentialFinanceCostPage(countryCode), value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(foreignPropertyNavigator.nextPage(ForeignUnusedResidentialFinanceCostPage(countryCode), taxYear, mode, request.userAnswers, updatedAnswers))
      )
  }
}
