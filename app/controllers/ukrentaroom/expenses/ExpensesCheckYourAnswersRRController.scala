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

package controllers.ukrentaroom.expenses

import audit.RentARoomExpenses
import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.propertyrentals.expenses.{ExpensesSaveFailed, NotFoundException}
import controllers.routes
import models.JourneyContext
import models.requests.DataRequest
import pages.ukrentaroom.expenses.ConsolidatedExpensesRRPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.PropertySubmissionService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.ukrentaroom.expenses._
import viewmodels.govuk.summarylist._
import views.html.ukrentaroom.expenses.ExpensesCheckYourAnswersRRView

import scala.concurrent.{ExecutionContext, Future}

class ExpensesCheckYourAnswersRRController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ExpensesCheckYourAnswersRRView,
  propertySubmissionService: PropertySubmissionService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val rows: Seq[SummaryListRow] =
        if (request.userAnswers.get(ConsolidatedExpensesRRPage).exists(_.areExpensesConsolidated)) {
          Seq(
            ConsolidatedExpensesRRSummary.row(taxYear, request.userAnswers)
          ).flatten
        } else {
          Seq(
            ConsolidatedExpensesRRSummary
              .row(taxYear, request.userAnswers),
            RentsRatesAndInsuranceRRSummary
              .row(taxYear, request.userAnswers),
            RepairsAndMaintenanceCostsRRSummary
              .row(taxYear, request.userAnswers),
            LegalManagementOtherFeeSummary
              .row(taxYear, request.userAnswers),
            CostOfServicesProvidedSummary
              .row(taxYear, request.userAnswers),
            ResidentialPropertyFinanceCostsSummary
              .row(taxYear, request.userAnswers, request.user.isAgentMessageKey),
            UnusedResidentialPropertyFinanceCostsBroughtFwdSummary
              .row(taxYear, request.userAnswers, request.user.isAgentMessageKey),
            OtherPropertyExpensesRRSummary.row(taxYear, request.userAnswers)
          ).flatten
        }
      val list = SummaryListViewModel(rows = rows)

      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers
        .get(RentARoomExpenses)
        .map(propertyRentalsExpense => saveRentARoomExpenses(taxYear, request, propertyRentalsExpense))
        .getOrElse {
          logger.error("Rent a room expenses section is not present in userAnswers")
          Future.failed(NotFoundException)
        }
  }

  private def saveRentARoomExpenses(
    taxYear: Int,
    request: DataRequest[AnyContent],
    rentARoomExpenses: RentARoomExpenses
  )(implicit
    hc: HeaderCarrier
  ): Future[Result] = {
    val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, "rent-a-room-expenses")
    propertySubmissionService.saveJourneyAnswers(context, rentARoomExpenses).flatMap {
      case Right(_) =>
        Future.successful(Redirect(routes.SummaryController.show(taxYear)))
      case Left(_) => Future.failed(ExpensesSaveFailed)
    }
  }
}
