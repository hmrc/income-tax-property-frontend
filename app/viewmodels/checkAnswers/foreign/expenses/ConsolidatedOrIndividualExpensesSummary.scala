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

package viewmodels.checkAnswers.foreign.expenses

import controllers.foreign.expenses.routes.ConsolidatedOrIndividualExpensesController
import models.{CheckMode, ConsolidatedOrIndividualExpenses, UserAnswers}
import pages.foreign.expenses.ConsolidatedOrIndividualExpensesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ConsolidatedOrIndividualExpensesSummary {

  def row(taxYear: Int, countryCode: String, answers: UserAnswers)(implicit
                                                                   messages: Messages
  ): Option[SummaryListRow] =
    answers.get(ConsolidatedOrIndividualExpensesPage(countryCode)).flatMap {
      case ConsolidatedOrIndividualExpenses(true, amount) =>
        Some(SummaryListRowViewModel(
          key = KeyViewModel("consolidatedOrIndividualExpenses.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel(bigDecimalCurrency(amount.get)).withCssClass(valueCssClass),
          actions = Seq(
            ActionItemViewModel("site.change",
              ConsolidatedOrIndividualExpensesController.onPageLoad(taxYear, countryCode, CheckMode).url)
              .withVisuallyHiddenText(messages("consolidatedOrIndividualExpenses.change.hidden"))
          )))
      case ConsolidatedOrIndividualExpenses(false, _) =>
        Some(SummaryListRowViewModel(
          key = KeyViewModel("consolidatedOrIndividualExpenses.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel("consolidatedOrIndividualExpenses.checkYourAnswers.individual").withCssClass(valueCssClass),
          actions = Seq(
            ActionItemViewModel("site.change",
              ConsolidatedOrIndividualExpensesController.onPageLoad(taxYear, countryCode, CheckMode).url)
              .withVisuallyHiddenText(messages("consolidatedOrIndividualExpenses.change.hidden"))
          )
        ))
      case _ => Option.empty[SummaryListRow]
    }
}
