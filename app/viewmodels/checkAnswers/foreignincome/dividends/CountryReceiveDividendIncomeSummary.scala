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

import models.{UserAnswers, CheckMode}
import pages.CountryReceiveDividendIncomePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import controllers.foreignincome.dividends.routes.CountryReceiveDividendIncomeController
import pages.foreign.Country
import service.CountryNamesDataSource
import viewmodels.checkAnswers.FormatUtils.keyCssClass

object CountryReceiveDividendIncomeSummary  {

  def row(taxYear: Int, answers: UserAnswers, currentLang: String)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(CountryReceiveDividendIncomePage).map {
      answer =>

        SummaryListRowViewModel(
          key     = KeyViewModel("countryReceiveDividendIncome.checkYourAnswersLabel").withCssClass(keyCssClass),
          value   = ValueViewModel(HtmlFormat.escape(CountryNamesDataSource.getCountry(answer.code, currentLang).getOrElse(Country("", "")).name).toString),
          actions = Seq(
            ActionItemViewModel("site.change", CountryReceiveDividendIncomeController.onPageLoad(taxYear, CheckMode).url)
              .withVisuallyHiddenText(messages("countryReceiveDividendIncome.change.hidden"))
          )
        )
    }
}
