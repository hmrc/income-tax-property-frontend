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

package controllers.enhancedstructuresbuildingallowance

import audit.{AuditModel, AuditService}
import controllers.actions._
import controllers.exceptions.InternalErrorFailure
import models.JourneyPath.{ESBA, RentalsAndRentARoomESBA}
import models.backend.PropertyDetails
import models.requests.DataRequest
import models.{AccountingMethod, AuditPropertyType, EsbasWithSupportingQuestions, JourneyContext, JourneyName, PropertyType, Rentals, RentalsRentARoom, SectionName}
import pages.foreign.Country
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.{BusinessService, PropertySubmissionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.enhancedstructurebuildingallowance.ClaimEnhancedSBASummary
import viewmodels.govuk.summarylist._
import views.html.enhancedstructuresbuildingallowance.ClaimEsbaCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClaimEsbaCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  propertySubmissionService: PropertySubmissionService,
  businessService: BusinessService,
  auditService: AuditService,
  view: ClaimEsbaCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          ClaimEnhancedSBASummary.row(taxYear, request.userAnswers, request.user.isAgentMessageKey, propertyType)
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
  )(implicit hc: HeaderCarrier): Future[Result] =
    businessService
      .getUkPropertyDetails(request.user.nino, request.user.mtditid)
      .flatMap {
        case Right(Some(propertyDetails)) =>
          val journeyPath = propertyType match {
            case Rentals => ESBA
            case _       => RentalsAndRentARoomESBA
          }
          val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, journeyPath)
          saveEsba(taxYear, request, propertyType, context, propertyDetails)
        case Left(_) =>
          logger.error("CashOrAccruals information could not be retrieved from downstream.")
          Future.failed(InternalErrorFailure("CashOrAccruals information could not be retrieved from downstream."))

      }

  private def saveEsba(
    taxYear: Int,
    request: DataRequest[AnyContent],
    propertyType: PropertyType,
    context: JourneyContext,
    propertyDetails: PropertyDetails
  )(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Result] = {
    val esbaAnswers: EsbasWithSupportingQuestions =
      EsbasWithSupportingQuestions(claimEnhancedStructureBuildingAllowance = false, Some(false), List.empty)
    for {
      accountingMethod <- checkAccountingMethod(propertyDetails)
      result <-
        propertySubmissionService.saveJourneyAnswers(context, esbaAnswers, propertyDetails.incomeSourceId).flatMap {
          case Right(_) =>
            auditESBAClaims(
              taxYear = taxYear,
              request = request,
              esbasWithSupportingQuestions = esbaAnswers,
              propertyType = propertyType,
              isFailed = false,
              accountingMethod = accountingMethod
            )
            Future.successful(
              Redirect(
                controllers.enhancedstructuresbuildingallowance.routes.EsbaSectionFinishedController
                  .onPageLoad(taxYear, propertyType)
              )
            )
          case Left(_) =>
            auditESBAClaims(
              taxYear = taxYear,
              request = request,
              esbasWithSupportingQuestions = esbaAnswers,
              propertyType = propertyType,
              isFailed = true,
              accountingMethod = accountingMethod
            )
            logger.error("Error saving ESBA with no claims")
            Future.failed(InternalErrorFailure("Error saving ESBA with no claims"))
        }

    } yield result
  }

  private def checkAccountingMethod(propertyDetails: PropertyDetails): Future[AccountingMethod] =
    propertyDetails.getAccountingMethod match {
      case Some(value) => Future.successful(value)
      case None =>
        logger.error(
          "No accrualsOrCash exists, hence setting accounting method to Traditional on audit"
        )
        Future.failed(InternalErrorFailure("Accruals or cash could not be retrieved"))
    }

  private def auditESBAClaims(
    taxYear: Int,
    request: DataRequest[AnyContent],
    esbasWithSupportingQuestions: EsbasWithSupportingQuestions,
    propertyType: PropertyType,
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
      journeyName = propertyType match {
        case Rentals          => JourneyName.Rentals
        case RentalsRentARoom => JourneyName.RentalsRentARoom
      },
      sectionName = SectionName.ESBA,
      accountingMethod = accountingMethod,
      isUpdate = false,
      isFailed = isFailed,
      agentReferenceNumber = request.user.agentRef,
      userEnteredDetails = esbasWithSupportingQuestions
    )
    auditService.sendAuditEvent(auditModel)
  }
}
