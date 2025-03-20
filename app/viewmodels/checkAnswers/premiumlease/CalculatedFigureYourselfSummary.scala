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

package viewmodels.checkAnswers.premiumlease

import controllers.premiumlease.routes
import models.{CalculatedFigureYourself, CheckMode, PropertyType, UserAnswers}
import pages.premiumlease.CalculatedFigureYourselfPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CalculatedFigureYourselfSummary {

  def row(taxYear: Int, answers: UserAnswers, propertyType: PropertyType, individualOrAgent: String)(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    answers.get(CalculatedFigureYourselfPage(propertyType)).flatMap {
      case CalculatedFigureYourself(true, Some(amount)) =>
        Some(
          SummaryListRowViewModel(
            key = KeyViewModel("calculatedFigureYourself.checkYourAnswersAmountLabel").withCssClass(keyCssClass),
            value = ValueViewModel(bigDecimalCurrency(amount)).withCssClass(valueCssClass),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                routes.CalculatedFigureYourselfController.onPageLoad(taxYear, CheckMode, propertyType).url
              )
                .withVisuallyHiddenText(messages(s"calculatedFigureYourself.change.hidden.$individualOrAgent"))
            )
          )
        )
      case CalculatedFigureYourself(false, _) =>
        Some(
          SummaryListRowViewModel(
            key = KeyViewModel(s"calculatedFigureYourself.checkYourAnswersQuestionLabel.$individualOrAgent").withCssClass(keyCssClass),
            value = ValueViewModel("site.no").withCssClass(valueCssClass),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                routes.CalculatedFigureYourselfController.onPageLoad(taxYear, CheckMode, propertyType).url
              )
                .withVisuallyHiddenText(messages(s"calculatedFigureYourself.change.hidden.$individualOrAgent"))
            )
          )
        )
      case _ => Option.empty[SummaryListRow]
    }
}
