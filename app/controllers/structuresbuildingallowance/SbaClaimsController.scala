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

package controllers.structuresbuildingallowance

import audit.{AuditService, RentalsAuditModel}
import controllers.actions._
import controllers.exceptions.InternalErrorFailure
import forms.structurebuildingallowance.SbaClaimsFormProvider
import models.requests.DataRequest
import models.{JourneyContext, PropertyType, Rentals, UserAnswers}
import pages.structurebuildingallowance._
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import service.PropertySubmissionService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.structurebuildingallowance.StructureBuildingAllowanceSummary
import viewmodels.govuk.summarylist._
import views.html.structurebuildingallowance.SbaClaimsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SbaClaimsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: SbaClaimsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: SbaClaimsView,
  propertySubmissionService: PropertySubmissionService,
  auditService: AuditService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val form: Form[Boolean] = formProvider(request.user.isAgentMessageKey)
      val list: SummaryList = summaryList(taxYear, request.userAnswers, propertyType: PropertyType)

      Ok(view(form, list, taxYear, request.user.isAgentMessageKey, propertyType))
    }

  def onSubmit(taxYear: Int, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      val list = summaryList(taxYear, request.userAnswers, propertyType)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(view(formWithErrors, list, taxYear, request.user.isAgentMessageKey, propertyType))
            ),
          value => handleValidForm(value, taxYear, request, propertyType)
        )
    }

  private def summaryList(taxYear: Int, userAnswers: UserAnswers, propertyType: PropertyType)(implicit
    messages: Messages
  ) = {
    val sbaEntries = userAnswers.get(StructureBuildingAllowanceGroup(propertyType)).toSeq.flatten
    val rows = sbaEntries.zipWithIndex.flatMap { case (_, index) =>
      StructureBuildingAllowanceSummary.row(taxYear, index, userAnswers, propertyType)
    }
    SummaryListViewModel(rows)
  }

  private def handleValidForm(
    addAnotherClaim: Boolean,
    taxYear: Int,
    request: DataRequest[AnyContent],
    propertyType: PropertyType
  )(implicit
    hc: HeaderCarrier
  ): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(SbaClaimsPage(propertyType), addAnotherClaim))
      _              <- sessionRepository.set(updatedAnswers)
      result <- if (addAnotherClaim) { redirectToAddClaim(taxYear, propertyType) }
                else { saveSBAClaims(taxYear, request, propertyType) }
    } yield result

  private def redirectToAddClaim(taxYear: Int, propertyType: PropertyType) =
    Future.successful(
      Redirect(routes.AddClaimStructureBuildingAllowanceController.onPageLoad(taxYear, propertyType))
    )

  private def saveSBAClaims(taxYear: Int, request: DataRequest[AnyContent], propertyType: PropertyType)(implicit
    hc: HeaderCarrier
  ): Future[Result] = {
    val sbaInfoOpt = getSBA(request.userAnswers, propertyType)
    sbaInfoOpt
      .map { sbaInfo =>
        val journeyPath = if (propertyType == Rentals) "property-rental-sba" else "rentals-and-rent-a-room-sba"
        val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, journeyPath)
        propertySubmissionService.saveJourneyAnswers(context, sbaInfo).flatMap {
          case Right(_) =>
            auditSBAClaims(taxYear, request, sbaInfo)
            Future.successful(Redirect(routes.SbaSectionFinishedController.onPageLoad(taxYear, propertyType)))
          case Left(_) =>
            logger.error("Error saving SBA Claims")
            Future.failed(InternalErrorFailure("Error saving SBA claims"))
        }
      }
      .getOrElse {
        logger.error("Structure and Building Allowance not found in userAnswers")
        Future.failed(InternalErrorFailure("Structure and Building Allowance not found in userAnswers"))
      }

  }

  private def getSBA(userAnswers: UserAnswers, propertyType: PropertyType) = {
    val sbaInfoOpt = for {
      claimSummaryPage <- userAnswers.get(ClaimStructureBuildingAllowancePage(propertyType))
      sbaGroup         <- userAnswers.get(StructureBuildingAllowanceGroup(propertyType))
    } yield SbaInfo(claimSummaryPage, sbaGroup)
    sbaInfoOpt
  }

  private def auditSBAClaims(
    taxYear: Int,
    request: DataRequest[AnyContent],
    sbaInfo: SbaInfo
  )(implicit
    hc: HeaderCarrier
  ): Unit = {
    val auditModel = RentalsAuditModel[SbaInfo](
      nino = request.user.nino,
      userType = request.user.affinityGroup,
      mtdItId = request.user.mtditid,
      agentReferenceNumber = request.user.agentRef,
      taxYear = taxYear,
      isUpdate = false,
      sectionName = "PropertyRentalsSBA",
      userEnteredRentalDetails = sbaInfo
    )
    auditService.sendRentalsAuditEvent(auditModel)
  }

}
