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

package controllers.foreign.income

import controllers.actions._
import forms.foreign.income.ForeignPropertyRentalIncomeFormProvider
import models.Mode
import navigation.ForeignPropertyNavigator
import pages.foreign.income.{ForeignIncomeSectionAddCountryCode, ForeignPropertyRentalIncomePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.foreign.income.ForeignPropertyRentalIncomeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignPropertyRentalIncomeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ForeignPropertyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ForeignPropertyRentalIncomeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ForeignPropertyRentalIncomeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, mode: Mode, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      val preparedForm = request.userAnswers.get(ForeignPropertyRentalIncomePage(countryCode)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, taxYear, request.user.isAgentMessageKey, mode, countryCode))
    }

  def onSubmit(taxYear: Int, mode: Mode, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future
              .successful(BadRequest(view(formWithErrors, taxYear, request.user.isAgentMessageKey, mode, countryCode))),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(request.userAnswers.set(ForeignPropertyRentalIncomePage(countryCode), value))
              updatedAnswersWithCountryCode <-
                Future.fromTry(updatedAnswers.set(ForeignIncomeSectionAddCountryCode(countryCode), countryCode))
              _ <- sessionRepository.set(updatedAnswersWithCountryCode)
            } yield Redirect(
              navigator.nextPage(
                ForeignPropertyRentalIncomePage(countryCode),
                taxYear,
                mode,
                request.userAnswers,
                updatedAnswers
              )
            )
        )
    }
}
