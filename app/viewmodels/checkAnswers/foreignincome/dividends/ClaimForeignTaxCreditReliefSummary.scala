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

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.foreignincome.dividends.ClaimForeignTaxCreditReliefPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ClaimForeignTaxCreditReliefSummary  {

  def row(taxYear: Int, countryCode: String, individualOrAgent: String, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ClaimForeignTaxCreditReliefPage(countryCode)).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = KeyViewModel(s"dividendsClaimForeignTaxCreditRelief.checkYourAnswersLabel.${individualOrAgent}").withCssClass(keyCssClass),
          value   = ValueViewModel(value).withCssClass(valueCssClass),
          actions = Seq(
            ActionItemViewModel("site.change", controllers.foreignincome.dividends.routes.ClaimForeignTaxCreditReliefController.onPageLoad(taxYear, countryCode, CheckMode).url)
              .withVisuallyHiddenText(messages("dividendsClaimForeignTaxCreditRelief.change.hidden"))
          )
        )
    }
}
