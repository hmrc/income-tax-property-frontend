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

package controllers.propertyrentals.expenses

import audit.{AuditModel, AuditService, PropertyRentalsExpense}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes
import models.requests.DataRequest
import pages.PageConstants
import pages.propertyrentals.expenses.ConsolidatedExpensesPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.propertyrentals.expenses._
import viewmodels.govuk.summarylist._
import views.html.propertyrentals.expenses.ExpensesCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.Future


class ExpensesCheckYourAnswersController @Inject()(
                                                    override val messagesApi: MessagesApi,
                                                    identify: IdentifierAction,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    view: ExpensesCheckYourAnswersView,
                                                    audit: AuditService
                                                  ) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val rows: Seq[SummaryListRow] = if
      (request.userAnswers.get(ConsolidatedExpensesPage).exists(expenses => expenses.consolidatedExpensesYesNo)) {
        Seq(
          ConsolidatedExpensesSummary.row(taxYear, request.userAnswers)
        ).flatten
      } else {
        Seq(
          ConsolidatedExpensesSummary.row(taxYear, request.userAnswers),
          RentsRatesAndInsuranceSummary.row(taxYear, request.userAnswers),
          RepairsAndMaintenanceCostsSummary.row(taxYear, request.userAnswers),
          LoanInterestSummary.row(taxYear, request.userAnswers),
          OtherProfessionalFeesSummary.row(taxYear, request.userAnswers),
          CostsOfServicesProvidedSummary.row(taxYear, request.userAnswers),
          PropertyBusinessTravelCostsSummary.row(taxYear, request.userAnswers),
          OtherAllowablePropertyExpensesSummary.row(taxYear, request.userAnswers)
        ).flatten
      }
      val list = SummaryListViewModel(rows = rows)

      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(PropertyRentalsExpense) match {
        case Some(propertyRentalsExpense) =>
          auditCYA(taxYear, request, propertyRentalsExpense)
        case None =>
          logger.error(s"${PageConstants.propertyRentalsExpense} section is not present in userAnswers")
      }
      Future.successful(Redirect(routes.SummaryController.show(taxYear)))
  }

  private def auditCYA(taxYear: Int, request: DataRequest[AnyContent], propertyRentalsExpense: PropertyRentalsExpense)(implicit hc: HeaderCarrier): Unit = {
    val auditModel = AuditModel(
      request.user.nino,
      request.user.affinityGroup,
      request.user.mtditid,
      taxYear,
      isUpdate = false,
      "PropertyRentalsExpense",
      propertyRentalsExpense)

    audit.sendRentalsAuditEvent(auditModel)
  }
}
