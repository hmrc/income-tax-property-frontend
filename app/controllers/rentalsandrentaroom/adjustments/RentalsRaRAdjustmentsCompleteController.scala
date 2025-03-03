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

package controllers.rentalsandrentaroom.adjustments

import controllers.ControllerUtils.statusForPage
import controllers.actions._
import forms.rentalsandrentaroom.adjustments.RentalsRaRAdjustmentsCompleteFormProvider
import models.JourneyContext
import models.JourneyPath.PropertyRentalsAndRentARoomAdjustments
import pages.rentalsandrentaroom.adjustments.RentalsRaRAdjustmentsCompletePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import service.JourneyAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rentalsandrentaroom.adjustments.RentalsRaRAdjustmentsCompleteView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RentalsRaRAdjustmentsCompleteController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: RentalsRaRAdjustmentsCompleteFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RentalsRaRAdjustmentsCompleteView,
  journeyAnswersService: JourneyAnswersService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(RentalsRaRAdjustmentsCompletePage) match {
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
              updatedAnswers <- Future.fromTry(request.userAnswers.set(RentalsRaRAdjustmentsCompletePage, value))
              _              <- sessionRepository.set(updatedAnswers)
              _ <- journeyAnswersService
                     .setStatus(
                       JourneyContext(
                         taxYear = taxYear,
                         mtditid = request.user.mtditid,
                         nino = request.user.nino,
                         journeyPath = PropertyRentalsAndRentARoomAdjustments
                       ),
                       status = statusForPage(value),
                       request.user
                     )
            } yield Redirect(controllers.routes.SummaryController.show(taxYear))
        )
  }
}
