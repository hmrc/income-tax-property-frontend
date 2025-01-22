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

package controllers.foreign

import controllers.ControllerUtils.statusForPage
import controllers.actions._
import controllers.statusError
import forms.foreign.ForeignSelectCountriesCompleteFormProvider
import models.JourneyPath.ForeignSelectCountry
import models.{ForeignProperty, JourneyContext, NormalMode}
import navigation.ForeignPropertyNavigator
import pages.foreign.ForeignSelectCountriesCompletePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import service.JourneyAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.foreign.ForeignSelectCountriesCompleteView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignSelectCountriesCompleteController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  foreignPropertyNavigator: ForeignPropertyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ForeignSelectCountriesCompleteFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ForeignSelectCountriesCompleteView,
  journeyAnswersService: JourneyAnswersService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(ForeignSelectCountriesCompletePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, taxYear))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ForeignSelectCountriesCompletePage, value))
              _              <- sessionRepository.set(updatedAnswers)
              status <- journeyAnswersService
                          .setForeignStatus(
                            JourneyContext(
                              taxYear = taxYear,
                              mtditid = request.user.mtditid,
                              nino = request.user.nino,
                              journeyPath = ForeignSelectCountry
                            ),
                            status = statusForPage(value),
                            request.user,
                            ""
                          )
            } yield status.fold(
              _ =>
                statusError(
                  journeyName = "foreign-property-select-country",
                  propertyType = ForeignProperty,
                  user = request.user,
                  taxYear = taxYear
                ),
              _ =>
                Redirect(
                  foreignPropertyNavigator
                    .nextPage(
                      ForeignSelectCountriesCompletePage,
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
