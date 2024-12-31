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

import audit.{AuditModel, AuditService, ForeignAllowance}
import controllers.actions._
import controllers.exceptions.SaveJourneyAnswersFailed
import models.JourneyPath.ForeignPropertyAllowances
import models.backend.ServiceError
import models.requests.DataRequest
import models._
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
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

  def onPageLoad(taxYear: Int, countryCode: String): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
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

  def onSubmit(taxYear: Int, countryCode: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(ForeignAllowance) match {
        case Some(allowance) => saveAllowances(taxYear, countryCode, request, allowance)
        case None =>
          logger.error("Allowance in property rentals is not present in userAnswers")
          Future.failed(NotFoundException)
      }
  }


  private def saveAllowances(taxYear: Int, countryCode: String, request: DataRequest[AnyContent], allowance: ForeignAllowance)(implicit
                                                                                                                               hc: HeaderCarrier
  ) =
    saveAllowanceForPropertyRentals(taxYear, countryCode, request, allowance).flatMap {
      case Right(_: Unit) =>
        auditAllowanceCYA(taxYear, countryCode, request, allowance, isFailed = false, AccountingMethod.Traditional)
        Future.successful(
          Redirect(controllers.allowances.routes.AllowancesSectionFinishedController.onPageLoad(taxYear))
        )
      case Left(error) =>
        logger.error(s"Failed to save Allowances section: ${error.toString}")
        auditAllowanceCYA(taxYear, countryCode, request, allowance, isFailed = true, AccountingMethod.Traditional)
        Future.failed(SaveJourneyAnswersFailed("Failed to save Rentals Allowances section"))
    }

  private def saveAllowanceForPropertyRentals(
    taxYear: Int,
    countryCode: String,
    request: DataRequest[AnyContent],
    allowance: ForeignAllowance
  )(implicit
    hc: HeaderCarrier
  ): Future[Either[ServiceError, Unit]] = {
    val context = JourneyContext(
      taxYear = taxYear,
      mtditid = request.user.mtditid,
      nino = request.user.nino,
      journeyPath = ForeignPropertyAllowances
    )
    propertySubmissionService.saveJourneyAnswers[ForeignAllowance](context, allowance)
  }

  private def auditAllowanceCYA(
    taxYear: Int,
    countryCode: String,
    request: DataRequest[AnyContent],
    allowances: ForeignAllowance,
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

    auditService.sendRentalsAuditEvent(auditModel)
  }
}

case object NotFoundException extends Exception("Allowance in property rentals is not present in userAnswers")
