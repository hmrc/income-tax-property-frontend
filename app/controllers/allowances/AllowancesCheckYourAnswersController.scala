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

package controllers.allowances

import controllers.actions._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.allowances._
import viewmodels.govuk.summarylist._
import views.html.allowances.AllowancesCheckYourAnswersView

import javax.inject.Inject

class AllowancesCheckYourAnswersController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: AllowancesCheckYourAnswersView
                                     ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
      implicit request =>

        val list = SummaryListViewModel(
          rows = Seq(
            CapitalAllowancesForACarSummary.row(taxYear, request.userAnswers),
            AnnualInvestmentAllowanceSummary.row(taxYear, request.userAnswers),
            ElectricChargePointAllowanceSummary.row(taxYear, request.userAnswers),
            ZeroEmissionCarAllowanceSummary.row(taxYear, request.userAnswers),
            ZeroEmissionGoodsVehicleAllowanceSummary.row(taxYear, request.userAnswers),
            BusinessPremisesRenovationSummary.row(taxYear, request.userAnswers),
            ReplacementOfDomesticGoodsSummary.row(taxYear, request.userAnswers),
            OtherCapitalAllowanceSummary.row(taxYear, request.userAnswers)
          ).flatten
        )

        Ok(view(list, taxYear))
  }
}
