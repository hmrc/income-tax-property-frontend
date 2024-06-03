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

package controllers.ukrentaroom.expenses

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.ukrentaroom.expenses.ConsolidatedRRExpensesPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.ukrentaroom.expenses._
import viewmodels.govuk.summarylist._
import views.html.ukrentaroom.expenses.RaRExpensesCheckYourAnswersView

import scala.concurrent.Future

class ExpensesCheckYourAnswersRRController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: RaRExpensesCheckYourAnswersView
) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val rows: Seq[SummaryListRow] =
        if (request.userAnswers.get(ConsolidatedRRExpensesPage).exists(_.areExpensesConsolidated)) {
          Seq(
            ConsolidatedExpensesRRSummary.row(taxYear, request.userAnswers)
          ).flatten
        } else {
          Seq(
            ConsolidatedExpensesRRSummary
              .row(taxYear, request.userAnswers),
            RentsRatesAndInsuranceRRSummary
              .row(taxYear, request.userAnswers),
            RepairsAndMaintenanceCostsRRSummary
              .row(taxYear, request.userAnswers),
            LegalManagementOtherFeeSummary
              .row(taxYear, request.userAnswers),
            CostOfServicesProvidedSummary
              .row(taxYear, request.userAnswers),
            ResidentialPropertyFinanceCostsSummary
              .row(taxYear, request.userAnswers, request.user.isAgentMessageKey),
            UnusedResidentialPropertyFinanceCostsBroughtFwdSummary
              .row(taxYear, request.userAnswers, request.user.isAgentMessageKey),
            OtherPropertyExpensesRRSummary.row(taxYear, request.userAnswers)
          ).flatten
        }
      val list = SummaryListViewModel(rows = rows)

      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      Future.successful(
        Redirect(controllers.routes.SummaryController.show(taxYear))
      )
  }
}
