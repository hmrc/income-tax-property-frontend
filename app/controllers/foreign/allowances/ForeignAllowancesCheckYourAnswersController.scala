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
import controllers.PropertyDetailsHandler
import controllers.actions._
import controllers.exceptions.SaveJourneyAnswersFailed
import controllers.foreign.allowances.routes.ForeignAllowancesCompleteController
import models._
import models.backend.PropertyDetails
import models.requests.DataRequest
import pages.foreign.Country
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.{BusinessService, PropertySubmissionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import viewmodels.checkAnswers.foreign.adjustments.ForeignCapitalAllowancesForACarSummary
import viewmodels.checkAnswers.foreign.allowances._
import viewmodels.govuk.summarylist._
import views.html.foreign.allowances.ForeignAllowancesCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ForeignAllowancesCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ForeignAllowancesCheckYourAnswersView,
  auditService: AuditService,
  propertySubmissionService: PropertySubmissionService,
  businessService: BusinessService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with PropertyDetailsHandler with Logging {
  def onPageLoad(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      withForeignPropertyDetails[Result](businessService, request.user.nino, request.user.mtditid) {
        (propertyData: PropertyDetails) =>
          val rows = propertyData.accrualsOrCash match {
            case Some(false) =>
              Seq(ForeignCapitalAllowancesForACarSummary.row(taxYear, countryCode, request.userAnswers)).flatten
            case _ =>
              Seq(
                ForeignZeroEmissionCarAllowanceSummary.row(taxYear, countryCode, request.userAnswers),
                ForeignZeroEmissionGoodsVehiclesSummary.row(taxYear, countryCode, request.userAnswers),
                ForeignReplacementOfDomesticGoodsSummary.row(taxYear, countryCode, request.userAnswers),
                ForeignOtherCapitalAllowancesSummary.row(taxYear, countryCode, request.userAnswers)
              ).flatten
          }

          val list = SummaryListViewModel(rows = rows)
          Future.successful(Ok(view(list, taxYear, countryCode)))

      }(hc, ec)
    }

  def onSubmit(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers
        .get(ReadForeignPropertyAllowances(countryCode))
        .fold {
          val errorMsg =
            s"Foreign property allowances section is missing for userId: ${request.userId}, taxYear: $taxYear, countryCode: $countryCode"
          logger.error(errorMsg)
          Future.successful(NotFound(errorMsg))
        } { foreignPropertyAllowances =>
          saveForeignPropertyAllowances(taxYear, request, foreignPropertyAllowances, countryCode)
        }
    }
  private def saveForeignPropertyAllowances(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignPropertyAllowances: ForeignPropertyAllowances,
    countryCode: String
  )(implicit hc: HeaderCarrier): Future[Result] =
    withForeignPropertyDetails(businessService, request.user.nino, request.user.mtditid) { propertyDetails =>
      val context =
        JourneyContext(taxYear, request.user.mtditid, request.user.nino, JourneyPath.ForeignPropertyAllowances)
      val accrualsOrCash = propertyDetails.accrualsOrCash.getOrElse(true)

      propertySubmissionService
        .saveForeignPropertyJourneyAnswers(context, foreignPropertyAllowances)
        .map {
          case Right(_) => Redirect(ForeignAllowancesCompleteController.onPageLoad(taxYear, countryCode))
          case Left(error) =>
            logger.error(s"Failed to save Foreign Allowances section: ${error.toString}")
            throw SaveJourneyAnswersFailed("Failed to save Foreign Allowances section")
        }
        .andThen {
          case Success(_) =>
            auditAllowanceCYA(taxYear, request, foreignPropertyAllowances, isFailed = false, accrualsOrCash)
          case Failure(_) =>
            auditAllowanceCYA(taxYear, request, foreignPropertyAllowances, isFailed = true, accrualsOrCash)
        }
    }

  private def auditAllowanceCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    allowances: ForeignPropertyAllowances,
    isFailed: Boolean,
    accrualsOrCash: Boolean
  )(implicit hc: HeaderCarrier): Unit = {

    val auditModel = AuditModel(
      request.user.affinityGroup,
      request.user.nino,
      request.user.mtditid,
      taxYear,
      propertyType = AuditPropertyType.ForeignProperty,
      countryCode = Country.UK.code,
      journeyName = JourneyName.ForeignProperty,
      sectionName = SectionName.Allowances,
      accountingMethod = if (accrualsOrCash) AccountingMethod.Traditional else AccountingMethod.Cash,
      isUpdate = false,
      isFailed = isFailed,
      request.user.agentRef,
      allowances
    )
    auditService.sendAuditEvent(auditModel)
  }
}
