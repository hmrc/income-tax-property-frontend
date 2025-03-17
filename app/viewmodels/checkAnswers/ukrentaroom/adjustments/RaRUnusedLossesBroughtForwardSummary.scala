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

package viewmodels.checkAnswers.ukrentaroom.adjustments

import controllers.ukrentaroom.adjustments.routes
import models.{CheckMode, UnusedLossesBroughtForward, UserAnswers}
import pages.ukrentaroom.adjustments.RaRUnusedLossesBroughtForwardPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object RaRUnusedLossesBroughtForwardSummary  {

  def row(taxYear: Int, answers: UserAnswers, individualOrAgent: String)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(RaRUnusedLossesBroughtForwardPage).flatMap {
      answer =>
        val row: Content => Option[SummaryListRow] = content => Some(
          SummaryListRowViewModel(
            key     = KeyViewModel(s"raRUnusedLossesBroughtForward.checkYourAnswersLabel.${individualOrAgent}").withCssClass(keyCssClass),
            value   = ValueViewModel(content).withCssClass(valueCssClass),
            actions = Seq(
              ActionItemViewModel("site.change", routes.RaRUnusedLossesBroughtForwardController.onPageLoad(taxYear, CheckMode).url)
                .withVisuallyHiddenText(messages("raRUnusedLossesBroughtForward.change.hidden"))
            )
          )
        )

        answer match {
          case UnusedLossesBroughtForward(true, Some(amount)) =>
            row(bigDecimalCurrency(amount))
          case UnusedLossesBroughtForward(false, _) =>
            row("site.no")
          case _ =>
            Option.empty[SummaryListRow]
        }
    }
}
