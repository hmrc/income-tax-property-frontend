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

package controllers.foreign.allowances

import audit.{AuditModel, AuditService, ForeignPropertyAllowances, ReadForeignPropertyAllowances}
import controllers.actions._
import controllers.exceptions.{NotFoundException, SaveJourneyAnswersFailed}
import controllers.foreign.allowances.routes.ForeignAllowancesCompleteController
import models._
import models.requests.DataRequest
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.foreign.allowances._
import viewmodels.govuk.summarylist._
import views.html.foreign.allowances.ForeignAllowancesCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignAllowancesCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ForeignAllowancesCheckYourAnswersView,
  auditService: AuditService,
  propertySubmissionService: PropertySubmissionService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          ForeignZeroEmissionCarAllowanceSummary.row(taxYear, countryCode, request.userAnswers),
          ForeignZeroEmissionGoodsVehiclesSummary.row(taxYear, countryCode, request.userAnswers),
          ForeignReplacementOfDomesticGoodsSummary.row(taxYear, countryCode, request.userAnswers),
          ForeignOtherCapitalAllowancesSummary.row(taxYear, countryCode, request.userAnswers)
        ).flatten
      )

      Ok(view(list, taxYear, countryCode))
    }

  def onSubmit(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers
        .get(ReadForeignPropertyAllowances(countryCode))
        .map(foreignPropertyAllowances =>
          saveForeignPropertyAllowances(taxYear, request, foreignPropertyAllowances, countryCode)
        )
        .getOrElse {
          logger.error(
            s"Foreign property allowances section is not present in userAnswers for userId: ${request.userId} "
          )
          Future.failed(
            NotFoundException("Foreign property allowances section is not present in userAnswers")
          )
        }
    }

  private def saveForeignPropertyAllowances(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignPropertyAllowances: ForeignPropertyAllowances,
    countryCode: String
  )(implicit hc: HeaderCarrier): Future[Result] = {
    val context =
      JourneyContext(taxYear, request.user.mtditid, request.user.nino, JourneyPath.ForeignPropertyAllowances)
    propertySubmissionService.saveForeignPropertyJourneyAnswers(context, foreignPropertyAllowances).flatMap {
      case Right(_) =>
        auditAllowanceCYA(taxYear, request, foreignPropertyAllowances, isFailed = false, AccountingMethod.Traditional)
        Future.successful(Redirect(ForeignAllowancesCompleteController.onPageLoad(taxYear, countryCode)))
      case Left(error) =>
        logger.error(s"Failed to save Foreign Allowances section : ${error.toString}")
        auditAllowanceCYA(taxYear, request, foreignPropertyAllowances, isFailed = true, AccountingMethod.Traditional)
        Future.failed(SaveJourneyAnswersFailed("Failed to Foreign Allowances section"))
    }

  }

  private def auditAllowanceCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    allowances: ForeignPropertyAllowances,
    isFailed: Boolean,
    accountingMethod: AccountingMethod
  )(implicit hc: HeaderCarrier): Unit = {

    val auditModel = AuditModel(
      request.user.nino,
      request.user.affinityGroup,
      request.user.mtditid,
      request.user.agentRef,
      taxYear,
      isUpdate = false,
      sectionName = SectionName.Allowances,
      propertyType = AuditPropertyType.ForeignProperty,
      journeyName = JourneyName.ForeignProperty,
      accountingMethod = accountingMethod,
      isFailed = isFailed,
      allowances
    )

    auditService.sendAuditEvent(auditModel)
  }
}
