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

import controllers.actions._
import controllers.foreign.expenses.routes.ForeignExpensesSectionCompleteController
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.foreign.expenses._
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.expenses.ForeignPropertyExpensesCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignPropertyExpensesCheckYourAnswersController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         val controllerComponents: MessagesControllerComponents,
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
      Future.successful(Redirect(ForeignExpensesSectionCompleteController.onPageLoad(taxYear, countryCode)))
  }
}
