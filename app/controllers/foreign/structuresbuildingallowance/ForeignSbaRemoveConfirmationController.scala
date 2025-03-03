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

package controllers.foreign.structuresbuildingallowance

import controllers.actions._
import forms.foreign.structurebuildingallowance.ForeignSbaRemoveConfirmationFormProvider
import models.{Mode, UserAnswers}
import navigation.ForeignPropertyNavigator
import pages.foreign.structurebuildingallowance.{ForeignSbaRemoveConfirmationPage, ForeignStructureBuildingAllowanceWithIndex}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.foreign.structurebuildingallowance.ForeignSbaRemoveConfirmationSummary
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.structurebuildingallowance.ForeignSbaRemoveConfirmationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class ForeignSbaRemoveConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  foreignNavigator: ForeignPropertyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ForeignSbaRemoveConfirmationFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ForeignSbaRemoveConfirmationView
)(implicit val ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, index: Int, countryCode: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val form = formProvider()
      val list = summaryList(taxYear, request.userAnswers, countryCode, index)
      Ok(view(form, list, taxYear, index, countryCode, mode))
    }

  def onSubmit(taxYear: Int, index: Int, countryCode: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form = formProvider()
      val list = summaryList(taxYear, request.userAnswers, countryCode, index)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(
                view(formWithErrors, list, taxYear, index, countryCode, mode)
              )
            ),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(request.userAnswers.set(ForeignSbaRemoveConfirmationPage(countryCode), value))
              updatedAnswers <-
                Future.fromTry {
                  if (value) {
                    updatedAnswers.remove(ForeignStructureBuildingAllowanceWithIndex(index, countryCode))
                  } else {
                    Success(updatedAnswers)
                  }
                }
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              foreignNavigator.nextPage(
                ForeignSbaRemoveConfirmationPage(countryCode),
                taxYear,
                mode,
                request.userAnswers,
                updatedAnswers
              )
            )
        )
    }

  private def summaryList(taxYear: Int, userAnswers: UserAnswers, countryCode: String, index: Int)(implicit
    messages: Messages
  ) = {
    val foreignSbaEntryToRemove =
      ForeignSbaRemoveConfirmationSummary.row(taxYear, index, userAnswers, countryCode).toSeq
    SummaryListViewModel(foreignSbaEntryToRemove)
  }
}
