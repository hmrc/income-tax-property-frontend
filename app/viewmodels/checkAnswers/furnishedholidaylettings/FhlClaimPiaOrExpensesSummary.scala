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

package viewmodels.checkAnswers.furnishedholidaylettings

import models.{CheckMode, UserAnswers}
import pages.furnishedholidaylettings.FhlClaimPiaOrExpensesPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object FhlClaimPiaOrExpensesSummary  {

  def row(taxYear: Int, answers: UserAnswers, individualOrAgent: String)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(FhlClaimPiaOrExpensesPage).map {
      answer =>

        val value = ValueViewModel(
          HtmlContent(
            HtmlFormat.escape(messages(s"fhlClaimPiaOrExpenses.$answer"))
          )
        )

        SummaryListRowViewModel(
          key     = s"fhlClaimPiaOrExpenses.checkYourAnswersLabel.$individualOrAgent",
          value   = value,
          actions = Seq(
            ActionItemViewModel("site.change", controllers.furnishedholidaylettings.routes.FhlClaimPiaOrExpensesController.onPageLoad(taxYear, CheckMode).url)
              .withVisuallyHiddenText(messages("fhlClaimPiaOrExpenses.change.hidden"))
          )
        )
    }
}
