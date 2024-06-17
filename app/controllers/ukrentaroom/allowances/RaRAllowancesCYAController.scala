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

package controllers.ukrentaroom.allowances

import audit.RentARoomAllowances._
import audit.{AuditModel, AuditService, RentARoomAllowances}
import controllers.actions._
import models.JourneyContext
import models.requests.DataRequest
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.ukrentaroom.allowances._
import viewmodels.govuk.summarylist._
import views.html.ukrentaroom.allowances.RaRAllowancesCYAView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RaRAllowancesCYAController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: RaRAllowancesCYAView,
  audit: AuditService,
  propertySubmissionService: PropertySubmissionService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          RaRCapitalAllowancesForACarSummary.row(taxYear, request.userAnswers),
          RaRAnnualInvestmentAllowanceSummary.row(taxYear, request.userAnswers),
          ElectricChargePointAllowanceForAnEVSummary.row(taxYear, request.userAnswers),
          ZeroEmissionCarAllowanceSummary.row(taxYear, request.userAnswers),
          ReplacementsOfDomesticGoodsSummary.row(taxYear, request.userAnswers),
          OtherCapitalAllowancesSummary.row(taxYear, request.userAnswers)
        ).flatten
      )

      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, "rent-a-room-allowances")

      request.userAnswers.get(RentARoomAllowances) match {
        case Some(allowances) =>
          propertySubmissionService.saveJourneyAnswers(context, allowances).map {

            case Right(_) =>
              auditCYA(taxYear, request, allowances)
              Redirect(controllers.routes.SummaryController.show(taxYear))
            case Left(_) => InternalServerError
          }
        case None =>
          logger.error("Allowances Section is not present in userAnswers")

          Future.successful(Redirect(controllers.routes.SummaryController.show(taxYear)))
      }
  }

  private def auditCYA(taxYear: Int, request: DataRequest[AnyContent], allowances: RentARoomAllowances)(implicit
    hc: HeaderCarrier
  ): Unit = {
    val auditModel = AuditModel(
      request.user.nino,
      request.user.affinityGroup,
      request.user.mtditid,
      request.user.agentRef,
      taxYear,
      isUpdate = false,
      "RentARoomAllowances",
      allowances
    )

    audit.sendRentalsAuditEvent(auditModel)
  }
}