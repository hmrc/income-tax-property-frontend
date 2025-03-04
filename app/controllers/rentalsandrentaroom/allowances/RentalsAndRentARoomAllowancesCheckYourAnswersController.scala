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

package controllers.rentalsandrentaroom.allowances

import audit.{AuditModel, AuditService}
import controllers.PropertyDetailsHandler
import controllers.actions._
import controllers.exceptions.InternalErrorFailure
import models.JourneyPath.RentalsAndRentARoomAllowances
import models.backend.PropertyDetails
import models.requests.DataRequest
import models.{AccountingMethod, AuditPropertyType, JourneyContext, JourneyName, RentalsAndRentARoomAllowance, RentalsRentARoom, SectionName}
import pages.foreign.Country
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.{BusinessService, PropertySubmissionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.allowances._
import viewmodels.govuk.SummaryListFluency
import views.html.rentalsandrentaroom.allowances.RentalsAndRentARoomAllowancesCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RentalsAndRentARoomAllowancesCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  propertySubmissionService: PropertySubmissionService,
  val controllerComponents: MessagesControllerComponents,
  view: RentalsAndRentARoomAllowancesCheckYourAnswersView,
  businessService: BusinessService,
  auditService: AuditService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with SummaryListFluency with PropertyDetailsHandler with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          CapitalAllowancesForACarSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          AnnualInvestmentAllowanceSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          ZeroEmissionCarAllowanceSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          ZeroEmissionGoodsVehicleAllowanceSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          BusinessPremisesRenovationSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          ReplacementOfDomesticGoodsSummary.row(taxYear, request.userAnswers, RentalsRentARoom),
          OtherCapitalAllowanceSummary.row(taxYear, request.userAnswers, RentalsRentARoom)
        ).flatten
      )

      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val context =
        JourneyContext(taxYear, request.user.mtditid, request.user.nino, RentalsAndRentARoomAllowances)

      request.userAnswers.get(RentalsAndRentARoomAllowance) match {
        case Some(rentalsAndRentARoomAllowance) =>
          propertySubmissionService
            .saveJourneyAnswers(context, rentalsAndRentARoomAllowance)
            .flatMap {
              case Right(_) =>
                auditAllowancesCYA(taxYear, request, rentalsAndRentARoomAllowance, isFailed = false)
                Future.successful(
                  Redirect(
                    controllers.rentalsandrentaroom.allowances.routes.RentalsRaRAllowancesCompleteController
                      .onPageLoad(taxYear)
                  )
                )
              case Left(_) =>
                auditAllowancesCYA(taxYear, request, rentalsAndRentARoomAllowance, isFailed = true)
                Future.failed(InternalErrorFailure("Property submission save error"))
            }

        case None =>
          logger.error("RentalsAndRentARoomIncome section is not present in userAnswers")
          Future.failed(InternalErrorFailure("RentalsAndRentARoomIncome section is not present in userAnswers"))
      }

  }

  private def auditAllowancesCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    allowances: RentalsAndRentARoomAllowance,
    isFailed: Boolean
  )(implicit
    hc: HeaderCarrier
  ): Future[Unit] =
    withUkPropertyDetails[Unit](businessService, request.user.nino, request.user.mtditid) {
      propertyDetails: PropertyDetails =>
        Future(
          propertyDetails match {
            case PropertyDetails(_, _, Some(accrualsOrCash), _) =>
              val auditModel = AuditModel(
                userType = request.user.affinityGroup,
                nino = request.user.nino,
                mtdItId = request.user.mtditid,
                taxYear = taxYear,
                propertyType = AuditPropertyType.UKProperty,
                countryCode = Country.UK.code,
                journeyName = JourneyName.RentalsRentARoom,
                sectionName = SectionName.Allowances,
                accountingMethod = if (accrualsOrCash) AccountingMethod.Traditional else AccountingMethod.Cash,
                isUpdate = false,
                isFailed = isFailed,
                agentReferenceNumber = request.user.agentRef,
                userEnteredDetails = allowances
              )
              auditService.sendAuditEvent(auditModel)
            case _ => logger.error("CashOrAccruals information could not be retrieved from downstream.")
          }
        )
    }
}
