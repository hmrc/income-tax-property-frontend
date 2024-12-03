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

package viewmodels.checkAnswers.foreign.expenses

import controllers.foreign.expenses.routes
import models.{UserAnswers, CheckMode}
import pages.foreign.expenses.ForeignRentsRatesAndInsurancePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{keyCssClass, valueCssClass, bigDecimalCurrency}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ForeignRentsRatesAndInsuranceSummary {

  def row(taxYear: Int, countryCode: String, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ForeignRentsRatesAndInsurancePage(countryCode)).map {
      answer =>

        SummaryListRowViewModel(
          key = KeyViewModel("foreignRentsRatesAndInsurance.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel(bigDecimalCurrency(answer)).withCssClass(valueCssClass),
          actions = Seq(
            ActionItemViewModel("site.change", routes.ForeignRentsRatesAndInsuranceController.onPageLoad(taxYear, countryCode, CheckMode).url)
              .withVisuallyHiddenText(messages("foreignRentsRatesAndInsurance.change.hidden"))
          )
        )
    }
}
