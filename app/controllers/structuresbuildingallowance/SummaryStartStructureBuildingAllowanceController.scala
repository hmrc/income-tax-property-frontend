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

import controllers.actions._
import forms.structurebuildingallowance.ClaimStructureBuildingAllowanceFormProvider
import models.{Mode, NormalMode}
import navigation.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.structurebuildingallowance.StructureBuildingAllowancePage
import views.html.structurebuildingallowance.ClaimStructureBuildingAllowanceView

import javax.inject.Inject
import scala.concurrent.Future

class SummaryStartStructureBuildingAllowanceController @Inject()(
                                                                  override val messagesApi: MessagesApi,
                                                                  sessionRepository: SessionRepository,
                                                                  navigator: Navigator,
                                                                  identify: IdentifierAction,
                                                                  getData: DataRetrievalAction,
                                                                  requireData: DataRequiredAction,
                                                                  formProvider: ClaimStructureBuildingAllowanceFormProvider,
                                                                  val controllerComponents: MessagesControllerComponents,
                                                                  view: ClaimStructureBuildingAllowanceView
                                                                ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      //controllers.routes.ClaimStructureBuildingAllowanceController.onPageLoad(taxYear, NormalMode).url
      //Ok(view(StructureBuildingAllowancePage(taxYear, request.user.isAgentMessageKey)))
      val emptyForm = new ClaimStructureBuildingAllowanceFormProvider()(request.user.isAgentMessageKey)
      Ok(view(emptyForm, taxYear, NormalMode, request.user.isAgentMessageKey))
  }

/*  def onSubmit(taxYear: Int, mode: Mode, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, taxYear, request.user.isAgentMessageKey, mode, index))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(StructureBuildingAllowancePage(taxYear, request.user.isAgentMessageKey), value))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(StructureBuildingAllowancePage(), taxYear, mode, index, request.userAnswers, updatedAnswers))
      )
  }*/
}
