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

package viewmodels.checkAnswers

import controllers.foreign.routes
import models.{UserAnswers, CheckMode}
import pages.foreign.{SelectIncomeCountryPage, Country}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import service.CountryNamesDataSource
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.keyCssClass
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object SelectIncomeCountrySummary {

  def row(taxYear: Int, index: Int, answers: UserAnswers, currentLang: String)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(SelectIncomeCountryPage(index)).map { answer =>
      SummaryListRowViewModel(
        key = KeyViewModel("selectIncomeCountry.checkYourAnswersLabel").withCssClass(keyCssClass),
        value = ValueViewModel(HtmlFormat.escape(CountryNamesDataSource.getCountry(answer.code, currentLang).getOrElse(Country("", "")).name).toString),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.SelectIncomeCountryController.onPageLoad(taxYear, index, CheckMode).url
          )
            .withVisuallyHiddenText(messages("selectIncomeCountry.change.hidden"))
        )
      )
    }
}
