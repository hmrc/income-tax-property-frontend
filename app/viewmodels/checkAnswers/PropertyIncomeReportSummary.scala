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

import models.{UserAnswers, CheckMode}
import pages.foreign.PropertyIncomeReportPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{selectCountriesValueAlignLeftCssClass, keyAlignLeftCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object PropertyIncomeReportSummary {

  def row(taxYear: Int, individualOrAgent: String, answers: UserAnswers)(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    answers.get(PropertyIncomeReportPage).map { answer =>
      val value = if (answer) {
        s"propertyIncomeReport.reportPropertyIncome.$individualOrAgent"
      } else {
        s"propertyIncomeReport.doNotReportPropertyIncome.$individualOrAgent"
      }
      SummaryListRowViewModel(
        key = KeyViewModel("propertyIncomeReport.checkYourAnswersLabel") withCssClass keyAlignLeftCssClass,
        value = ValueViewModel(value).withCssClass(selectCountriesValueAlignLeftCssClass),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.foreign.routes.PropertyIncomeReportController.onPageLoad(taxYear, CheckMode).url
          )
            .withVisuallyHiddenText(messages("propertyIncomeReport.change.hidden")).withCssClass(valueCssClass)
        )
      )
    }
}
