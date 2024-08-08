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

package viewmodels.checkAnswers.propertyrentals.expenses

import controllers.propertyrentals.expenses.routes
import models.{CheckMode, Rentals, UserAnswers}
import pages.propertyrentals.expenses.PropertyBusinessTravelCostsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object PropertyBusinessTravelCostsSummary {

  def row(taxYear: Int, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(PropertyBusinessTravelCostsPage(Rentals)) match {
      case Some(answer) =>
        Some(
          SummaryListRowViewModel(
            key = KeyViewModel("propertyBusinessTravelCosts.checkYourAnswersLabel").withCssClass(keyCssClass),
            value = ValueViewModel(bigDecimalCurrency(answer)).withCssClass(valueCssClass),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                routes.PropertyBusinessTravelCostsController.onPageLoad(taxYear, CheckMode, Rentals).url
              )
                .withVisuallyHiddenText(messages("propertyBusinessTravelCosts.change.hidden"))
            )
          )
        )
      case _ => Option.empty[SummaryListRow]
    }
}
