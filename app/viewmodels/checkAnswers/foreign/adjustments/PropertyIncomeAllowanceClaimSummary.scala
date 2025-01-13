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

package viewmodels.checkAnswers.foreign.adjustments

import models.{UserAnswers, CheckMode}
import pages.foreign.adjustments.PropertyIncomeAllowanceClaimPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import controllers.foreign.adjustments.routes.PropertyIncomeAllowanceClaimController
import viewmodels.checkAnswers.FormatUtils.{keyCssClass, bigDecimalCurrency, valueCssClass}

object PropertyIncomeAllowanceClaimSummary  {

  def row(answers: UserAnswers, taxYear: Int, countryCode: String)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(PropertyIncomeAllowanceClaimPage(countryCode)).map {
      answer =>

        SummaryListRowViewModel(
          key     = KeyViewModel("propertyIncomeAllowanceClaim.checkYourAnswersLabel").withCssClass(keyCssClass),
          value   = ValueViewModel(bigDecimalCurrency(answer)).withCssClass(valueCssClass),
          actions = Seq(
            ActionItemViewModel("site.change", PropertyIncomeAllowanceClaimController.onPageLoad(taxYear, countryCode, CheckMode).url)
              .withVisuallyHiddenText(messages("propertyIncomeAllowanceClaim.change.hidden"))
          )
        )
    }
}
