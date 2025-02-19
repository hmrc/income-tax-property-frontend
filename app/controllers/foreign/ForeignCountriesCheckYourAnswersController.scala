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

package controllers.foreign

import audit.{AuditModel, AuditService}
import controllers.actions._
import controllers.exceptions.{SaveJourneyAnswersFailed, NotFoundException}
import controllers.foreign.routes.ForeignSelectCountriesCompleteController
import models.JourneyPath.ForeignSelectCountry
import models.requests.DataRequest
import models.{JourneyName, AuditPropertyType, JourneyContext, SectionName, ForeignPropertySelectCountry, AccountingMethod}
import play.api.i18n.Lang.logger
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.language.LanguageUtils
import viewmodels.checkAnswers.PropertyIncomeReportSummary
import viewmodels.checkAnswers.foreign._
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.ForeignCountriesCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignCountriesCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  propertySubmissionService: PropertySubmissionService,
  audit: AuditService,
  view: ForeignCountriesCheckYourAnswersView,
  languageUtils: LanguageUtils
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          TotalIncomeSummary.row(taxYear, request.userAnswers),
          PropertyIncomeReportSummary.row(taxYear, request.user.isAgentMessageKey, request.userAnswers),
          CountriesRentedPropertySummary.rowList(taxYear, request.userAnswers, languageUtils.getCurrentLang.locale.toString),
          ClaimPropertyIncomeAllowanceOrExpensesSummary.row(taxYear, request.userAnswers)
        ).flatten
      )
      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers
        .get(ForeignPropertySelectCountry)
        .map(foreignPropertySelectCountry =>
          saveForeignPropertySelectCountry(taxYear, request, foreignPropertySelectCountry)
        )
        .getOrElse {
          logger.error(
            s"Foreign property select country section is not present in userAnswers for userId: ${request.userId} "
          )
          Future.failed(
            NotFoundException("Foreign property select country section is not present in userAnswers")
          )
        }
  }

  private def saveForeignPropertySelectCountry(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignPropertySelectCountry: ForeignPropertySelectCountry
  )(implicit hc: HeaderCarrier): Future[Result] = {
    val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, ForeignSelectCountry)
    propertySubmissionService.saveForeignPropertyJourneyAnswers(context, foreignPropertySelectCountry).flatMap {
      case Right(_) =>
        auditCYA(taxYear, request, foreignPropertySelectCountry, isFailed = false, AccountingMethod.Traditional)
        Future.successful(Redirect(ForeignSelectCountriesCompleteController.onPageLoad(taxYear)))

      case Left(error) =>
        logger.error(s"Failed to save Foreign Property Select Country section : ${error.toString}")
        auditCYA(taxYear, request, foreignPropertySelectCountry, isFailed = true, AccountingMethod.Traditional)
        Future.failed(SaveJourneyAnswersFailed("Failed to Foreign Property Select Country section"))

    }

  }

  private def auditCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignPropertySelectCountry: ForeignPropertySelectCountry,
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
      sectionName = SectionName.ForeignPropertySelectCountry,
      propertyType = AuditPropertyType.ForeignProperty,
      journeyName = JourneyName.ForeignProperty,
      accountingMethod = accountingMethod,
      isFailed = isFailed,
      foreignPropertySelectCountry
    )

    audit.sendAuditEvent(auditModel)
  }
}
