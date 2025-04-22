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

package viewmodels.checkAnswers.ukrentaroom

import controllers.ukrentaroom.routes.ReliefAmountController
import models.{CheckMode, PropertyType, UserAnswers}
import pages.ukrentaroom.ReliefAmountPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ReliefAmountSummary {
  def row(taxYear: Int, userAnswers: UserAnswers, propertyType: PropertyType)(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    userAnswers.get(ReliefAmountPage(propertyType)).map { amount =>
      SummaryListRowViewModel(
        key = KeyViewModel(s"ukrentaroom.reliefAmount.checkYourAnswersLabel")
          .withCssClass(keyCssClass),
        value = ValueViewModel(bigDecimalCurrency(amount)).withCssClass(valueCssClass),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            ReliefAmountController.onPageLoad(taxYear, CheckMode, propertyType).url
          )
            .withVisuallyHiddenText(messages("ukrentaroom.reliefAmount..change.hidden"))
        )
      )
    }

}
