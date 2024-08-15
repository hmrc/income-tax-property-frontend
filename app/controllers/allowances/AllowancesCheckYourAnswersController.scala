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

import audit.{AuditService, RentalsAllowance, RentalsAuditModel}
import controllers.actions._
import models.backend.ServiceError
import models.requests.DataRequest
import models.{JourneyContext, Rentals}
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
          ElectricChargePointAllowanceSummary.row(taxYear, request.userAnswers),
          ZeroEmissionCarAllowanceSummary.row(taxYear, request.userAnswers),
          ZeroEmissionGoodsVehicleAllowanceSummary.row(taxYear, request.userAnswers),
          BusinessPremisesRenovationSummary.row(taxYear, request.userAnswers, Rentals),
          ReplacementOfDomesticGoodsSummary.row(taxYear, request.userAnswers),
          OtherCapitalAllowanceSummary.row(taxYear, request.userAnswers)
        ).flatten
      )

      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(RentalsAllowance) match {
        case Some(allowance) =>
          saveAllowanceForPropertyRentals(taxYear, request, allowance).map {
            case Left(_) => InternalServerError
            case Right(_: Unit) =>
              auditAllowanceCYA(taxYear, request, allowance)
              Redirect(controllers.allowances.routes.AllowancesSectionFinishedController.onPageLoad(taxYear))
          }
        case None =>
          logger.error("Allowance in property rentals is not present in userAnswers")
          Future.failed(NotFoundException)
      }
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
      journeyName = "property-rental-allowances"
    )
    propertySubmissionService.saveJourneyAnswers[RentalsAllowance](context, allowance)
  }

  private def auditAllowanceCYA(taxYear: Int, request: DataRequest[AnyContent], allowance: RentalsAllowance)(implicit
    hc: HeaderCarrier
  ): Unit = {
    val event = RentalsAuditModel[RentalsAllowance](
      nino = request.user.nino,
      userType = request.user.affinityGroup,
      mtdItId = request.user.mtditid,
      agentReferenceNumber = request.user.agentRef,
      taxYear = taxYear,
      isUpdate = false,
      sectionName = "PropertyRentalsAllowance",
      userEnteredRentalDetails = allowance
    )
    auditService.sendRentalsAuditEvent(event)
  }
}

case object NotFoundException extends Exception("Allowance in property rentals is not present in userAnswers")
