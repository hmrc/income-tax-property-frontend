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

package controllers.rentalsandrentaroom.income

import controllers.actions._
import controllers.exceptions.InternalErrorFailure
import models.{JourneyContext, RentalsAndRentARoomIncome, RentalsRentARoom}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.PropertySubmissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.premiumlease._
import viewmodels.checkAnswers.propertyrentals.income._
import viewmodels.govuk.summarylist._
import views.html.rentalsandrentaroom.income.RentalsAndRentARoomIncomeCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RentalsAndRentARoomIncomeCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  propertySubmissionService: PropertySubmissionService,
  val controllerComponents: MessagesControllerComponents,
  view: RentalsAndRentARoomIncomeCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          IsNonUKLandlordSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          IncomeFromPropertySummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          DeductingTaxSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          LeasePremiumPaymentSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          CalculatedFigureYourselfSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          ReceivedGrantLeaseAmountSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          YearLeaseAmountSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          ReversePremiumsReceivedSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          PremiumsGrantLeaseSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          OtherIncomeFromPropertySummary.row(taxYear, request.userAnswers, RentalsRentARoom)
        ).flatten
      )

      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val context =
        JourneyContext(taxYear, request.user.mtditid, request.user.nino, "rentals-and-rent-a-room-income")

      request.userAnswers.get(RentalsAndRentARoomIncome) match {
        case Some(propertyRentalsIncome) =>
          propertySubmissionService
            .saveJourneyAnswers(context, propertyRentalsIncome)
            .flatMap {
              case Right(_) =>
                Future.successful(
                  Redirect(
                    controllers.rentalsandrentaroom.income.routes.RentalsRaRIncomeCompleteController.onPageLoad(taxYear)
                  )
                )
              case Left(_) =>
                Future.failed(InternalErrorFailure("Property submission save error"))
            }

        case None =>
          logger.error("RentalsAndRentARoomIncome section is not present in userAnswers")
          Future.failed(InternalErrorFailure("RentalsAndRentARoomIncome section is not present in userAnswers"))
      }

  }
}
