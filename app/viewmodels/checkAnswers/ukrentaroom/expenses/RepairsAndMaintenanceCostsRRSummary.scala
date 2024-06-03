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

import controllers.ukrentaroom.expenses.routes
import models.{CheckMode, UserAnswers}
import pages.ukrentaroom.expenses.RepairsAndMaintenanceCostsRRPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
object RepairsAndMaintenanceCostsRRSummary {

  def row(taxYear: Int, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(RepairsAndMaintenanceCostsRRPage).map { answer =>
      SummaryListRowViewModel(
        key = KeyViewModel("repairsAndMaintenanceCostsRR.checkYourAnswersLabel").withCssClass(keyCssClass),
        value = ValueViewModel(bigDecimalCurrency(answer)).withCssClass(valueCssClass),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.RepairsAndMaintenanceCostsRRController.onPageLoad(taxYear, CheckMode).url
          )
            .withVisuallyHiddenText(messages("repairsAndMaintenanceCostsRR.change.hidden"))
        )
      )
    }
}
