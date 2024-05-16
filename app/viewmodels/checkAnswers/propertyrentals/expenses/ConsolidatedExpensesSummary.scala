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

import controllers.propertyrentals.expenses.routes._
import models.TotalIncomeUtils.isTotalIncomeUnder85K
import models.{CheckMode, ConsolidatedExpenses, PropertyType, Rentals, UserAnswers}
import pages.PageConstants
import pages.propertyrentals.expenses.ConsolidatedExpensesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ConsolidatedExpensesSummary {

  def row(taxYear: Int, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    if (isTotalIncomeUnder85K(answers)) {
      answers.get(ConsolidatedExpensesPage(PageConstants.propertyRentalsExpense)).flatMap {
        case ConsolidatedExpenses(true, Some(amount)) =>
          Some(SummaryListRowViewModel(
            key = KeyViewModel("consolidatedExpenses.checkYourAnswersLabel").withCssClass(keyCssClass),
            value = ValueViewModel(bigDecimalCurrency(amount)).withCssClass(valueCssClass),
            actions = Seq(
              ActionItemViewModel("site.change", ConsolidatedExpensesController.onPageLoad(taxYear, CheckMode, Rentals).url)
                .withVisuallyHiddenText(messages("consolidatedExpenses.change.hidden"))
            )))
        case ConsolidatedExpenses(false, _) =>
          Some(SummaryListRowViewModel(
            key = KeyViewModel("consolidatedExpenses.checkYourAnswersLabel").withCssClass(keyCssClass),
            value = ValueViewModel("site.no").withCssClass(valueCssClass),
            actions = Seq(
              ActionItemViewModel("site.change", ConsolidatedExpensesController.onPageLoad(taxYear, CheckMode, Rentals).url)
                .withVisuallyHiddenText(messages("consolidatedExpenses.change.hidden"))
            )
          ))
        case _ => Option.empty[SummaryListRow]
      }
    } else {
      Option.empty[SummaryListRow]
    }
  }
}
