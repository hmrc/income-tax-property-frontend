/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels.checkAnswers.ukandforeignproperty

import controllers.ukandforeignproperty.routes
import models.{CheckMode, UkAndForeignPropertyClaimExpensesOrRelief, UserAnswers}
import pages.ukandforeignproperty.UkAndForeignPropertyClaimExpensesOrReliefPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{keyCssClass, valueCssClass}
import viewmodels.govuk.all.{ActionItemViewModel, FluentActionItem, FluentKey, FluentValue, KeyViewModel, SummaryListRowViewModel, ValueViewModel}
import viewmodels.implicits._

object UKAndForeignClaimExpensesOrReliefSummary {

  def row(taxYear: Int, answers: UserAnswers)(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    answers.get(UkAndForeignPropertyClaimExpensesOrReliefPage).flatMap {
      case UkAndForeignPropertyClaimExpensesOrRelief(true) =>
        Some(
          SummaryListRowViewModel(
            key = KeyViewModel("claimExpensesOrRelief.checkYourAnswersLabel.type").withCssClass(keyCssClass),
            value = ValueViewModel("claimExpensesOrRelief.yes").withCssClass(valueCssClass),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                routes.UkAndForeignPropertyClaimExpensesOrReliefController.onPageLoad(taxYear, CheckMode).url
              )
                .withVisuallyHiddenText(messages("claimExpensesOrRelief.change.hidden"))
            )
          )
        )
      case UkAndForeignPropertyClaimExpensesOrRelief(false) =>
        Some(
          SummaryListRowViewModel(
            key = KeyViewModel("claimExpensesOrRelief.checkYourAnswersLabel.type").withCssClass(keyCssClass),
            value = ValueViewModel("claimExpensesOrRelief.no").withCssClass(valueCssClass),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                routes.UkAndForeignPropertyClaimExpensesOrReliefController.onPageLoad(taxYear, CheckMode).url
              )
                .withVisuallyHiddenText(messages("claimExpensesOrRelief.change.hidden"))
            )
          )
        )
      case _ => Option.empty
    }

}
