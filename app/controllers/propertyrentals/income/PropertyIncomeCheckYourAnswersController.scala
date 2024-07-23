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

import audit.{AuditService, RentalsAuditModel, RentalsIncome}
import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.requests.DataRequest
import models.{JourneyContext, Rentals}
import pages.PageConstants.incomePath
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
          IsNonUKLandlordSummary.row(taxYear, request.userAnswers),
          IncomeFromPropertySummary.row(taxYear, request.userAnswers, Rentals),
          DeductingTaxSummary.row(taxYear, request.userAnswers, Rentals),
          LeasePremiumPaymentSummary.row(taxYear, request.userAnswers, Rentals),
          CalculatedFigureYourselfSummary.row(taxYear, request.userAnswers, Rentals),
          ReceivedGrantLeaseAmountSummary.row(taxYear, request.userAnswers, Rentals),
          YearLeaseAmountSummary.row(taxYear, request.userAnswers),
          PremiumsGrantLeaseSummary.row(taxYear, request.userAnswers, Rentals),
          ReversePremiumsReceivedSummary.row(taxYear, request.userAnswers),
          OtherIncomeFromPropertySummary.row(taxYear, request.userAnswers)
        ).flatten
      )

      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, "rental-income")

      request.userAnswers.get(RentalsIncome) match {
        case Some(propertyRentalsIncome) =>
          propertySubmissionService.savePropertyRentalsIncome(context, propertyRentalsIncome).map {
            case Right(_) =>
              auditCYA(taxYear, request, propertyRentalsIncome)
              Redirect(controllers.propertyrentals.income.routes.IncomeSectionFinishedController.onPageLoad(taxYear))
            case Left(_) =>
              InternalServerError
          }

        case None =>
          logger.error(s"${incomePath(Rentals)} section is not present in userAnswers")
      }

      Future.successful(
        Redirect(controllers.propertyrentals.income.routes.IncomeSectionFinishedController.onPageLoad(taxYear))
      )
  }

  private def auditCYA(taxYear: Int, request: DataRequest[AnyContent], propertyRentalsIncome: RentalsIncome)(implicit
    hc: HeaderCarrier
  ): Unit = {
    val auditModel = RentalsAuditModel(
      request.user.nino,
      request.user.affinityGroup,
      request.user.mtditid,
      agentReferenceNumber = request.user.agentRef,
      taxYear,
      isUpdate = false,
      "PropertyRentalsIncome",
      propertyRentalsIncome
    )

    audit.sendRentalsAuditEvent(auditModel)
  }
}
