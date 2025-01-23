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

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.foreign.adjustments.ForeignWhenYouReportedTheLossPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.valueCssClass
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ForeignWhenYouReportedTheLossSummary  {

  def row(taxYear: Int, countryCode: String, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ForeignWhenYouReportedTheLossPage(countryCode)).map {
      answer =>

        val value = ValueViewModel(
          HtmlContent(
            HtmlFormat.escape(messages(s"foreignWhenYouReportedTheLoss.$answer"))
          )
        ).withCssClass(valueCssClass)

        SummaryListRowViewModel(
          key     = "foreignWhenYouReportedTheLoss.checkYourAnswersLabel",
          value   = value,
          actions = Seq(
            ActionItemViewModel("site.change", controllers.foreign.adjustments.routes.ForeignWhenYouReportedTheLossController.onPageLoad(taxYear, countryCode, CheckMode).url)
              .withVisuallyHiddenText(messages("foreignWhenYouReportedTheLoss.change.hidden"))
          )
        )
    }
}
