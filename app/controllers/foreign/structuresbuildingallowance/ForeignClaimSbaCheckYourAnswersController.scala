/*
 * Copyright 2025 HM Revenue & Customs
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
import controllers.exceptions.{NotFoundException, SaveJourneyAnswersFailed}
import models.requests.DataRequest
import models.{AccountingMethod, AuditPropertyType, JourneyContext, JourneyName, JourneyPath, SectionName}
import pages.foreign.structurebuildingallowance.{ForeignClaimStructureBuildingAllowancePage, ForeignSbaInfo}
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.foreign.structurebuildingallowance.ForeignClaimSbaSummary
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.structurebuildingallowance.ForeignClaimSbaCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignClaimSbaCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  propertySubmissionService: PropertySubmissionService,
  auditService: AuditService,
  view: ForeignClaimSbaCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          ForeignClaimSbaSummary
            .row(taxYear, request.userAnswers, request.user.isAgentMessageKey, countryCode)
        ).flatten
      )
      Ok(view(list, taxYear, countryCode))
    }

  def onSubmit(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers
        .get(ForeignClaimStructureBuildingAllowancePage(countryCode))
        .map(claimSba => saveForeignPropertySba(taxYear, request, claimSba, countryCode))
        .getOrElse {
          logger.error(
            s"Foreign property sba section is not present in userAnswers for userId: ${request.userId} "
          )
          Future.failed(
            NotFoundException("Foreign property sba section is not present in userAnswers")
          )
        }
    }

  private def saveForeignPropertySba(
    taxYear: Int,
    request: DataRequest[AnyContent],
    claimSba: Boolean,
    countryCode: String
  )(implicit hc: HeaderCarrier): Future[Result] = {
    val foreignSbaInfo = ForeignSbaInfo(countryCode, claimSba, None)
    val context =
      JourneyContext(taxYear, request.user.mtditid, request.user.nino, JourneyPath.ForeignStructureBuildingAllowance)
    propertySubmissionService.saveForeignPropertyJourneyAnswers(context, foreignSbaInfo).flatMap {
      case Right(_) =>
        auditCYA(taxYear, request, foreignSbaInfo, isFailed = false, AccountingMethod.Traditional)
        Future.successful(Redirect(routes.ForeignSbaCompleteController.onPageLoad(taxYear, countryCode)))
      case Left(error) =>
        logger.error(s"Failed to save Foreign sba section : ${error.toString}")
        auditCYA(taxYear, request, foreignSbaInfo, isFailed = true, AccountingMethod.Traditional)
        Future.failed(SaveJourneyAnswersFailed("Failed to Foreign sba section"))
    }

  }

  private def auditCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignSbaInfo: ForeignSbaInfo,
    isFailed: Boolean,
    accountingMethod: AccountingMethod
  )(implicit
    hc: HeaderCarrier
  ): Unit = {
    val auditModel = AuditModel(
      request.user.nino,
      request.user.affinityGroup,
      request.user.mtditid,
      request.user.agentRef,
      taxYear,
      isUpdate = false,
      sectionName = SectionName.ForeignStructureBuildingAllowance,
      propertyType = AuditPropertyType.ForeignProperty,
      journeyName = JourneyName.ForeignProperty,
      accountingMethod = accountingMethod,
      isFailed = isFailed,
      foreignSbaInfo
    )

    auditService.sendAuditEvent(auditModel)
  }
}
