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

package controllers.foreign.expenses

import audit.{AuditModel, AuditService}
import controllers.PropertyDetailsHandler
import controllers.actions._
import controllers.exceptions.SaveJourneyAnswersFailed
import controllers.foreign.expenses.routes.ForeignExpensesSectionCompleteController
import models.AuditPropertyType.ForeignProperty
import models.requests.DataRequest
import models.{AccountingMethod, ForeignPropertyExpenses, JourneyContext, JourneyName, JourneyPath, ReadForeignPropertyExpenses, SectionName}
import pages.foreign.Country
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.{BusinessService, PropertySubmissionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.foreign.expenses._
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.expenses.ForeignPropertyExpensesCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ForeignPropertyExpensesCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  propertySubmissionService: PropertySubmissionService,
  audit: AuditService,
  businessService: BusinessService,
  view: ForeignPropertyExpensesCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with PropertyDetailsHandler {

  def onPageLoad(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          ConsolidatedOrIndividualExpensesSummary.row(taxYear, countryCode, request.userAnswers),
          ForeignRentsRatesAndInsuranceSummary.row(taxYear, countryCode, request.userAnswers),
          ForeignPropertyRepairsAndMaintenanceSummary.row(taxYear, countryCode, request.userAnswers),
          ForeignNonResidentialPropertyFinanceCostsSummary.row(taxYear, countryCode, request.userAnswers),
          ForeignProfessionalFeesSummary.row(taxYear, countryCode, request.userAnswers),
          ForeignCostsOfServicesProvidedSummary.row(taxYear, countryCode, request.userAnswers),
          ForeignOtherAllowablePropertyExpensesSummary.row(taxYear, countryCode, request.userAnswers)
        ).flatten
      )

      Ok(view(list, taxYear, countryCode))
    }

  def onSubmit(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers
        .get(ReadForeignPropertyExpenses(countryCode))
        .fold {
          val errorMsg =
            s"Foreign property expenses section is missing for userId: ${request.userId}, taxYear: $taxYear, countryCode: $countryCode"
          logger.error(errorMsg)
          Future.successful(NotFound(errorMsg))
        } { foreignPropertyExpenses =>
          saveForeignPropertyExpenses(taxYear, request, foreignPropertyExpenses, countryCode)
        }
    }

  private def saveForeignPropertyExpenses(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignPropertyExpenses: ForeignPropertyExpenses,
    countryCode: String
  )(implicit hc: HeaderCarrier): Future[Result] =
    withForeignPropertyDetails(businessService, request.user.nino, request.user.mtditid) { propertyDetails =>
      val context =
        JourneyContext(taxYear, request.user.mtditid, request.user.nino, JourneyPath.ForeignPropertyExpenses)
      val accrualsOrCash = propertyDetails.accrualsOrCash.getOrElse(true)

      propertySubmissionService
        .saveForeignPropertyJourneyAnswers(context, foreignPropertyExpenses)
        .map {
          case Right(_) => Redirect(ForeignExpensesSectionCompleteController.onPageLoad(taxYear, countryCode))
          case Left(error) =>
            logger.error(s"Failed to save Foreign Expenses section: ${error.toString}")
            throw SaveJourneyAnswersFailed("Failed to save Foreign Expenses section")
        }
        .andThen {
          case Success(_) =>
            auditCYA(taxYear, request, foreignPropertyExpenses, isFailed = false, accrualsOrCash)
          case Failure(_) =>
            auditCYA(taxYear, request, foreignPropertyExpenses, isFailed = true, accrualsOrCash)
        }
    }

  private def auditCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignPropertyExpenses: ForeignPropertyExpenses,
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
      propertyType = ForeignProperty,
      countryCode = Country.UK.code,
      journeyName = JourneyName.ForeignProperty,
      sectionName = SectionName.ForeignPropertyExpenses,
      accountingMethod = if (accrualsOrCash) AccountingMethod.Traditional else AccountingMethod.Cash,
      isUpdate = false,
      isFailed = isFailed,
      request.user.agentRef,
      foreignPropertyExpenses
    )

    audit.sendAuditEvent(auditModel)
  }
}
