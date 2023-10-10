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

package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, ReversePremiumsReceived, UserAnswers}
import pages.ReversePremiumsReceivedPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.implicits._
import viewmodels.govuk.summarylist._


object ReversePremiumsReceivedSummary {

  def rowBoolean(taxYear: Int, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =

    answers.get(ReversePremiumsReceivedPage).map {
      case ReversePremiumsReceived(false, _) =>
        SummaryListRowViewModel(
          key = "reversePremiumsReceived.checkYourAnswersLabel",
          value = ValueViewModel("site.no"),
          actions = Seq(
            ActionItemViewModel("site.change", routes.ReversePremiumsReceivedController.onPageLoad(taxYear, CheckMode).url)
              .withVisuallyHiddenText(messages("reversePremiumsReceived.change.hidden"))
          )

        )
      case ReversePremiumsReceived(true, _) =>
        SummaryListRowViewModel(
          key = "reversePremiumsReceived.checkYourAnswersLabel",
          value = ValueViewModel("site.yes"),
          actions = Seq(
            ActionItemViewModel("site.change", routes.ReversePremiumsReceivedController.onPageLoad(taxYear, CheckMode).url)
              .withVisuallyHiddenText(messages("reversePremiumsReceived.change.hidden"))
          ))
    }

  def rowAmount(taxYear: Int, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    answers.get(ReversePremiumsReceivedPage).map {
      case ReversePremiumsReceived(true, Some(amount)) =>
        SummaryListRowViewModel(
          key = "reversePremiumsReceived.checkYourAnswersLabel",
          value = ValueViewModel(s"£$amount"),
          actions = Seq(
            ActionItemViewModel("site.change", routes.ReversePremiumsReceivedController.onPageLoad(taxYear, CheckMode).url)
              .withVisuallyHiddenText(messages("reversePremiumsReceived.change.hidden"))
          ))

    }
  }
}
