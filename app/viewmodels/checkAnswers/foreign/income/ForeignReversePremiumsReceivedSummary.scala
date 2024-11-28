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

package viewmodels.checkAnswers.foreign.income

import models.{CheckMode, UserAnswers}
import pages.foreign.income.ForeignReversePremiumsReceivedPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ForeignReversePremiumsReceivedSummary {

  def row(taxYear: Int, countryCode: String, answers: UserAnswers)(implicit
    messages: Messages
  ): Option[SummaryListRow] = {

    val value = answers.get(ForeignReversePremiumsReceivedPage(countryCode)).map { answer =>
      answer.amount.fold("site.no")(value => value.toString)
    }

    answers.get(ForeignReversePremiumsReceivedPage(countryCode)).map { answer =>
      SummaryListRowViewModel(
        key = KeyViewModel("reversePremiumsReceived.checkYourAnswersLabel").withCssClass(keyCssClass),
        value = ValueViewModel(value.get).withCssClass(valueCssClass),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.foreign.income.routes.ForeignReversePremiumsReceivedController
              .onPageLoad(taxYear, countryCode, CheckMode)
              .url
          )
            .withVisuallyHiddenText(messages("reversePremiumsReceived.change.hidden"))
        )
      )
    }
  }
}
