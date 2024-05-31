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

import controllers.ukrentaroom.expenses.routes.ConsolidatedRRExpensesController
import models.{CheckMode, ConsolidatedRRExpenses, UserAnswers}
import pages.ConsolidatedRRExpensesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ConsolidatedExpensesRRSummary {

  def row(taxYear: Int, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {

    val consolidated: Option[SummaryListRow] = Some(
      SummaryListRowViewModel(
        key = KeyViewModel("consolidatedRRExpenses.checkYourAnswersLabel").withCssClass(keyCssClass),
        value = ValueViewModel("Individual").withCssClass(valueCssClass),
        actions = Seq(
          ActionItemViewModel("site.change", ConsolidatedRRExpensesController.onPageLoad(taxYear, CheckMode).url)
            .withVisuallyHiddenText(messages("consolidatedRRExpenses.change.hidden"))
        )
      )
    )

    val individual: BigDecimal => Option[SummaryListRow] = amount =>
      Some(
        SummaryListRowViewModel(
          key = KeyViewModel("consolidatedRRExpenses.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel(bigDecimalCurrency(amount)).withCssClass(valueCssClass),
          actions = Seq(
            ActionItemViewModel("site.change", ConsolidatedRRExpensesController.onPageLoad(taxYear, CheckMode).url)
              .withVisuallyHiddenText(messages("consolidatedRRExpenses.change.hidden"))
          )
        )
      )

    answers.get(ConsolidatedRRExpensesPage) match {
      case Some(ConsolidatedRRExpenses(true, Some(amount)))  => individual(amount)
      case Some(ConsolidatedRRExpenses(true, None))          => ???
      case Some(ConsolidatedRRExpenses(false, None))         => consolidated
      case Some(ConsolidatedRRExpenses(false, Some(amount))) => ???
      case None                                              => ???
    }
  }
}
