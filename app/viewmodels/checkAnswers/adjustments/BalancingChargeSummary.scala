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

package viewmodels.checkAnswers.adjustments

import controllers.adjustments.routes
import models.{BalancingCharge, CheckMode, UserAnswers}
import pages.adjustments.BalancingChargePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.bigDecimalCurrency
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object BalancingChargeSummary  {

  def row(taxYear: Int, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    answers.get(BalancingChargePage).flatMap {
      case BalancingCharge(yesNo, amount) =>
        Some(SummaryListRowViewModel(
          key = "privateUseAdjustment.checkYourAnswersLabel",
          value = ValueViewModel(bigDecimalCurrency(amount.get)),
          actions = Seq(
            ActionItemViewModel("site.change", routes.BalancingChargeController.onPageLoad(taxYear, CheckMode).url)
              .withVisuallyHiddenText(messages("privateUseAdjustment.change.hidden"))
          )))
      case BalancingCharge(_, _) =>
        Some(SummaryListRowViewModel(
          key = "privateUseAdjustment.checkYourAnswersLabel",
          value = ValueViewModel("site.no"),
          actions = Seq(
            ActionItemViewModel("site.change", routes.BalancingChargeController.onPageLoad(taxYear, CheckMode).url)
              .withVisuallyHiddenText(messages("privateUseAdjustment.change.hidden"))
          )
        ))
      case _ => Option.empty[SummaryListRow]
    }
  }

}
