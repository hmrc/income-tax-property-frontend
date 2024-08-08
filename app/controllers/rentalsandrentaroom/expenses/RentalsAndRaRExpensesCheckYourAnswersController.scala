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

package controllers.rentalsandrentaroom.expenses

import controllers.actions._
import controllers.exceptions.{InternalErrorFailure, NotFoundException}
import models.{JourneyContext, RentalsAndRentARoomExpenses, RentalsRentARoom}
import models.requests.DataRequest
import pages.propertyrentals.expenses.ConsolidatedExpensesPage
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.PropertySubmissionService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.propertyrentals.expenses.{ConsolidatedExpensesSummary, CostsOfServicesProvidedSummary, LoanInterestSummary, OtherAllowablePropertyExpensesSummary, OtherProfessionalFeesSummary, PropertyBusinessTravelCostsSummary, RentsRatesAndInsuranceSummary, RepairsAndMaintenanceCostsSummary}
import viewmodels.govuk.SummaryListFluency
import views.html.rentalsandrentaroom.expenses.RentalsAndRaRExpensesCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RentalsAndRaRExpensesCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  propertySubmissionService: PropertySubmissionService,
  val controllerComponents: MessagesControllerComponents,
  view: RentalsAndRaRExpensesCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with SummaryListFluency with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val rows: Seq[SummaryListRow] = generateRows(taxYear, request)
      val list = SummaryListViewModel(rows = rows)
      Ok(view(list, taxYear))

  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val context =
        JourneyContext(taxYear, request.user.mtditid, request.user.nino, "rentals-and-rent-a-room-expenses")

      request.userAnswers.get(RentalsAndRentARoomExpenses) match {
        case Some(propertyRentalsExpenses) =>
          propertySubmissionService
            .saveJourneyAnswers(context, propertyRentalsExpenses)
            .flatMap {
              case Right(_) =>
                Future.successful(
                  Redirect(
                    controllers.rentalsandrentaroom.expenses.routes.RentalsAndRaRExpensesCheckYourAnswersController
                      .onPageLoad(taxYear)
                  )
                )
              case Left(_) =>
                Future.failed(InternalErrorFailure("Property submission save error"))
            }

        case None =>
          logger.error("RentalsAndRentARoomExpenses section is not present in userAnswers")
          Future.failed(InternalErrorFailure("RentalsAndRentARoomExpenses section is not present in userAnswers"))
      }

  }
  private def generateRows(taxYear: Int, request: DataRequest[AnyContent])(implicit
    messages: Messages
  ): Seq[SummaryListRow] = {
    val consolidatedExpensesRows = request.userAnswers.get(ConsolidatedExpensesPage(RentalsRentARoom)) match {
      case Some(_) =>
        ConsolidatedExpensesSummary
          .rows(taxYear, request.userAnswers, request.user.isAgentMessageKey)
          .getOrElse(Seq.empty)
      case None => Seq.empty
    }
    consolidatedExpensesRows ++ individualExpenses(taxYear, request).flatten
  }

  private def individualExpenses(taxYear: Int, request: DataRequest[AnyContent])(implicit messages: Messages) =
    Seq(
      RentsRatesAndInsuranceSummary.row(taxYear, request.userAnswers),
      RepairsAndMaintenanceCostsSummary.row(taxYear, request.userAnswers),
      LoanInterestSummary.row(taxYear, request.userAnswers),
      OtherProfessionalFeesSummary.row(taxYear, request.userAnswers),
      CostsOfServicesProvidedSummary.row(taxYear, request.userAnswers),
      PropertyBusinessTravelCostsSummary.row(taxYear, request.userAnswers),
      OtherAllowablePropertyExpensesSummary.row(taxYear, request.userAnswers)
    )
}
