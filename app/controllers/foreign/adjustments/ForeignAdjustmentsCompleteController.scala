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

import controllers.ControllerUtils.statusForPage
import controllers.actions._
import forms.foreign.adjustments.ForeignAdjustmentsCompleteFormProvider
import models.JourneyPath.ForeignPropertyAdjustments
import models.{JourneyContext, NormalMode}
import navigation.ForeignPropertyNavigator
import pages.foreign.adjustments.ForeignAdjustmentsCompletePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import service.JourneyAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.foreign.adjustments.ForeignAdjustmentsCompleteView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignAdjustmentsCompleteController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  foreignPropertyNavigator: ForeignPropertyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  journeyAnswersService: JourneyAnswersService,
  formProvider: ForeignAdjustmentsCompleteFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ForeignAdjustmentsCompleteView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val preparedForm = request.userAnswers.get(ForeignAdjustmentsCompletePage(countryCode)) match {
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
              updatedAnswers <-
                Future.fromTry(request.userAnswers.set(ForeignAdjustmentsCompletePage(countryCode), value))
              _ <- sessionRepository.set(updatedAnswers)
              status <- journeyAnswersService.setForeignStatus(
                          ctx = JourneyContext(
                            taxYear = taxYear,
                            mtditid = request.user.mtditid,
                            nino = request.user.nino,
                            journeyPath = ForeignPropertyAdjustments
                          ),
                          status = statusForPage(value),
                          user = request.user,
                          countryCode = countryCode
                        )
            } yield status.fold(
              _ =>
                // TODO: When we implement navigation story, update the route to show message from backend or error
                Redirect(controllers.routes.SummaryController.show(taxYear)),
              _ =>
                Redirect(
                  foreignPropertyNavigator.nextPage(
                    ForeignAdjustmentsCompletePage(countryCode),
                    taxYear,
                    NormalMode,
                    request.userAnswers,
                    updatedAnswers
                  )
                )
            )
        )
    }
}
