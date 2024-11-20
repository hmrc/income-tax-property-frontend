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
import forms.ForeignPropertyIncomeCheckYourAnswersFormProvider

import javax.inject.Inject
import models.{Mode, PropertyType}
import navigation.Navigator
import pages.ForeignPropertyIncomeCheckYourAnswersPage
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.foreign.income.ForeignPropertyIncomeCheckYourAnswersView
import viewmodels.govuk.all.SummaryListViewModel
import viewmodels.checkAnswers.foreign.{PremiumsGrantLeaseYNSummary, ForeignYearLeaseAmountSummary}
import viewmodels.checkAnswers.foreign.income.ForeignPropertyRentalIncomeSummary
import viewmodels.checkAnswers.premiumlease.{CalculatedFigureYourselfSummary, ReceivedGrantLeaseAmountSummary, PremiumsGrantLeaseSummary}
import viewmodels.checkAnswers.propertyrentals.income.{OtherIncomeFromPropertySummary, ReversePremiumsReceivedSummary}

import scala.concurrent.{ExecutionContext, Future}

class ForeignPropertyIncomeCheckYourAnswersController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: ForeignPropertyIncomeCheckYourAnswersFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: ForeignPropertyIncomeCheckYourAnswersView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, countryCode: String, propertyType: PropertyType): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val list = SummaryListViewModel(
        rows = Seq(
          ForeignPropertyRentalIncomeSummary.row(taxYear, request.userAnswers, countryCode),
          PremiumsGrantLeaseYNSummary.row(request.userAnswers, taxYear, countryCode),
          CalculatedFigureYourselfSummary.row(taxYear, request.userAnswers, propertyType),
          ReceivedGrantLeaseAmountSummary.row(taxYear, request.userAnswers, propertyType),
          ForeignYearLeaseAmountSummary.row(taxYear, countryCode, request.userAnswers),
          PremiumsGrantLeaseSummary.row(taxYear, request.userAnswers, propertyType),
          ReversePremiumsReceivedSummary.row(taxYear, request.userAnswers, propertyType),
          OtherIncomeFromPropertySummary.row(taxYear, request.userAnswers, propertyType),
        ).flatten
      )

      Ok(view(list, taxYear, countryCode))
  }

  def onSubmit(taxYear: Int, countryCode: String, propertyType: PropertyType): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      Future.successful(Redirect(controllers.foreign.income.routes.ForeignPropertyIncomeCheckYourAnswersController.onPageLoad(taxYear, countryCode, propertyType)))
  }
}
