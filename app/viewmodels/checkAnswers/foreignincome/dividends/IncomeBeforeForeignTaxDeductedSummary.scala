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

import controllers.foreignincome.dividends.routes
import models.{CheckMode, UserAnswers}
import pages.foreign.Country
import pages.foreignincome.IncomeBeforeForeignTaxDeductedPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object IncomeBeforeForeignTaxDeductedSummary  {

  def row(taxYear: Int, country: Country, answers: UserAnswers, individualOrAgent: String)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(IncomeBeforeForeignTaxDeductedPage(country.code)).map {
      answer =>

        SummaryListRowViewModel(
          key     = KeyViewModel(messages(s"incomeBeforeForeignTaxDeducted.checkYourAnswersLabel.$individualOrAgent", country.name)).withCssClass(keyCssClass),
          value   = ValueViewModel(bigDecimalCurrency(answer)).withCssClass(valueCssClass),
          actions = Seq(
            ActionItemViewModel("site.change", routes.IncomeBeforeForeignTaxDeductedController.onPageLoad(taxYear, country.code, CheckMode).url)
              .withVisuallyHiddenText(messages("incomeBeforeForeignTaxDeducted.change.hidden"))
          )
        )
    }
}
