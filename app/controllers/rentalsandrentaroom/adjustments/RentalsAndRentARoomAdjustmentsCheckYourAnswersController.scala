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

import audit.{AuditModel, AuditService}
import controllers.actions._
import controllers.exceptions.InternalErrorFailure
import models.JourneyPath.RentalsAndRentARoomAdjustments
import models.{RentalsRentARoom, _}
import models.backend.PropertyDetails
import models.requests.DataRequest
import pages.foreign.Country
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
  businessService: BusinessService,
  auditService: AuditService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with SummaryListFluency with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          PrivateUseAdjustmentSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          BalancingChargeSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          PropertyIncomeAllowanceSummary
            .row(taxYear, request.userAnswers, RentalsRentARoom, request.user.isAgentMessageKey),
          BusinessPremisesRenovationAllowanceBalancingChargeSummary.row(taxYear, request.userAnswers),
          ResidentialFinanceCostSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          UnusedResidentialFinanceCostSummary.row(taxYear, request.userAnswers, RentalsRentARoom)
        ).flatten
      )
      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      businessService
        .getUkPropertyDetails(request.user.nino, request.user.mtditid)
        .flatMap {
          case Right(Some(details)) => saveAdjustments(request, taxYear, details)
          case Left(_) =>
            val errormessage =
              s"Failed to retrieve property details for user with nino ${request.user.nino} and mtditid ${request.user.mtditid}"
            logger.error(errormessage)
            Future.failed(InternalErrorFailure(errormessage))
        }
  }

  private def saveAdjustments(
    request: DataRequest[AnyContent],
    taxYear: Int,
    details: PropertyDetails
  )(implicit
    hc: HeaderCarrier
  ) =
    request.userAnswers.get(RentalsAndRentARoomAdjustment) match {
      case Some(rentalsRentARoomAdjustments) =>
        saveAnswersAndAudit(taxYear, request, details, rentalsRentARoomAdjustments)
      case None =>
        logger.error("RentalsAndRentARoomAdjustments section is not present in userAnswers")
        Future.failed(InternalErrorFailure("RentalsAndRentARoomAdjustments section is not present in userAnswers"))
    }

  private def saveAnswersAndAudit(
    taxYear: Int,
    request: DataRequest[AnyContent],
    details: PropertyDetails,
    rentalsRentARoomAdjustments: RentalsAndRentARoomAdjustment
  )(implicit
    hc: HeaderCarrier
  ) = {
    val context =
      JourneyContext(taxYear, request.user.mtditid, request.user.nino, RentalsAndRentARoomAdjustments)
    details.accountingMethod.flatMap { accountingMethod =>
      propertySubmissionService
        .saveJourneyAnswers(context, rentalsRentARoomAdjustments, details.incomeSourceId)
        .flatMap {
          case Right(_) =>
            auditAdjustments(
              context.taxYear,
              request,
              rentalsRentARoomAdjustments,
              isFailed = false,
              accountingMethod
            )
            Future.successful(
              Redirect(
                controllers.rentalsandrentaroom.adjustments.routes.RentalsRaRAdjustmentsCompleteController
                  .onPageLoad(taxYear)
              )
            )
          case Left(_) =>
            auditAdjustments(
              context.taxYear,
              request,
              rentalsRentARoomAdjustments,
              isFailed = true,
              accountingMethod
            )
            Future.failed(InternalErrorFailure("Failed to save Rentals and Rent a Room Adjustments section."))
        }
    }
  }

  private def auditAdjustments(
    taxYear: Int,
    request: DataRequest[AnyContent],
    adjustments: RentalsAndRentARoomAdjustment,
    isFailed: Boolean,
    accountingMethod: AccountingMethod
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
      sectionName = SectionName.Adjustments,
      accountingMethod = accountingMethod,
      isUpdate = false,
      isFailed = isFailed,
      agentReferenceNumber = request.user.agentRef,
      userEnteredDetails = adjustments
    )
    auditService.sendAuditEvent(auditModel)
  }
}
