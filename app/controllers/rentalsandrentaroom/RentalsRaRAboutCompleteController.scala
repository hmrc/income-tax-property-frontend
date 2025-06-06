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

package controllers.rentalsandrentaroom

import controllers.ControllerUtils.statusForPage
import controllers.actions._
import controllers.statusError
import forms.rentalsandrentaroom.RentalsAndRaRAboutCompleteFormProvider
import models.JourneyPath.PropertyRentalsAndRentARoomAbout
import models.{JourneyContext, Mode, RentalsRentARoom}
import navigation.Navigator
import pages.UKPropertySelectPage
import pages.rentalsandrentaroom.RentalsRaRAboutCompletePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import service.JourneyAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rentalsandrentaroom.RentalsRaRAboutCompleteView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RentalsRaRAboutCompleteController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  journeyAnswersService: JourneyAnswersService,
  formProvider: RentalsAndRaRAboutCompleteFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RentalsRaRAboutCompleteView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(RentalsRaRAboutCompletePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, taxYear, mode))
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, taxYear, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(RentalsRaRAboutCompletePage, value))
              _              <- sessionRepository.set(updatedAnswers)
              status <- journeyAnswersService.setUKPropertyStatus(
                          JourneyContext(
                            taxYear = taxYear,
                            mtditid = request.user.mtditid,
                            nino = request.user.nino,
                            journeyPath = PropertyRentalsAndRentARoomAbout
                          ),
                          status = statusForPage(value),
                          request.user
                        )
            } yield status.fold(
              _ =>
                statusError(
                  journeyName = "about",
                  propertyType = RentalsRentARoom,
                  user = request.user,
                  taxYear = taxYear
                ),
              _ =>
                Redirect(navigator.nextPage(UKPropertySelectPage, taxYear, mode, request.userAnswers, updatedAnswers))
            )
        )
  }
}
