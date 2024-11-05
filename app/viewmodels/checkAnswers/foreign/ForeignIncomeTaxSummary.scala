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

import controllers.foreign.routes
import models.{CheckMode, ForeignIncomeTax, UserAnswers}
import pages.foreign.ForeignIncomeTaxPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ForeignIncomeTaxSummary {

  def row(taxYear: Int, individualOrAgent: String, countryCode: String, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ForeignIncomeTaxPage(countryCode)).flatMap { answer =>
      val row: Content => Option[SummaryListRow] = content => Some(
        SummaryListRowViewModel(
          key = KeyViewModel(s"foreignIncomeTax.checkYourAnswersLabel.$individualOrAgent").withCssClass(keyCssClass),
          value = ValueViewModel(content).withCssClass(valueCssClass),
          actions = Seq(
            ActionItemViewModel("site.change", routes.ForeignIncomeTaxController.onPageLoad(taxYear, countryCode, CheckMode).url)
              .withVisuallyHiddenText(messages(s"foreignIncomeTax.change.hidden.$individualOrAgent"))
          )
        )
      )

      answer match {
        case ForeignIncomeTax(true, Some(amount)) =>
          row(bigDecimalCurrency(amount))
        case ForeignIncomeTax(false, _) =>
          row("site.no")
        case _ =>
          Option.empty[SummaryListRow]
      }
    }
}
