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

package controllers.foreign.expenses

import controllers.actions._
import forms.foreign.expenses.ForeignRentsRatesAndInsuranceFormProvider
import models.Mode
import navigation.ForeignPropertyNavigator
import pages.foreign.expenses.{ForeignExpensesSectionAddCountryCode, ForeignRentsRatesAndInsurancePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.foreign.expenses.ForeignRentsRatesAndInsuranceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignRentsRatesAndInsuranceController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ForeignPropertyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ForeignRentsRatesAndInsuranceFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ForeignRentsRatesAndInsuranceView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, countryCode: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)

      val preparedForm = request.userAnswers.get(ForeignRentsRatesAndInsurancePage(countryCode)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, taxYear, countryCode, request.user.isAgentMessageKey, mode))
    }

  def onSubmit(taxYear: Int, countryCode: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future
              .successful(BadRequest(view(formWithErrors, taxYear, countryCode, request.user.isAgentMessageKey, mode))),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(request.userAnswers.set(ForeignRentsRatesAndInsurancePage(countryCode), value))
              updatedAnswersWithCountryCode <-
                Future.fromTry(updatedAnswers.set(ForeignExpensesSectionAddCountryCode(countryCode), countryCode))
              _ <- sessionRepository.set(updatedAnswersWithCountryCode)
            } yield Redirect(
              navigator.nextPage(
                ForeignRentsRatesAndInsurancePage(countryCode),
                taxYear,
                mode,
                request.userAnswers,
                updatedAnswers
              )
            )
        )
    }
}
