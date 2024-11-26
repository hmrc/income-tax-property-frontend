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

import models.{CheckMode, UserAnswers}
import pages.foreign.expenses.ForeignOtherAllowablePropertyExpensesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ForeignOtherAllowablePropertyExpensesSummary  {

  def row(taxYear: Int, countryCode: String, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ForeignOtherAllowablePropertyExpensesPage(countryCode)).map {
      answer =>

        SummaryListRowViewModel(
          key     = KeyViewModel("foreignOtherAllowablePropertyExpenses.checkYourAnswersLabel").withCssClass(valueCssClass),
          value   = ValueViewModel(bigDecimalCurrency(answer)).withCssClass(valueCssClass),
          actions = Seq(
            ActionItemViewModel("site.change", controllers.foreign.expenses.routes.ForeignOtherAllowablePropertyExpensesController.onPageLoad(taxYear, countryCode, CheckMode).url)
              .withVisuallyHiddenText(messages("foreignOtherAllowablePropertyExpenses.change.hidden"))
          )
        )
    }
}
