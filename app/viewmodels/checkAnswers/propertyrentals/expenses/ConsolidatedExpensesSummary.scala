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
import models.{CheckMode, ConsolidatedExpenses, PropertyType, UserAnswers}
import pages.propertyrentals.expenses.ConsolidatedExpensesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ConsolidatedExpensesSummary {

  def rows(taxYear: Int, answers: UserAnswers, individualOrAgent: String, propertyType: PropertyType)(implicit
    messages: Messages
  ): Option[Seq[SummaryListRow]] =
    answers.get(ConsolidatedExpensesPage(propertyType)).flatMap {
      case ConsolidatedExpenses(true, Some(amount)) =>
        Some(
          Seq(
            SummaryListRowViewModel(
              key = KeyViewModel("consolidatedExpenses.checkYourAnswersLabel.type").withCssClass(keyCssClass),
              value = ValueViewModel("consolidatedExpenses.yes").withCssClass(valueCssClass),
              actions = Seq(
                ActionItemViewModel(
                  "site.change",
                  routes.ConsolidatedExpensesController.onPageLoad(taxYear, CheckMode, propertyType).url
                )
                  .withVisuallyHiddenText(messages("consolidatedExpenses.change.hidden"))
              )
            ),
            SummaryListRowViewModel(
              key = KeyViewModel(s"consolidatedExpenses.checkYourAnswersLabel.amount.$individualOrAgent")
                .withCssClass(keyCssClass),
              value = ValueViewModel(bigDecimalCurrency(amount)).withCssClass(valueCssClass),
              actions = Seq(
                ActionItemViewModel(
                  "site.change",
                  routes.ConsolidatedExpensesController.onPageLoad(taxYear, CheckMode, propertyType).url
                )
                  .withVisuallyHiddenText(messages("consolidatedExpenses.change.hidden"))
              )
            )
          )
        )
      case ConsolidatedExpenses(false, _) =>
        Some(
          Seq(
            SummaryListRowViewModel(
              key = KeyViewModel("consolidatedExpenses.checkYourAnswersLabel.type").withCssClass(keyCssClass),
              value = ValueViewModel("consolidatedRRExpenses.no").withCssClass(valueCssClass),
              actions = Seq(
                ActionItemViewModel(
                  "site.change",
                  routes.ConsolidatedExpensesController.onPageLoad(taxYear, CheckMode, propertyType).url
                )
                  .withVisuallyHiddenText(messages("consolidatedExpenses.change.hidden"))
              )
            )
          )
        )
      case _ => Option.empty
    }

}
