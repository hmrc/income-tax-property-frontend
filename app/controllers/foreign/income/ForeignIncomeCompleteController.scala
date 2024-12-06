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

import controllers.ControllerUtils.statusForPage
import controllers.actions._
import controllers.statusError
import forms.foreign.income.ForeignIncomeSectionCompleteFormProvider
import models.JourneyPath.ForeignPropertyIncome
import models.{ForeignProperty, JourneyContext, NormalMode}
import navigation.ForeignPropertyNavigator
import pages.foreign.income.ForeignIncomeSectionCompletePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import service.JourneyAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.foreign.income.ForeignIncomeSectionCompleteView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignIncomeCompleteController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  foreignPropertyNavigator: ForeignPropertyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ForeignIncomeSectionCompleteFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ForeignIncomeSectionCompleteView,
  journeyAnswersService: JourneyAnswersService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val form = formProvider()

      val preparedForm = request.userAnswers.get(ForeignIncomeSectionCompletePage(countryCode)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, taxYear, countryCode))
    }

  def onSubmit(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form = formProvider()
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, taxYear, countryCode))),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(request.userAnswers.set(ForeignIncomeSectionCompletePage(countryCode), value))
              _ <- sessionRepository.set(updatedAnswers)
              status <- journeyAnswersService.setStatus(
                          JourneyContext(
                            taxYear = taxYear,
                            mtditid = request.user.mtditid,
                            nino = request.user.nino,
                            journeyPath = ForeignPropertyIncome
                          ),
                          status = statusForPage(value),
                          request.user
                        )
            } yield status.fold(
              _ =>
                statusError(
                  journeyName = "foreign-property-income",
                  propertyType = ForeignProperty,
                  user = request.user,
                  taxYear = taxYear
                ),
              _ =>
                Redirect(
                  foreignPropertyNavigator.nextPage(
                    ForeignIncomeSectionCompletePage(countryCode),
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
