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

package viewmodels.checkAnswers.foreign

import models.{CheckMode, PremiumCalculated, UserAnswers}
import pages.foreign.CalculatedPremiumLeaseTaxablePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CalculatedPremiumLeaseTaxableSummary {

  def row(taxYear: Int, countryCode: String, answers: UserAnswers, individualOrAgent: String)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(CalculatedPremiumLeaseTaxablePage(countryCode)).flatMap {
      case PremiumCalculated(true, _) =>
        Some(SummaryListRowViewModel(
          key = KeyViewModel(s"calculatedPremiumLeaseTaxable.checkYourAnswersLabel.$individualOrAgent").withCssClass(keyCssClass),
          value = ValueViewModel("site.yes").withCssClass(valueCssClass),
          actions = Seq(
            ActionItemViewModel("site.change",
              controllers.foreign.routes.CalculatedPremiumLeaseTaxableController.onPageLoad(taxYear, countryCode, CheckMode).url)
              .withVisuallyHiddenText(messages("calculatedPremiumLeaseTaxable.change.hidden"))
          )))
      case PremiumCalculated(false, _) =>
        Some(SummaryListRowViewModel(
          key = KeyViewModel(s"calculatedPremiumLeaseTaxable.checkYourAnswersLabel.$individualOrAgent").withCssClass(keyCssClass),
          value = ValueViewModel("site.no").withCssClass(valueCssClass),
          actions = Seq(
            ActionItemViewModel("site.change",
              controllers.foreign.routes.CalculatedPremiumLeaseTaxableController.onPageLoad(taxYear, countryCode, CheckMode).url)
              .withVisuallyHiddenText(messages("calculatedPremiumLeaseTaxable.change.hidden"))
          )
        ))
      case _ => Option.empty[SummaryListRow]
    }
}
