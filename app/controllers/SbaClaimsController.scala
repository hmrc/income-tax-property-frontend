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

package controllers

import controllers.actions._
import forms.SbaClaimsFormProvider

import javax.inject.Inject
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.SbaClaimsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.structurebuildingallowance.StructureBuildingAllowanceClaimSummary
import views.html.SbaClaimsView
import viewmodels.govuk.summarylist._

import scala.concurrent.{ExecutionContext, Future}

class SbaClaimsController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: SbaClaimsFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: SbaClaimsView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(SbaClaimsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      val list = SummaryListViewModel(
        rows = Seq(
          StructureBuildingAllowanceClaimSummary.claims(taxYear, 0, request.userAnswers),
          StructureBuildingAllowanceClaimSummary.claims(taxYear, 0, request.userAnswers)
        ).flatten
      )

      Ok(view(preparedForm, list, taxYear, request.user.isAgentMessageKey))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val list = SummaryListViewModel(
        rows = Seq(
          StructureBuildingAllowanceClaimSummary.row(taxYear, 0, request.userAnswers)
        ).flatten
      )

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, list, taxYear, request.user.isAgentMessageKey))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(SbaClaimsPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(SbaClaimsPage, taxYear, NormalMode, request.userAnswers, updatedAnswers))
      )
  }
}
