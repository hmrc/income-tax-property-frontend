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

package viewmodels.checkAnswers.foreignincome.dividends

import controllers.foreignincome.dividends.routes.ForeignTaxDeductedFromDividendIncomeController
import models.{CheckMode, UserAnswers}
import pages.foreign.Country
import pages.foreignincome.dividends.ForeignTaxDeductedFromDividendIncomePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ForeignTaxDeductedFromDividendIncomeSummary  {

  def row(taxYear: Int, country: Country, individualOrAgent: String, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ForeignTaxDeductedFromDividendIncomePage(country.code)).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = KeyViewModel(messages(s"foreignTaxDeductedFromDividendIncome.checkYourAnswersLabel.${individualOrAgent}", country.name)).withCssClass(keyCssClass),
          value   = ValueViewModel(value).withCssClass(valueCssClass),
          actions = Seq(
            ActionItemViewModel("site.change", ForeignTaxDeductedFromDividendIncomeController.onPageLoad(taxYear, country.code, CheckMode).url)
              .withVisuallyHiddenText(messages(s"foreignTaxDeductedFromDividendIncome.change.hidden.${individualOrAgent}", country.name))
          )
        )
    }
}
