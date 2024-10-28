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

package controllers.enhancedstructuresbuildingallowance

import controllers.ControllerUtils.statusForPage
import controllers.actions._
import controllers.exceptions.InternalErrorFailure
import forms.enhancedstructuresbuildingallowance.EsbaSectionFinishedFormProvider
import models.JourneyPath.{PropertyRentalsAndRentARoomESBA, RentalESBA}
import models.requests.DataRequest
import models.{JourneyContext, NormalMode, PropertyType, Rentals}
import navigation.Navigator
import pages.enhancedstructuresbuildingallowance.EsbaSectionFinishedPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import service.JourneyAnswersService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.enhancedstructuresbuildingallowance.EsbaSectionFinishedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EsbaSectionFinishedController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: EsbaSectionFinishedFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: EsbaSectionFinishedView,
  journeyAnswersService: JourneyAnswersService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(taxYear: Int, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val preparedForm = request.userAnswers.get(EsbaSectionFinishedPage(propertyType)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, NormalMode, taxYear, propertyType))
    }

  def onSubmit(taxYear: Int, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, NormalMode, taxYear, propertyType))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(EsbaSectionFinishedPage(propertyType), value))
              _              <- sessionRepository.set(updatedAnswers)
              _              <- saveStatus(taxYear, request, value, propertyType)
            } yield Redirect(
              navigator.nextPage(
                EsbaSectionFinishedPage(propertyType),
                taxYear,
                NormalMode,
                request.userAnswers,
                updatedAnswers
              )
            )
        )
    }

  private def saveStatus(taxYear: Int, request: DataRequest[AnyContent], value: Boolean, propertyType: PropertyType)(
    implicit hc: HeaderCarrier
  ): Future[Result] = {
    val sectionPath = if (propertyType == Rentals) RentalESBA else PropertyRentalsAndRentARoomESBA
    val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, sectionPath)

    journeyAnswersService
      .setStatus(
        context,
        statusForPage(value),
        request.user
      )
      .flatMap {
        case Right(_) => Future.successful(Redirect(controllers.routes.SummaryController.show(taxYear)))
        case Left(_) =>
          Future.failed(
            InternalErrorFailure(s"Failed to save the status for the ESBA section in tax year: $taxYear")
          )
      }
  }

}
