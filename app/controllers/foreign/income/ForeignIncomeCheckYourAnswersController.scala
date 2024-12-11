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

package controllers.foreign.income

import audit.{AuditModel, AuditService}
import controllers.actions._
import controllers.exceptions.{NotFoundException, SaveJourneyAnswersFailed}
import controllers.foreign.income.routes.ForeignIncomeCompleteController
import models.requests.DataRequest
import models._
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.foreign._
import viewmodels.checkAnswers.foreign.income.{ForeignPropertyRentalIncomeSummary, ForeignReversePremiumsReceivedSummary}
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.income.ForeignPropertyIncomeCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignIncomeCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  propertySubmissionService: PropertySubmissionService,
  audit: AuditService,
  view: ForeignPropertyIncomeCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          ForeignPropertyRentalIncomeSummary.row(taxYear, request.userAnswers, countryCode),
          PremiumsGrantLeaseYNSummary.row(request.userAnswers, taxYear, countryCode, request.user.isAgentMessageKey),
          CalculatedPremiumLeaseTaxableSummary.row(taxYear, countryCode, request.userAnswers),
          CalculatedPremiumLeaseTaxableAmountSummary.row(taxYear, countryCode, request.userAnswers),
          ForeignReceivedGrantLeaseAmountSummary.row(taxYear, countryCode, request.userAnswers),
          TwelveMonthPeriodsInLeaseSummary.row(taxYear, countryCode, request.userAnswers),
          ForeignPremiumsGrantLeaseSummary.row(taxYear, countryCode, request.userAnswers),
          ForeignReversePremiumsReceivedSummary.row(taxYear, countryCode, request.userAnswers),
          ForeignOtherIncomeFromPropertySummary.row(taxYear, countryCode, request.userAnswers)
        ).flatten
      )

      Ok(view(list, taxYear, countryCode))
    }

  def onSubmit(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers
        .get(ReadForeignPropertyIncome(countryCode))
        .map(foreignPropertyIncome => saveForeignPropertyIncome(taxYear, request, foreignPropertyIncome, countryCode))
        .getOrElse {
          logger.error(
            s"Foreign property income section is not present in userAnswers for userId: ${request.userId} "
          )
          Future.failed(
            NotFoundException("Foreign property income section is not present in userAnswers")
          )
        }

      Future.successful(
        Redirect(
          ForeignIncomeCompleteController.onPageLoad(taxYear, countryCode)
        )
      )
    }

  private def saveForeignPropertyIncome(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignPropertyIncome: ForeignPropertyIncome,
    countryCode: String
  )(implicit hc: HeaderCarrier): Future[Result] = {
    val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, JourneyPath.ForeignPropertyIncome)
    propertySubmissionService.saveJourneyAnswers(context, foreignPropertyIncome).flatMap {
      case Right(_) =>
        auditCYA(taxYear, request, foreignPropertyIncome, isFailed = false, AccountingMethod.Traditional)
        Future.successful(Redirect(ForeignIncomeCompleteController.onPageLoad(taxYear, countryCode)))
      case Left(error) =>
        logger.error(s"Failed to save Foreign Income section : ${error.toString}")
        auditCYA(taxYear, request, foreignPropertyIncome, isFailed = true, AccountingMethod.Traditional)
        Future.failed(SaveJourneyAnswersFailed("Failed to Foreign Income section"))
    }

  }

  private def auditCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignPropertyIncome: ForeignPropertyIncome,
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
      sectionName = SectionName.ForeignPropertyIncome,
      propertyType = AuditPropertyType.ForeignProperty,
      journeyName = JourneyName.ForeignProperty,
      accountingMethod = accountingMethod,
      isFailed = isFailed,
      foreignPropertyIncome
    )

    audit.sendAuditEvent(auditModel)
  }
}
