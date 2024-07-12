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

package viewmodels.checkAnswers.ukrentaroom.expenses

import controllers.ukrentaroom.expenses.routes
import models.{CheckMode, ConsolidatedRRExpenses, UserAnswers}
import pages.ukrentaroom.expenses.ConsolidatedExpensesRRPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ConsolidatedExpensesRRSummary {

  def rows(taxYear: Int, individualOrAgent: String, answers: UserAnswers)(implicit
    messages: Messages
  ): Option[Seq[SummaryListRow]] =
    answers.get(ConsolidatedExpensesRRPage).flatMap {
      case ConsolidatedRRExpenses(true, Some(amount)) =>
        Some(
          Seq(
            SummaryListRowViewModel(
              key = KeyViewModel("consolidatedRRExpenses.checkYourAnswersLabel.type").withCssClass(keyCssClass),
              value = ValueViewModel("consolidatedRRExpenses.yes").withCssClass(valueCssClass),
              actions = Seq(
                ActionItemViewModel(
                  "site.change",
                  routes.ConsolidatedExpensesRRController.onPageLoad(taxYear, CheckMode).url
                )
                  .withVisuallyHiddenText(messages("consolidatedRRExpenses.change.hidden"))
              )
            ),
            SummaryListRowViewModel(
              key = KeyViewModel(s"consolidatedRRExpenses.checkYourAnswersLabel.amount.$individualOrAgent")
                .withCssClass(keyCssClass),
              value = ValueViewModel(bigDecimalCurrency(amount)).withCssClass(valueCssClass),
              actions = Seq(
                ActionItemViewModel(
                  "site.change",
                  routes.ConsolidatedExpensesRRController.onPageLoad(taxYear, CheckMode).url
                )
                  .withVisuallyHiddenText(messages("consolidatedRRExpenses.change.hidden"))
              )
            )
          )
        )
      case ConsolidatedRRExpenses(false, _) =>
        Some(
          Seq(
            SummaryListRowViewModel(
              key = KeyViewModel("consolidatedRRExpenses.checkYourAnswersLabel.type").withCssClass(keyCssClass),
              value = ValueViewModel("consolidatedRRExpenses.no").withCssClass(valueCssClass),
              actions = Seq(
                ActionItemViewModel(
                  "site.change",
                  routes.ConsolidatedExpensesRRController.onPageLoad(taxYear, CheckMode).url
                )
                  .withVisuallyHiddenText(messages("consolidatedRRExpenses.change.hidden"))
              )
            )
          )
        )
      case _ => Option.empty
    }
}
