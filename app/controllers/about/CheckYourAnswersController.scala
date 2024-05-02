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

import audit.{AuditModel, AuditService, PropertyAbout}
import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes
import models.JourneyContext
import models.requests.DataRequest
import pages.ReportPropertyIncomePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.about.{ReportPropertyIncomeSummary, TotalIncomeSummary, UKPropertySelectSummary}
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

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
      val ukPropertyRow = UKPropertySelectSummary.row(taxYear, request.userAnswers)

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
      val propertyAbout = request.userAnswers.get(PropertyAbout).getOrElse {
        logger.error("PropertyAbout Section is not present in userAnswers")
        throw new NoSuchElementException("PropertyAbout Section is not present in userAnswers")
      }
      val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, "property-about")

      propertySubmissionService.saveJourneyAnswers(context, propertyAbout).map {
        case Right(_) =>
          auditCYA(taxYear, request, propertyAbout)
          Redirect(routes.SummaryController.show(taxYear))
        case Left(_) => InternalServerError
      }

  }

  private def auditCYA(taxYear: Int, request: DataRequest[AnyContent], propertyAbout: PropertyAbout)(implicit
    hc: HeaderCarrier
  ): Unit = {
    val auditModel = AuditModel(
      request.user.nino,
      request.user.affinityGroup,
      request.user.mtditid,
      taxYear,
      isUpdate = false,
      "PropertyAbout",
      propertyAbout
    )

    audit.sendRentalsAuditEvent(auditModel)
  }
}
