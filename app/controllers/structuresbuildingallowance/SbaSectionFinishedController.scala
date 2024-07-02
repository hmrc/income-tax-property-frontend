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

package controllers.structuresbuildingallowance

import controllers.ControllerUtils.statusForPage
import controllers.actions._
import forms.structurebuildingallowance.SbaSectionFinishedFormProvider
import models.{JourneyContext, NormalMode}
import navigation.Navigator
import pages.structurebuildingallowance.SbaSectionFinishedPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import service.JourneyAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.structurebuildingallowance.SbaSectionFinishedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SbaSectionFinishedController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: SbaSectionFinishedFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: SbaSectionFinishedView,
  journeyAnswersService: JourneyAnswersService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(SbaSectionFinishedPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, NormalMode, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, NormalMode, taxYear))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(SbaSectionFinishedPage, value))
              _              <- sessionRepository.set(updatedAnswers)
              _ <- journeyAnswersService.setStatus(
                     JourneyContext(
                       taxYear,
                       request.user.mtditid,
                       request.user.nino,
                       "rental-sba"
                     ),
                     statusForPage(value),
                     request.user
                   )
            } yield Redirect(
              navigator.nextPage(SbaSectionFinishedPage, taxYear, NormalMode, request.userAnswers, updatedAnswers)
            )
        )
  }
}
