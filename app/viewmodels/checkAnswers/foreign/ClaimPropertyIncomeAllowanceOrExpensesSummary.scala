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

package viewmodels.checkAnswers.foreign

import models.{CheckMode, UserAnswers}
import pages.foreign.ClaimPropertyIncomeAllowanceOrExpensesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{keyCssClass, selectCountriesValueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ClaimPropertyIncomeAllowanceOrExpensesSummary {

  def row(taxYear: Int, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ClaimPropertyIncomeAllowanceOrExpensesPage).map { answer =>
      val value =
        if (answer) "claimPropertyIncomeAllowanceOrExpenses.PIA" else "claimPropertyIncomeAllowanceOrExpenses.expenses"

      SummaryListRowViewModel(
        key = KeyViewModel("claimPropertyIncomeAllowanceOrExpenses.checkYourAnswersLabel").withCssClass(keyCssClass),
        value = ValueViewModel(value).withCssClass(selectCountriesValueCssClass),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.foreign.routes.ClaimPropertyIncomeAllowanceOrExpensesController
              .onPageLoad(taxYear, CheckMode)
              .url
          )
            .withVisuallyHiddenText(messages("claimPropertyIncomeAllowanceOrExpenses.change.hidden"))
        )
      )
    }
}
