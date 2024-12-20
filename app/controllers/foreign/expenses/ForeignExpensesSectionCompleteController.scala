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

import controllers.ControllerUtils.statusForPage
import controllers.actions._
import controllers.statusError
import forms.foreign.expenses.ForeignExpensesSectionCompleteFormProvider
import models.JourneyPath.ForeignPropertyExpenses
import models.{ForeignProperty, JourneyContext, NormalMode}
import navigation.ForeignPropertyNavigator
import pages.foreign.expenses.ForeignExpensesSectionCompletePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import service.JourneyAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.foreign.expenses.ForeignExpensesSectionCompleteView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignExpensesSectionCompleteController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ForeignPropertyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ForeignExpensesSectionCompleteFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ForeignExpensesSectionCompleteView,
  journeyAnswersService: JourneyAnswersService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val preparedForm = request.userAnswers.get(ForeignExpensesSectionCompletePage(countryCode)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, taxYear, countryCode))
    }

  def onSubmit(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, taxYear, countryCode))),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(request.userAnswers.set(ForeignExpensesSectionCompletePage(countryCode), value))
              _ <- sessionRepository.set(updatedAnswers)
              status <- journeyAnswersService.setForeignStatus(
                          JourneyContext(
                            taxYear = taxYear,
                            mtditid = request.user.mtditid,
                            nino = request.user.nino,
                            journeyPath = ForeignPropertyExpenses
                          ),
                          status = statusForPage(value),
                          request.user,
                          countryCode
                        )
            } yield status.fold(
              _ =>
                statusError(
                  journeyName = "foreign-property-expenses",
                  propertyType = ForeignProperty,
                  user = request.user,
                  taxYear = taxYear
                ),
              _ =>
                Redirect(
                  navigator.nextPage(
                    ForeignExpensesSectionCompletePage(countryCode),
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
