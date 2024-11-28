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

import controllers.actions._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.foreign.income.{ForeignPropertyRentalIncomeSummary, ForeignReversePremiumsReceivedSummary}
import viewmodels.checkAnswers.foreign._
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.income.ForeignPropertyIncomeCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignPropertyIncomeCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
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
          ForeignYearLeaseAmountSummary.row(taxYear, countryCode, request.userAnswers),
          ForeignPremiumsGrantLeaseSummary.row(taxYear, countryCode, request.userAnswers),
          ForeignReversePremiumsReceivedSummary.row(taxYear, countryCode, request.userAnswers),
          ForeignOtherIncomeFromPropertySummary.row(taxYear, countryCode, request.userAnswers)
        ).flatten
      )

      Ok(view(list, taxYear, countryCode))
    }

  def onSubmit(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      Future.successful(
        Redirect(
          controllers.foreign.income.routes.ForeignIncomeSectionCompleteController.onPageLoad(taxYear, countryCode)
        )
      )
    }
}
