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

package controllers.structuresbuildingallowance

import audit.{AuditModel, AuditService}
import controllers.actions._
import controllers.exceptions.InternalErrorFailure
import models.JourneyPath.{PropertyRentalSBA, RentalsAndRentARoomSBA}
import models.backend.PropertyDetails
import models.{AccountingMethod, AuditPropertyType, JourneyContext, JourneyName, PropertyType, Rentals, RentalsRentARoom, SectionName}
import models.requests.DataRequest
import pages.foreign.Country
import pages.structurebuildingallowance.SbaInfo
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.{BusinessService, PropertySubmissionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.structurebuildingallowance.ClaimStructureBuildingAllowanceSummary
import viewmodels.govuk.summarylist._
import views.html.structurebuildingallowance.ClaimSbaCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClaimSbaCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  propertySubmissionService: PropertySubmissionService,
  auditService: AuditService,
  businessService: BusinessService,
  view: ClaimSbaCheckYourAnswersView
)(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          ClaimStructureBuildingAllowanceSummary
            .row(taxYear, request.userAnswers, request.user.isAgentMessageKey, propertyType)
        ).flatten
      )
      Ok(view(list, taxYear, propertyType))
    }

  def onSubmit(taxYear: Int, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      saveJourneyAnswers(taxYear, request, propertyType)
    }

  private def saveJourneyAnswers(
    taxYear: Int,
    request: DataRequest[AnyContent],
    propertyType: PropertyType
  )(implicit hc: HeaderCarrier
  ): Future[Result] = {
    val journeyPath = if (propertyType == Rentals) PropertyRentalSBA else RentalsAndRentARoomSBA
    val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, journeyPath)
    businessService
      .getUkPropertyDetails(request.user.nino, request.user.mtditid)
      .flatMap {
        case Right(Some(propertyDetails)) => saveSBANoClaim(taxYear, request, context, propertyType, propertyDetails)
        case Left(_) =>
          logger.error("CashOrAccruals information could not be retrieved from downstream.")
          Future.failed(InternalErrorFailure("CashOrAccruals information could not be retrieved from downstream."))
      }
  }

  private def saveSBANoClaim(
    taxYear: Int,
    request: DataRequest[AnyContent],
    context: JourneyContext,
    propertyType: PropertyType,
    propertyDetails: PropertyDetails
  )(implicit hc: HeaderCarrier
  ): Future[Result] = {
    val sbaInfo: SbaInfo = SbaInfo(claimStructureBuildingAllowance = false, structureBuildingFormGroup = Array.empty)
    propertySubmissionService.saveJourneyAnswers(context, sbaInfo, propertyDetails.incomeSourceId).flatMap {
      case Right(_) =>
        auditSBANoClaim(
          taxYear,
          request,
          sbaInfo,
          propertyType,
          isFailed = false,
          accrualsOrCash = propertyDetails.accrualsOrCash.get
        )
        Future.successful(Redirect(
          controllers.structuresbuildingallowance.routes.SbaSectionFinishedController.onPageLoad(taxYear, propertyType)
        ))
      case Left(_) =>
        auditSBANoClaim(
          taxYear,
          request,
          sbaInfo,
          propertyType,
          isFailed = true,
          accrualsOrCash = propertyDetails.accrualsOrCash.get
        )
        logger.error("Error saving SBA with no claims")
        Future.failed(InternalErrorFailure("Error saving SBA with no claims"))
    }
  }

  private def auditSBANoClaim(
    taxYear: Int,
    request: DataRequest[AnyContent],
    sbaInfo: SbaInfo,
    propertyType: PropertyType,
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
      journeyName = propertyType match {
        case Rentals          => JourneyName.Rentals
        case RentalsRentARoom => JourneyName.RentalsRentARoom
      },
      sectionName = SectionName.SBA,
      accountingMethod = if (accrualsOrCash) AccountingMethod.Traditional else AccountingMethod.Cash,
      isUpdate = false,
      isFailed = isFailed,
      agentReferenceNumber = request.user.agentRef,
      userEnteredDetails = sbaInfo
    )
    auditService.sendAuditEvent(auditModel)
  }
}
