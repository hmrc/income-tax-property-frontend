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

package controllers.propertyrentals.expenses

import audit.{AuditModel, AuditService, RentalsExpense}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.JourneyPath.PropertyRentalExpenses
import models._
import models.requests.DataRequest
import pages.foreign.Country
import pages.propertyrentals.expenses.ConsolidatedExpensesPage
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.PropertySubmissionService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.propertyrentals.expenses._
import viewmodels.govuk.summarylist._
import views.html.propertyrentals.expenses.ExpensesCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ExpensesCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ExpensesCheckYourAnswersView,
  audit: AuditService,
  propertySubmissionService: PropertySubmissionService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val rows: Seq[SummaryListRow] = generateRows(taxYear, request)
      val list = SummaryListViewModel(rows = rows)
      Ok(view(list, taxYear))
  }

  private def generateRows(taxYear: Int, request: DataRequest[AnyContent])(implicit
    messages: Messages
  ): Seq[SummaryListRow] = {
    val consolidatedExpensesRows = request.userAnswers.get(ConsolidatedExpensesPage(Rentals)) match {
      case Some(_) =>
        ConsolidatedExpensesSummary
          .rows(taxYear, request.userAnswers, request.user.isAgentMessageKey, Rentals)
          .getOrElse(Seq.empty)
      case None => Seq.empty
    }
    consolidatedExpensesRows ++ individualExpenses(taxYear, request).flatten
  }

  private def individualExpenses(taxYear: Int, request: DataRequest[AnyContent])(implicit messages: Messages) =
    Seq(
      RentsRatesAndInsuranceSummary.row(taxYear, request.userAnswers, request.user.isAgentMessageKey, Rentals),
      RepairsAndMaintenanceCostsSummary.row(taxYear, request.userAnswers, Rentals),
      LoanInterestSummary.row(taxYear, request.userAnswers, Rentals),
      OtherProfessionalFeesSummary.row(taxYear, request.userAnswers, Rentals),
      CostsOfServicesProvidedSummary.row(taxYear, request.userAnswers, Rentals),
      PropertyBusinessTravelCostsSummary.row(taxYear, request.userAnswers, Rentals),
      OtherAllowablePropertyExpensesSummary.row(taxYear, request.userAnswers, Rentals)
    )

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers
        .get(RentalsExpense)
        .map(propertyRentalsExpense => saveExpenses(taxYear, request, propertyRentalsExpense))
        .getOrElse {
          logger.error("Property Rentals Expense section is not present in userAnswers")
          Future.failed(NotFoundException)
        }
  }

  private def saveExpenses(taxYear: Int, request: DataRequest[AnyContent], expenses: RentalsExpense)(implicit
    hc: HeaderCarrier
  ): Future[Result] = {
    val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, PropertyRentalExpenses)

    propertySubmissionService.saveJourneyAnswers(context, expenses).flatMap {
      case Right(_) =>
        auditCYA(taxYear, request, expenses, isFailed = false, AccountingMethod.Traditional)
        Future.successful(Redirect(routes.ExpensesSectionFinishedController.onPageLoad(taxYear)))
      case Left(error) =>
        logger.error(s"Failed to to save Rentals Expenses: ${error.toString}")
        auditCYA(taxYear, request, expenses, isFailed = true, AccountingMethod.Traditional)
        Future.failed(ExpensesSaveFailed)
    }
  }

  private def auditCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    propertyRentalsExpense: RentalsExpense,
    isFailed: Boolean,
    accountingMethod: AccountingMethod
  )(implicit
    hc: HeaderCarrier
  ): Unit = {
    val auditModel = AuditModel(
      request.user.affinityGroup,
      request.user.nino,
      request.user.mtditid,
      taxYear,
      propertyType = AuditPropertyType.UKProperty,
      countryCode = Country.UK.code,
      journeyName = JourneyName.Rentals,
      sectionName = SectionName.Expenses,
      accountingMethod = accountingMethod,
      isUpdate = false,
      isFailed = isFailed,
      agentReferenceNumber = request.user.agentRef,
      propertyRentalsExpense
    )

    audit.sendRentalsAuditEvent(auditModel)
  }
}

case object NotFoundException extends Exception("Rentals Expenses Section is not present in userAnswers")

case object ExpensesSaveFailed extends Exception("Failed to to save Rentals Expenses")
