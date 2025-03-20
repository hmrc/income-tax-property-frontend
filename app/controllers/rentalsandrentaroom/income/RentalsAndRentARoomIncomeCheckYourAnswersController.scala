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

import audit.{AuditModel, AuditService}
import controllers.actions._
import controllers.exceptions.InternalErrorFailure
import models.backend.PropertyDetails
import models.requests.DataRequest
import models.{AccountingMethod, AuditPropertyType, JourneyContext, JourneyName, JourneyPath, RentalsAndRentARoomIncome, RentalsRentARoom, SectionName}
import pages.foreign.Country
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.{BusinessService, PropertySubmissionService}
import uk.gov.hmrc.http.HeaderCarrier
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
  businessService: BusinessService,
  val controllerComponents: MessagesControllerComponents,
  view: RentalsAndRentARoomIncomeCheckYourAnswersView,
  auditService: AuditService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          IsNonUKLandlordSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          DeductingTaxSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          PremiumForLeaseSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          CalculatedFigureYourselfSummary.row(taxYear, request.userAnswers, RentalsRentARoom, request.user.isAgentMessageKey),
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
      request.userAnswers.get(RentalsAndRentARoomIncome) match {
        case Some(propertyRentalsIncome) =>
          saveIncome(taxYear, request, propertyRentalsIncome)
        case None =>
          logger.error("RentalsAndRentARoomIncome section is not present in userAnswers")
          Future.failed(InternalErrorFailure("RentalsAndRentARoomIncome section is not present in userAnswers"))
      }
  }

  private def saveIncome(
    taxYear: Int,
    request: DataRequest[AnyContent],
    propertyRentalsIncome: RentalsAndRentARoomIncome
  )(implicit
    hc: HeaderCarrier
  ) = {
    val context =
      JourneyContext(taxYear, request.user.mtditid, request.user.nino, JourneyPath.RentalsAndRentARoomIncome)
    propertySubmissionService
      .saveJourneyAnswers(context, propertyRentalsIncome)
      .flatMap {
        case Right(_) =>
          auditIncomeCYA(taxYear, request, propertyRentalsIncome, isFailed = false)
          Future.successful(
            Redirect(
              controllers.rentalsandrentaroom.income.routes.RentalsRaRIncomeCompleteController.onPageLoad(taxYear)
            )
          )
        case Left(_) =>
          auditIncomeCYA(taxYear, request, propertyRentalsIncome, isFailed = true)
          Future.failed(InternalErrorFailure("Property submission save error"))
      }
  }

  private def auditIncomeCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    income: RentalsAndRentARoomIncome,
    isFailed: Boolean
  )(implicit
    hc: HeaderCarrier
  ): Future[Unit] =
    businessService
      .getUkPropertyDetails(request.user.nino, request.user.mtditid)
      .map {
        case Right(Some(PropertyDetails(_, _, Some(accrualsOrCash), _))) =>
          val auditModel = AuditModel(
            userType = request.user.affinityGroup,
            nino = request.user.nino,
            mtdItId = request.user.mtditid,
            taxYear = taxYear,
            propertyType = AuditPropertyType.UKProperty,
            countryCode = Country.UK.code,
            journeyName = JourneyName.RentalsRentARoom,
            sectionName = SectionName.Income,
            accountingMethod = if (accrualsOrCash) AccountingMethod.Traditional else AccountingMethod.Cash,
            isUpdate = false,
            isFailed = isFailed,
            agentReferenceNumber = request.user.agentRef,
            userEnteredDetails = income
          )
          auditService.sendAuditEvent(auditModel)
        case Left(_) => logger.error("CashOrAccruals information could not be retrieved from downstream.")
      }

}
