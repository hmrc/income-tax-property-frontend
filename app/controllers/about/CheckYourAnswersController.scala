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

package controllers.about

import audit.{AuditModel, AuditService, PropertyAbout => PropertyAboutAudit}
import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.exceptions.{NotFoundException, SaveJourneyAnswersFailed}
import models._
import models.requests.DataRequest
import pages.ReportPropertyIncomePage
import pages.foreign.Country
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.about.{ReportPropertyIncomeSummary, TotalIncomeSummary, UKPropertySelectSummary}
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersView,
  audit: AuditService,
  propertySubmissionService: PropertySubmissionService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val totalIncomeRow = TotalIncomeSummary.row(taxYear, request.user.isAgentMessageKey, request.userAnswers)
      val reportIncomeRow =
        ReportPropertyIncomeSummary.row(taxYear, request.user.isAgentMessageKey, request.userAnswers)
      val ukPropertyRow = UKPropertySelectSummary.row(taxYear, request.user.isAgentMessageKey, request.userAnswers)

      val propertyIncomeRows = if (request.userAnswers.get(ReportPropertyIncomePage).isDefined) {
        Seq(totalIncomeRow, reportIncomeRow, ukPropertyRow)
      } else {
        Seq(totalIncomeRow, ukPropertyRow)
      }

      val list = SummaryListViewModel(rows = propertyIncomeRows.flatten)

      Ok(view(taxYear, list))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers
        .get(PropertyAbout)
        .map(propertyAbout => savePropertyAbout(taxYear, request, propertyAbout))
        .getOrElse {
          logger.error(s"PropertyAbout section is not present in userAnswers for userId: ${request.userId}")
          Future.failed(NotFoundException("PropertyAbout section is not present in userAnswers"))
        }
  }

  private def savePropertyAbout(taxYear: Int, request: DataRequest[AnyContent], propertyAbout: PropertyAbout)(implicit
    hc: HeaderCarrier
  ): Future[Result] = {
    val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, JourneyPath.PropertyAbout)
    propertySubmissionService.saveJourneyAnswers(context, propertyAbout).flatMap {
      case Right(_) =>
        auditCYA(taxYear, request, propertyAbout, isFailed = false)
        Future.successful(Redirect(controllers.about.routes.AboutPropertyCompleteController.onPageLoad(taxYear)))
      case Left(error) =>
        logger.error(s"Failed to save About section: ${error.toString}")
        auditCYA(taxYear, request, propertyAbout, isFailed = true)
        Future.failed(SaveJourneyAnswersFailed("Failed to save About section"))
    }
  }

  private def auditCYA(taxYear: Int, request: DataRequest[AnyContent], propertyAbout: PropertyAbout, isFailed: Boolean)(
    implicit hc: HeaderCarrier
  ): Unit = {
    val auditModel = AuditModel(
      request.user.affinityGroup,
      request.user.nino,
      request.user.mtditid,
      taxYear,
      propertyType = AuditPropertyType.UKProperty,
      countryCode = Country.UK.code,
      journeyName = JourneyName.Rentals,
      sectionName = SectionName.About,
      accountingMethod = AccountingMethod.Cash,
      isUpdate = false,
      isFailed = isFailed,
      request.user.agentRef,
      PropertyAboutAudit(propertyAbout)
    )

    audit.sendRentalsAuditEvent(auditModel)
  }
}
