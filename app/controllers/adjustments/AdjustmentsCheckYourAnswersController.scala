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

package controllers.adjustments

import audit.RentalsAdjustment._
import audit.{AuditModel, AuditService, RentalsAdjustment}
import controllers.actions._
import controllers.exceptions.{InternalErrorFailure, SaveJourneyAnswersFailed}
import models.JourneyPath.PropertyRentalAdjustments
import models._
import models.requests.DataRequest
import pages.adjustments.UnusedLossesBroughtForwardPage
import pages.foreign.Country
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.PropertySubmissionService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.adjustments._
import viewmodels.govuk.summarylist._
import views.html.adjustments.AdjustmentsCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode

class AdjustmentsCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  propertySubmissionService: PropertySubmissionService,
  audit: AuditService,
  view: AdjustmentsCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val summaryListRows = Seq(
        PrivateUseAdjustmentSummary.row(taxYear, request.userAnswers, Rentals),
        BalancingChargeSummary.row(taxYear, request.userAnswers, Rentals),
        PropertyIncomeAllowanceSummary.row(taxYear, request.userAnswers, Rentals, request.user.isAgentMessageKey),
        RenovationAllowanceBalancingChargeSummary.row(taxYear, request.userAnswers, Rentals),
        ResidentialFinanceCostSummary.row(taxYear, request.userAnswers, Rentals),
        UnusedResidentialFinanceCostSummary.row(taxYear, request.userAnswers, Rentals)
      ).flatten

      val unusedLossesBroughtForwardAmount = request.userAnswers.get(UnusedLossesBroughtForwardPage(Rentals)).flatMap(_.unusedLossesBroughtForwardAmount).getOrElse(BigDecimal(0))

      val UnusedLossesBroughtForwardRows: IterableOnce[SummaryListRow] with Equals =
        request.userAnswers
          .get(UnusedLossesBroughtForwardPage(Rentals))
          .filter(_.unusedLossesBroughtForwardYesOrNo)
          .map(_ =>
            Seq(
              UnusedLossesBroughtForwardSummary.row(taxYear, request.userAnswers, Rentals, request.user.isAgentMessageKey),
              WhenYouReportedTheLossSummary.row(taxYear, request.userAnswers, Rentals, request.user.isAgentMessageKey, unusedLossesBroughtForwardAmount)
            ).flatten
          )
          .getOrElse(Seq(UnusedLossesBroughtForwardSummary.row(taxYear, request.userAnswers, Rentals, request.user.isAgentMessageKey)).flatten)

      val list = SummaryListViewModel(
        rows = summaryListRows
          .appendedAll(UnusedLossesBroughtForwardRows)
      )
      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(RentalsAdjustment) match {
        case Some(adjustments) => saveAdjustments(taxYear, request, adjustments)
        case None =>
          logger.error("Adjustments Section is not present in userAnswers")
          Future.failed(InternalErrorFailure("Adjustments Section is not present in userAnswers"))
      }
  }

  private def saveAdjustments(taxYear: Int, request: DataRequest[AnyContent], adjustments: RentalsAdjustment)(implicit
    hc: HeaderCarrier
  ) = {
    val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, PropertyRentalAdjustments)
    propertySubmissionService.saveJourneyAnswers(context, adjustments).flatMap {
      case Right(_) =>
        auditCYA(taxYear, request, adjustments, isFailed = false, AccountingMethod.Traditional)
        Future
          .successful(Redirect(controllers.adjustments.routes.RentalsAdjustmentsCompleteController.onPageLoad(taxYear)))
      case Left(error) =>
        logger.error(s"Failed to save Rentals Adjustments section: ${error.toString}")
        auditCYA(taxYear, request, adjustments, isFailed = true, AccountingMethod.Traditional)
        Future.failed(SaveJourneyAnswersFailed("Failed to save Rentals Adjustments section"))
    }
  }

  private def auditCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    adjustments: RentalsAdjustment,
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
      sectionName = SectionName.Adjustments,
      accountingMethod = accountingMethod,
      isUpdate = false,
      isFailed = isFailed,
      request.user.agentRef,
      adjustments
    )

    audit.sendRentalsAuditEvent(auditModel)
  }
}
