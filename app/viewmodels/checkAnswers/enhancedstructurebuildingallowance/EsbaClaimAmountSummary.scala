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

package viewmodels.checkAnswers.enhancedstructurebuildingallowance

import controllers.enhancedstructuresbuildingallowance.routes
import models.{CheckMode, PropertyType, UserAnswers}
import pages.enhancedstructuresbuildingallowance.EsbaClaimPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.bigDecimalCurrency
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object EsbaClaimAmountSummary {

  def row(taxYear: Int, index: Int, answers: UserAnswers, propertyType: PropertyType)(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    answers.get(EsbaClaimPage(index, propertyType)).map { answer =>
      SummaryListRowViewModel(
        key = "esbaClaimAmount.checkYourAnswersLabel",
        value = ValueViewModel(bigDecimalCurrency(answer)),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.EsbaClaimController.onPageLoad(taxYear, CheckMode, index, propertyType).url
          )
            .withVisuallyHiddenText(messages("esbaClaimAmount.change.hidden"))
        )
      )
    }

  def row(taxYear: Int, index: Int, claimValue: BigDecimal, propertyType: PropertyType)(implicit
    messages: Messages
  ): SummaryListRow = {

    val value = HtmlFormat.escape(bigDecimalCurrency(claimValue)).toString()
    SummaryListRowViewModel(
      key = KeyViewModel("esbaClaimAmount.checkYourAnswersLabel"),
      value = ValueViewModel(value),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.EsbaCheckYourAnswersController.onPageLoad(taxYear, index, propertyType).url
        )
          .withVisuallyHiddenText(messages("esbaClaimAmount.change.hidden")),
        ActionItemViewModel(
          "site.remove",
          routes.EsbaRemoveConfirmationController.onPageLoad(taxYear, index, propertyType).url
        )
          .withVisuallyHiddenText(messages("esbaClaimAmount.change.hidden"))
      ),
      actionsCss = "w-25"
    )
  }
}
