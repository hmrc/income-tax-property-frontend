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

package viewmodels.checkAnswers.foreign.adjustments

import controllers.routes
import models.{CheckMode, UnusedLossesPreviousYears, UserAnswers}
import pages.foreign.adjustments.ForeignUnusedLossesPreviousYearsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ForeignUnusedLossesPreviousYearsSummary  {

  def row(taxYear: Int, countryCode: String, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ForeignUnusedLossesPreviousYearsPage(countryCode)).flatMap {
      case UnusedLossesPreviousYears(true, amount) =>
        Some(SummaryListRowViewModel(
          key     = KeyViewModel("foreignUnusedLossesPreviousYears.checkYourAnswersLabel").withCssClass(keyCssClass),
          value   = ValueViewModel(bigDecimalCurrency(amount.get)).withCssClass(valueCssClass),
          actions = Seq(
            ActionItemViewModel("site.change", controllers.foreign.adjustments.routes.ForeignUnusedLossesPreviousYearsController.onPageLoad(taxYear, countryCode, CheckMode).url)
              .withVisuallyHiddenText(messages("foreignUnusedLossesPreviousYears.change.hidden"))
          )
        ))
      case UnusedLossesPreviousYears(false, _) =>
        Some(SummaryListRowViewModel(
          key     = KeyViewModel("foreignUnusedLossesPreviousYears.checkYourAnswersLabel").withCssClass(keyCssClass),
          value   = ValueViewModel("site.no").withCssClass(valueCssClass),
          actions = Seq(
            ActionItemViewModel("site.change", controllers.foreign.adjustments.routes.ForeignUnusedLossesPreviousYearsController.onPageLoad(taxYear, countryCode, CheckMode).url)
              .withVisuallyHiddenText(messages("foreignUnusedLossesPreviousYears.change.hidden"))
          )
        ))
      case _ => Option.empty[SummaryListRow]
    }
}
