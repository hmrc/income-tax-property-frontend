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

package controllers.propertyrentals.income

import audit.{AuditModel, AuditService, RentalsIncome}
import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.exceptions.{InternalErrorFailure, SaveJourneyAnswersFailed}
import models.JourneyPath.RentalIncome
import models._
import models.requests.DataRequest
import pages.PageConstants.incomePath
import pages.foreign.Country
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.premiumlease._
import viewmodels.checkAnswers.propertyrentals.income._
import viewmodels.govuk.summarylist._
import views.html.propertyrentals.income.IncomeCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class PropertyIncomeCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  propertySubmissionService: PropertySubmissionService,
  view: IncomeCheckYourAnswersView,
  audit: AuditService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          IsNonUKLandlordSummary.row(taxYear, request.userAnswers, Rentals),
          IncomeFromPropertySummary.row(taxYear, request.userAnswers, Rentals),
          DeductingTaxSummary.row(taxYear, request.userAnswers, Rentals),
          PremiumForLeaseSummary.row(taxYear, request.userAnswers, Rentals),
          CalculatedFigureYourselfSummary.row(taxYear, request.userAnswers, Rentals, request.user.isAgentMessageKey),
          ReceivedGrantLeaseAmountSummary.row(taxYear, request.userAnswers, Rentals),
          YearLeaseAmountSummary.row(taxYear, request.userAnswers, Rentals),
          ReversePremiumsReceivedSummary.row(taxYear, request.userAnswers, Rentals),
          PremiumsGrantLeaseSummary.row(taxYear, request.userAnswers, Rentals),
          OtherIncomeFromPropertySummary.row(taxYear, request.userAnswers, Rentals)
        ).flatten
      )

      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(RentalsIncome) match {
        case Some(propertyRentalsIncome) => saveIncomeSection(taxYear, request, propertyRentalsIncome)
        case None =>
          logger.error(s"${incomePath(Rentals)} section is not present in userAnswers")
          Future.failed(InternalErrorFailure(s"${incomePath(Rentals)} section is not present in userAnswers"))
      }
  }

  private def saveIncomeSection(taxYear: Int, request: DataRequest[AnyContent], propertyRentalsIncome: RentalsIncome)(
    implicit hc: HeaderCarrier
  ) = {
    val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, RentalIncome)
    propertySubmissionService.savePropertyRentalsIncome(context, propertyRentalsIncome).flatMap {
      case Right(_) =>
        auditCYA(context.taxYear, request, propertyRentalsIncome, isFailed = false, AccountingMethod.Traditional)
        Future.successful(
          Redirect(controllers.propertyrentals.income.routes.IncomeSectionFinishedController.onPageLoad(taxYear))
        )
      case Left(error) =>
        logger.error(s"Failed to to save Rentals Income: ${error.toString}")
        auditCYA(context.taxYear, request, propertyRentalsIncome, isFailed = true, AccountingMethod.Traditional)
        Future.failed(SaveJourneyAnswersFailed("Failed to to save Rentals Income"))
    }
  }

  private def auditCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    propertyRentalsIncome: RentalsIncome,
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
      propertyRentalsIncome
    )

    audit.sendRentalsAuditEvent(auditModel)
  }
}
