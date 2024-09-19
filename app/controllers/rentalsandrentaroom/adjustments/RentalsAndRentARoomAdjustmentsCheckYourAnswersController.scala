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

import controllers.actions._
import controllers.exceptions.InternalErrorFailure
import models.backend.PropertyDetails
import models.requests.DataRequest
import models.{JourneyContext, RentalsAndRentARoomAdjustment, RentalsRentARoom}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.{BusinessService, PropertySubmissionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.adjustments._
import viewmodels.govuk.SummaryListFluency
import views.html.rentalsandrentaroom.adjustments.RentalsAndRentARoomAdjustmentsCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
class RentalsAndRentARoomAdjustmentsCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  propertySubmissionService: PropertySubmissionService,
  val controllerComponents: MessagesControllerComponents,
  view: RentalsAndRentARoomAdjustmentsCheckYourAnswersView,
  businessService: BusinessService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with SummaryListFluency with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          PrivateUseAdjustmentSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          BalancingChargeSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          PropertyIncomeAllowanceSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          BusinessPremisesRenovationAllowanceBalancingChargeSummary.row(taxYear, request.userAnswers),
          ResidentialFinanceCostSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          UnusedResidentialFinanceCostSummary.row(taxYear, request.userAnswers, RentalsRentARoom)
        ).flatten
      )

      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val context =
        JourneyContext(taxYear, request.user.mtditid, request.user.nino, "rentals-and-rent-a-room-adjustments")

      businessService
        .getUkPropertyDetails(request.user.nino, request.user.mtditid)
        .flatMap {
          case Left(_) =>
            logger.error(
              s"Failed to retrieve property details for user with nino ${request.user.nino} and mrditid ${request.user.mtditid}"
            )
            Future.failed(
              InternalErrorFailure(
                s"Failed to retrieve property details for user with nino ${request.user.nino} and mrditid ${request.user.mtditid}"
              )
            )
          case Right(Some(details)) => saveAdjustments(request, context, details)
        }
  }

  private def saveAdjustments(
    request: DataRequest[AnyContent],
    context: JourneyContext,
    details: PropertyDetails
  )(implicit
    hc: HeaderCarrier
  ) =
    request.userAnswers.get(RentalsAndRentARoomAdjustment) match {
      case Some(rentalsRentARoomAdjustments) =>
        propertySubmissionService
          .saveJourneyAnswers(context, rentalsRentARoomAdjustments, details.incomeSourceId)
          .flatMap {
            case Right(_) =>
              Future.successful(Redirect(controllers.routes.SummaryController.show(context.taxYear)))
            case Left(_) =>
              Future.failed(InternalErrorFailure("Failed to save Rentals and Rent a Room Adjustments section."))
          }
      case None =>
        logger.error("RentalsAndRentARoomAdjustments section is not present in userAnswers")
        Future.failed(InternalErrorFailure("RentalsAndRentARoomAdjustments section is not present in userAnswers"))
    }
}
