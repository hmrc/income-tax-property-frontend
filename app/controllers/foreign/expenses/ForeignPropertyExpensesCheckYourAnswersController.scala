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
import controllers.actions._
import controllers.exceptions.{NotFoundException, SaveJourneyAnswersFailed}
import controllers.foreign.expenses.routes.ForeignExpensesSectionCompleteController
import models.AuditPropertyType.ForeignProperty
import models.{AccountingMethod, ForeignPropertyExpenses, JourneyContext, JourneyName, JourneyPath, ReadForeignPropertyExpenses, SectionName}
import models.requests.DataRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.foreign.expenses._
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.expenses.ForeignPropertyExpensesCheckYourAnswersView
import play.api.i18n.Lang.logger
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignPropertyExpensesCheckYourAnswersController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         val controllerComponents: MessagesControllerComponents,
                                         propertySubmissionService: PropertySubmissionService,
                                         audit: AuditService,
                                         view: ForeignPropertyExpensesCheckYourAnswersView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, countryCode: String): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

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

  def onSubmit(taxYear: Int, countryCode: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers
        .get(ReadForeignPropertyExpenses(countryCode))
        .map(foreignPropertyExpenses => saveForeignPropertyExpenses(taxYear, request, foreignPropertyExpenses, countryCode))
        .getOrElse {
          logger.error(
            s"Foreign property expenses section is not present in userAnswers for userId: ${request.userId} "
          )
          Future.failed(
            NotFoundException("Foreign property expenses section is not present in userAnswers")
          )
        }
  }

  private def saveForeignPropertyExpenses(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignPropertyExpenses: ForeignPropertyExpenses,
    countryCode: String
  )(implicit hc: HeaderCarrier): Future[Result] = {
    val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, JourneyPath.ForeignPropertyExpenses)
    propertySubmissionService.saveForeignPropertyJourneyAnswers(context, foreignPropertyExpenses).flatMap {
      case Right(_) =>
        auditCYA(taxYear, request, foreignPropertyExpenses, isFailed = false, AccountingMethod.Traditional)
        Future.successful(Redirect(ForeignExpensesSectionCompleteController.onPageLoad(taxYear, countryCode)))
      case Left(error) =>
        logger.error(s"Failed to save Foreign Expenses section : ${error.toString}")
        auditCYA(taxYear, request, foreignPropertyExpenses, isFailed = true, AccountingMethod.Traditional)
        Future.failed(SaveJourneyAnswersFailed("Failed to save Foreign Expenses section"))
    }
  }

  private def auditCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignPropertyExpenses: ForeignPropertyExpenses,
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
      sectionName = SectionName.ForeignPropertyExpenses,
      propertyType = ForeignProperty,
      journeyName = JourneyName.ForeignProperty,
      accountingMethod = accountingMethod,
      isFailed = isFailed,
      foreignPropertyExpenses
    )

    audit.sendAuditEvent(auditModel)
  }
}
