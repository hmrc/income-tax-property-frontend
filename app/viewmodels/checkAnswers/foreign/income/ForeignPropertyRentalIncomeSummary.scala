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

import controllers.foreign.income.routes
import models.{CheckMode, UserAnswers}
import pages.foreign.income.ForeignPropertyRentalIncomePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ForeignPropertyRentalIncomeSummary {

  def row(taxYear: Int, answers: UserAnswers, countryCode: String)(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    answers.get(ForeignPropertyRentalIncomePage(countryCode)).map { answer =>
      SummaryListRowViewModel(
        key = KeyViewModel("foreignPropertyRentalIncome.checkYourAnswersLabel").withCssClass(keyCssClass),
        value = ValueViewModel(bigDecimalCurrency(answer)).withCssClass(valueCssClass),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.ForeignPropertyRentalIncomeController.onPageLoad(taxYear, CheckMode, countryCode).url
          )
            .withVisuallyHiddenText(messages("foreignPropertyRentalIncome.change.hidden"))
        )
      )
    }

}
