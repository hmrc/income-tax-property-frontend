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

import audit.{AuditModel, AuditService}
import controllers.actions._
import controllers.exceptions.InternalErrorFailure
import models._
import models.backend.PropertyDetails
import models.requests.DataRequest
import pages.foreign.Country
import pages.propertyrentals.expenses.ConsolidatedExpensesPage
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.{BusinessService, PropertySubmissionService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.propertyrentals.expenses._
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
  view: RentalsAndRaRExpensesCheckYourAnswersView,
  businessService: BusinessService,
  auditService: AuditService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with SummaryListFluency with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val rows: Seq[SummaryListRow] = generateRowsForRentalsRentARoomExpenses(taxYear, request)
      val list = SummaryListViewModel(rows = rows)
      Ok(view(list, taxYear))

  }

  private def generateRowsForRentalsRentARoomExpenses(taxYear: Int, request: DataRequest[AnyContent])(implicit
    messages: Messages
  ): Seq[SummaryListRow] = {
    val consolidatedExpensesRows = request.userAnswers.get(ConsolidatedExpensesPage(RentalsRentARoom)) match {
      case Some(_) =>
        ConsolidatedExpensesSummary
          .rows(taxYear, request.userAnswers, request.user.isAgentMessageKey, RentalsRentARoom)
          .getOrElse(Seq.empty)
      case None => Seq.empty
    }
    consolidatedExpensesRows ++ individualForRentalsRentARoomExpenses(taxYear, request).flatten
  }

  private def individualForRentalsRentARoomExpenses(taxYear: Int, request: DataRequest[AnyContent])(implicit
    messages: Messages
  ) =
    Seq(
      RentsRatesAndInsuranceSummary.row(taxYear, request.userAnswers, request.user.isAgentMessageKey, RentalsRentARoom),
      RepairsAndMaintenanceCostsSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
      LoanInterestSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
      OtherProfessionalFeesSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
      CostsOfServicesProvidedSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
      PropertyBusinessTravelCostsSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
      OtherAllowablePropertyExpensesSummary.row(taxYear, request.userAnswers, RentalsRentARoom)
    )

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val context =
        JourneyContext(taxYear, request.user.mtditid, request.user.nino, JourneyPath.RentalsAndRentARoomExpenses)
      businessService
        .getUkPropertyDetails(request.user.nino, request.user.mtditid)
        .flatMap {
          case Right(Some(propertyDetails)) => saveExpenses(request, context, propertyDetails)
          case Left(_) =>
            logger.error("CashOrAccruals information could not be retrieved from downstream.")
            Future.failed(InternalErrorFailure("CashOrAccruals information could not be retrieved from downstream."))
        }

  }

  private def saveExpenses(
    request: DataRequest[AnyContent],
    context: JourneyContext,
    propertyDetails: PropertyDetails
  )(implicit
    hc: HeaderCarrier
  ) =
    request.userAnswers.get(RentalsAndRentARoomExpenses) match {
      case Some(rentalsRentARoomExpenses) =>
        propertySubmissionService
          .saveJourneyAnswers(context, rentalsRentARoomExpenses, propertyDetails.incomeSourceId)
          .flatMap {
            case Right(_) =>
              auditExpensesCYA(
                context.taxYear,
                request,
                rentalsRentARoomExpenses,
                isFailed = false,
                propertyDetails.accrualsOrCash.get
              )
              Future.successful(Redirect(routes.RentalsRaRExpensesCompleteController.onPageLoad(context.taxYear)))
            case Left(_) =>
              auditExpensesCYA(
                context.taxYear,
                request,
                rentalsRentARoomExpenses,
                isFailed = true,
                propertyDetails.accrualsOrCash.get
              )
              Future.failed(InternalErrorFailure("Failed to save Rentals and Rent a Room Expenses section."))
          }
      case None =>
        logger.error("RentalsAndRentARoomExpenses section is not present in userAnswers")
        Future.failed(InternalErrorFailure("RentalsAndRentARoomExpenses section is not present in userAnswers"))
    }

  private def auditExpensesCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    rentalsAndRentARoomExpenses: RentalsAndRentARoomExpenses,
    isFailed: Boolean,
    accrualsOrCash: Boolean
  )(implicit
    hc: HeaderCarrier
  ): Unit = {
    val auditModel = AuditModel(
      userType = request.user.affinityGroup,
      nino = request.user.nino,
      mtdItId = request.user.mtditid,
      taxYear = taxYear,
      propertyType = AuditPropertyType.UKProperty,
      countryCode = Country.UK.code,
      journeyName = JourneyName.RentalsRentARoom,
      sectionName = SectionName.Expenses,
      accountingMethod = if (accrualsOrCash) AccountingMethod.Traditional else AccountingMethod.Cash,
      isUpdate = false,
      isFailed = isFailed,
      agentReferenceNumber = request.user.agentRef,
      userEnteredDetails = rentalsAndRentARoomExpenses
    )
    auditService.sendAuditEvent(auditModel)
  }
}
