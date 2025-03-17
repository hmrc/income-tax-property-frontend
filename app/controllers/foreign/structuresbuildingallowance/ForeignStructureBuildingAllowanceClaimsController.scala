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

package controllers.foreign.structuresbuildingallowance

import audit.{AuditModel, AuditService}
import controllers.actions._
import controllers.exceptions.InternalErrorFailure
import forms.ForeignStructureBuildingAllowanceClaimsFormProvider
import models.backend.PropertyDetails
import models.requests.DataRequest
import models.{AccountingMethod, AuditPropertyType, JourneyContext, JourneyName, JourneyPath, NormalMode, SectionName, UserAnswers}
import pages.foreign.Country
import pages.foreign.structurebuildingallowance._
import play.api.data.Form
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import service.{BusinessService, PropertySubmissionService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.foreign.structurebuildingallowance.ForeignStructureBuildingAllowanceClaimsSummary
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.structurebuildingallowance.ForeignStructureBuildingAllowanceClaimsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignStructureBuildingAllowanceClaimsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ForeignStructureBuildingAllowanceClaimsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  propertySubmissionService: PropertySubmissionService,
  auditService: AuditService,
  businessService: BusinessService,
  view: ForeignStructureBuildingAllowanceClaimsView
)(implicit val ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {
  def onPageLoad(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val form: Form[Boolean] = formProvider()
      val list: SummaryList = summaryList(taxYear, request.userAnswers, countryCode)

      Ok(view(form, list, taxYear, countryCode, request.user.isAgentMessageKey))
    }

  private def summaryList(taxYear: Int, userAnswers: UserAnswers, countryCode: String)(implicit
    messages: Messages
  ) = {
    val foreignSbaEntries = userAnswers.get(ForeignStructureBuildingAllowanceGroup(countryCode)).toSeq.flatten
    val rows = foreignSbaEntries.zipWithIndex.flatMap { case (_, index) =>
      ForeignStructureBuildingAllowanceClaimsSummary.row(taxYear, index, userAnswers, countryCode)
    }
    SummaryListViewModel(rows)
  }

  def onSubmit(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form: Form[Boolean] = formProvider()
      val list: SummaryList = summaryList(taxYear, request.userAnswers, countryCode)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future
              .successful(BadRequest(view(formWithErrors, list, taxYear, countryCode, request.user.isAgentMessageKey))),
          value => handleValidForm(value, taxYear, request, countryCode)
        )
    }
  private def handleValidForm(
    addAnotherClaim: Boolean,
    taxYear: Int,
    request: DataRequest[AnyContent],
    countryCode: String
  )(implicit hc: HeaderCarrier): Future[Result] =
    for {
      updatedAnswers <-
        Future.fromTry(
          request.userAnswers.set(ForeignStructureBuildingAllowanceClaimsPage(countryCode), addAnotherClaim)
        )
      _ <- sessionRepository.set(updatedAnswers)
      result <- if (addAnotherClaim) {
                  val nextIndex = request.userAnswers
                    .get(ForeignStructureBuildingAllowanceGroup(countryCode))
                    .map(_.length)
                    .getOrElse(0)
                  redirectToAddClaim(taxYear, countryCode, nextIndex)
                } else { saveJourneyAnswers(taxYear, request, countryCode) }
    } yield result

  private def redirectToAddClaim(taxYear: Int, countryCode: String, nextIndex: Int): Future[Result] =
    Future.successful(
      Redirect(
        routes.ForeignStructureBuildingQualifyingDateController.onPageLoad(taxYear, countryCode, nextIndex, NormalMode)
      )
    )

  private def saveJourneyAnswers(taxYear: Int, request: DataRequest[AnyContent], countryCode: String)(implicit
    hc: HeaderCarrier
  ): Future[Result] = {
    val context =
      JourneyContext(taxYear, request.user.mtditid, request.user.nino, JourneyPath.ForeignStructureBuildingAllowance)
    businessService
      .getForeignPropertyDetails(request.user.nino, request.user.mtditid)
      .flatMap {
        case Right(Some(propertyDetails)) =>
          saveForeignSBAClaims(taxYear, request, context, countryCode, propertyDetails)
        case Left(_) =>
          logger.error("CashOrAccruals information could not be retrieved from downstream.")
          Future.failed(InternalErrorFailure("CashOrAccruals information could not be retrieved from downstream."))
      }
  }

  private def getForeignSbaInfo(userAnswers: UserAnswers, countryCode: String): Option[ForeignSbaInfo] =
    for {
      claimPage <- userAnswers.get(ForeignClaimStructureBuildingAllowancePage(countryCode))
    } yield ForeignSbaInfo(countryCode, claimPage, userAnswers.get(ForeignStructureBuildingAllowanceGroup(countryCode)))

  private def saveForeignSBAClaims(
    taxYear: Int,
    request: DataRequest[AnyContent],
    context: JourneyContext,
    countryCode: String,
    propertyDetails: PropertyDetails
  )(implicit
    hc: HeaderCarrier
  ): Future[Result] =
    getForeignSbaInfo(request.userAnswers, countryCode)
      .map { foreignSbaInfo =>
        propertySubmissionService
          .saveForeignPropertyJourneyAnswers[ForeignSbaInfo](context, foreignSbaInfo)
          .flatMap {
            case Right(_) =>
              auditCYA(
                taxYear,
                request,
                foreignSbaInfo,
                isFailed = false,
                accrualsOrCash = propertyDetails.accrualsOrCash.getOrElse(true)
              )
              Future.successful(Redirect(routes.ForeignSbaCompleteController.onPageLoad(taxYear, countryCode)))
            case Left(_) =>
              auditCYA(
                taxYear,
                request,
                foreignSbaInfo,
                isFailed = true,
                accrualsOrCash = propertyDetails.accrualsOrCash.getOrElse(true)
              )
              logger.error("Error saving Foreign SBA Claims")
              Future.failed(InternalErrorFailure("Error saving Foreign SBA claims"))
          }
      }
      .getOrElse {
        logger.error("Foreign Structure and Building Allowance not found in userAnswers")
        Future.failed(InternalErrorFailure("Foreign Structure and Building Allowance not found in userAnswers"))
      }
  private def auditCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignSba: ForeignSbaInfo,
    isFailed: Boolean,
    accrualsOrCash: Boolean
  )(implicit
    hc: HeaderCarrier
  ): Unit = {
    val auditModel = AuditModel(
      request.user.affinityGroup,
      request.user.nino,
      request.user.mtditid,
      taxYear,
      propertyType = AuditPropertyType.ForeignProperty,
      countryCode = Country.UK.code,
      journeyName = JourneyName.ForeignProperty,
      sectionName = SectionName.SBA,
      accountingMethod = if (accrualsOrCash) AccountingMethod.Traditional else AccountingMethod.Cash,
      isUpdate = false,
      isFailed = isFailed,
      request.user.agentRef,
      foreignSba
    )

    auditService.sendAuditEvent(auditModel)
  }
}
