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

package controllers.ukrentaroom

import controllers.actions._
import forms.ukrentaroom.TotalIncomeAmountFormProvider
import models.{Mode, PropertyType, UserAnswers}
import navigation.{Navigator, UkAndForeignPropertyNavigator}
import pages.{Page, isUkAndForeignAboutJourneyComplete}
import pages.ukrentaroom.TotalIncomeAmountPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ukrentaroom.TotalIncomeAmountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TotalIncomeAmountController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  ukAndForeignNavigator: UkAndForeignPropertyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: TotalIncomeAmountFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: TotalIncomeAmountView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, mode: Mode, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      val preparedForm = request.userAnswers.get(TotalIncomeAmountPage(propertyType)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, taxYear, mode, request.user.isAgentMessageKey, propertyType))
    }

  def onSubmit(taxYear: Int, mode: Mode, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(view(formWithErrors, taxYear, mode, request.user.isAgentMessageKey, propertyType))
            ),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(TotalIncomeAmountPage(propertyType), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(next_location(TotalIncomeAmountPage(propertyType), taxYear, mode, request.userAnswers, updatedAnswers))
        )
    }

  private def next_location(
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
