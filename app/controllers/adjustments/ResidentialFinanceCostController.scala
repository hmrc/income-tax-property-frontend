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

package controllers.adjustments

import controllers.actions._
import forms.adjustments.ResidentialFinanceCostFormProvider
import models.{Mode, PropertyType, UserAnswers}
import navigation.{Navigator, UkAndForeignPropertyNavigator}
import pages.{Page, isUkAndForeignAboutJourneyComplete}
import pages.adjustments.ResidentialFinanceCostPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import service.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.adjustments.ResidentialFinanceCostView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ResidentialFinanceCostController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  ukAndForeignNavigator: UkAndForeignPropertyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ResidentialFinanceCostFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ResidentialFinanceCostView,
  sessionService: SessionService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, mode: Mode, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData) { implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      if (request.userAnswers.isEmpty) {
        sessionService.createNewEmptySession(request.userId)
      }
      val preparedForm =
        request.userAnswers.getOrElse(UserAnswers(request.userId)).get(ResidentialFinanceCostPage(propertyType)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

      Ok(view(preparedForm, mode, taxYear, request.user.isAgentMessageKey, propertyType))
    }

  def onSubmit(taxYear: Int, mode: Mode, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(view(formWithErrors, mode, taxYear, request.user.isAgentMessageKey, propertyType))
            ),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ResidentialFinanceCostPage(propertyType), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              nextLocation(
                ResidentialFinanceCostPage(propertyType),
                taxYear: Int,
                mode,
                request.userAnswers,
                updatedAnswers
              )
            )
        )
    }

  private def nextLocation(
     page: Page,
     taxYear: Int,
     mode: Mode,
     userAnswers: UserAnswers,
     updatedAnswers: UserAnswers
  ): Call =
    if (isUkAndForeignAboutJourneyComplete(userAnswers)) {
      ukAndForeignNavigator.nextPage(page, taxYear, mode, userAnswers, updatedAnswers)
    } else {
      navigator
        .nextPage(page, taxYear, mode, userAnswers, updatedAnswers)
    }
}
