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
import controllers.PropertyDetailsHandler
import controllers.actions._
import controllers.exceptions.SaveJourneyAnswersFailed
import controllers.foreign.structuresbuildingallowance.routes.ForeignSbaCompleteController
import models.requests.DataRequest
import models.{AccountingMethod, AuditPropertyType, JourneyContext, JourneyName, JourneyPath, SectionName}
import pages.foreign.Country
import pages.foreign.structurebuildingallowance.{ForeignClaimStructureBuildingAllowancePage, ForeignSbaInfo}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.{BusinessService, PropertySubmissionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.foreign.structurebuildingallowance.ForeignClaimSbaSummary
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.structurebuildingallowance.ForeignClaimSbaCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ForeignClaimSbaCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  propertySubmissionService: PropertySubmissionService,
  auditService: AuditService,
  businessService: BusinessService,
  view: ForeignClaimSbaCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with PropertyDetailsHandler {

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
        .fold {
          val errorMsg =
            s"Foreign property sba section is missing for userId: ${request.userId}, taxYear: $taxYear, countryCode: $countryCode"
          logger.error(errorMsg)
          Future.successful(NotFound(errorMsg))
        } { claimSba =>
          saveForeignPropertySba(taxYear, request, claimSba, countryCode)
        }
    }

  private def saveForeignPropertySba(
    taxYear: Int,
    request: DataRequest[AnyContent],
    claimSba: Boolean,
    countryCode: String
  )(implicit hc: HeaderCarrier): Future[Result] =
    withForeignPropertyDetails(businessService, request.user.nino, request.user.mtditid) { propertyDetails =>
      val foreignSbaInfo = ForeignSbaInfo(countryCode, claimSba, None)
      val context =
        JourneyContext(taxYear, request.user.mtditid, request.user.nino, JourneyPath.ForeignStructureBuildingAllowance)
      val accrualsOrCash = propertyDetails.accrualsOrCash.getOrElse(true)

      propertySubmissionService
        .saveForeignPropertyJourneyAnswers(context, foreignSbaInfo)
        .map {
          case Right(_) => Redirect(ForeignSbaCompleteController.onPageLoad(taxYear, countryCode))
          case Left(error) =>
            logger.error(s"Failed to save Foreign sba section: ${error.toString}")
            throw SaveJourneyAnswersFailed("Failed to save Foreign sba section")
        }
        .andThen {
          case Success(_) =>
            auditCYA(taxYear, request, foreignSbaInfo, isFailed = false, accrualsOrCash)
          case Failure(_) =>
            auditCYA(taxYear, request, foreignSbaInfo, isFailed = true, accrualsOrCash)
        }
    }

  private def auditCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignSbaInfo: ForeignSbaInfo,
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
      sectionName = SectionName.ForeignStructureBuildingAllowance,
      accountingMethod = if (accrualsOrCash) AccountingMethod.Traditional else AccountingMethod.Cash,
      isUpdate = false,
      isFailed = isFailed,
      request.user.agentRef,
      foreignSbaInfo
    )

    auditService.sendAuditEvent(auditModel)
  }
}
