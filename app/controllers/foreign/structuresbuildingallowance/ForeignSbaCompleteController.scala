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

import controllers.ControllerUtils.statusForPage
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.exceptions.InternalErrorFailure
import forms.foreign.structurebuildingallowance.ForeignSbaCompleteFormProvider
import models.JourneyPath.ForeignStructureBuildingAllowance
import models.{JourneyContext, NormalMode}
import navigation.ForeignPropertyNavigator
import pages.foreign.structurebuildingallowance.ForeignSbaCompletePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import service.JourneyAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.foreign.structurebuildingallowance.ForeignSbaCompleteView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignSbaCompleteController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  foreignPropertyNavigator: ForeignPropertyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ForeignSbaCompleteFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ForeignSbaCompleteView,
  journeyAnswersService: JourneyAnswersService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val preparedForm = request.userAnswers.get(ForeignSbaCompletePage(countryCode)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, NormalMode, taxYear, countryCode))
    }

  def onSubmit(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, NormalMode, taxYear, countryCode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ForeignSbaCompletePage(countryCode), value))
              _              <- sessionRepository.set(updatedAnswers)
              status <- journeyAnswersService
                          .setForeignStatus(
                            JourneyContext(
                              taxYear = taxYear,
                              mtditid = request.user.mtditid,
                              nino = request.user.nino,
                              journeyPath = ForeignStructureBuildingAllowance
                            ),
                            status = statusForPage(value),
                            request.user,
                            countryCode
                          )
                          .flatMap {
                            case Right(_) =>
                              Future.successful(
                                Redirect(
                                  foreignPropertyNavigator
                                    .nextPage(
                                      ForeignSbaCompletePage(countryCode),
                                      taxYear,
                                      NormalMode,
                                      request.userAnswers,
                                      updatedAnswers
                                    )
                                )
                              )
                            case Left(_) =>
                              Future.failed(
                                InternalErrorFailure(s"Failed to save the status for SBA section in tax year: $taxYear")
                              )
                          }
            } yield status
        )
    }
}
