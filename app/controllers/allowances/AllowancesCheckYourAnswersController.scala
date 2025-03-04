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

package controllers.allowances

import audit.{AuditModel, AuditService, RentalsAllowance}
import controllers.actions._
import controllers.exceptions.SaveJourneyAnswersFailed
import models.JourneyPath.PropertyRentalAllowances
import models._
import models.backend.ServiceError
import models.requests.DataRequest
import pages.foreign.Country
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.allowances._
import viewmodels.govuk.summarylist._
import views.html.allowances.AllowancesCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AllowancesCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: AllowancesCheckYourAnswersView,
  auditService: AuditService,
  propertySubmissionService: PropertySubmissionService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          CapitalAllowancesForACarSummary.row(taxYear, request.userAnswers, Rentals),
          AnnualInvestmentAllowanceSummary.row(taxYear, request.userAnswers, Rentals),
          ZeroEmissionCarAllowanceSummary.row(taxYear, request.userAnswers, Rentals),
          ZeroEmissionGoodsVehicleAllowanceSummary.row(taxYear, request.userAnswers, Rentals),
          BusinessPremisesRenovationSummary.row(taxYear, request.userAnswers, Rentals),
          ReplacementOfDomesticGoodsSummary.row(taxYear, request.userAnswers, Rentals),
          OtherCapitalAllowanceSummary.row(taxYear, request.userAnswers, Rentals)
        ).flatten
      )

      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(RentalsAllowance) match {
        case Some(allowance) => saveAllowances(taxYear, request, allowance)
        case None =>
          logger.error("Allowance in property rentals is not present in userAnswers")
          Future.failed(NotFoundException)
      }
  }

  private def saveAllowances(taxYear: Int, request: DataRequest[AnyContent], allowance: RentalsAllowance)(implicit
    hc: HeaderCarrier
  ) =
    saveAllowanceForPropertyRentals(taxYear, request, allowance).flatMap {
      case Right(_: Unit) =>
        auditAllowanceCYA(taxYear, request, allowance, isFailed = false, AccountingMethod.Traditional)
        Future.successful(
          Redirect(controllers.allowances.routes.AllowancesSectionFinishedController.onPageLoad(taxYear))
        )
      case Left(error) =>
        logger.error(s"Failed to save Rentals Allowances section: ${error.toString}")
        auditAllowanceCYA(taxYear, request, allowance, isFailed = true, AccountingMethod.Traditional)
        Future.failed(SaveJourneyAnswersFailed("Failed to save Rentals Allowances section"))
    }

  private def saveAllowanceForPropertyRentals(
    taxYear: Int,
    request: DataRequest[AnyContent],
    allowance: RentalsAllowance
  )(implicit
    hc: HeaderCarrier
  ): Future[Either[ServiceError, Unit]] = {
    val context = JourneyContext(
      taxYear = taxYear,
      mtditid = request.user.mtditid,
      nino = request.user.nino,
      journeyPath = PropertyRentalAllowances
    )
    propertySubmissionService.saveJourneyAnswers[RentalsAllowance](context, allowance)
  }

  private def auditAllowanceCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    allowances: RentalsAllowance,
    isFailed: Boolean,
    accountingMethod: AccountingMethod
  )(implicit hc: HeaderCarrier): Unit = {

    val auditModel = AuditModel(
      request.user.affinityGroup,
      request.user.nino,
      request.user.mtditid,
      taxYear,
      propertyType = AuditPropertyType.UKProperty,
      countryCode = Country.UK.code,
      journeyName = JourneyName.Rentals,
      sectionName = SectionName.Allowances,
      accountingMethod = accountingMethod,
      isUpdate = false,
      isFailed = isFailed,
      request.user.agentRef,
      allowances
    )

    auditService.sendRentalsAuditEvent(auditModel)
  }
}

case object NotFoundException extends Exception("Allowance in property rentals is not present in userAnswers")
