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

package viewmodels.checkAnswers.foreign.structurebuildingallowance

import controllers.foreign.structuresbuildingallowance.routes
import models.{ForeignSbaOnIndex, UserAnswers}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ForeignStructureBuildingAllowanceClaimsSummary {

  def row(taxYear: Int, index: Int, answers: UserAnswers, countryCode: String)(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    answers.get(ForeignSbaOnIndex(index, countryCode)).map { answer =>
      val value = s"""${HtmlFormat.escape(answer.foreignStructureBuildingAddress.name)},
        ${HtmlFormat.escape(answer.foreignStructureBuildingAddress.number)} <br>
        ${HtmlFormat.escape(answer.foreignStructureBuildingAddress.postCode)}"""

      SummaryListRowViewModel(
        key = KeyViewModel(HtmlContent(value)).withCssClass(keyCssClass),
        value = ValueViewModel(
          bigDecimalCurrency(answer.foreignStructureBuildingAllowanceClaim)
        ).withCssClass(valueCssClass),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.ForeignSbaCheckYourAnswersController
              .onPageLoad(taxYear, countryCode, index)
              .url
          ).withVisuallyHiddenText(messages("foreignStructureBuildingAllowanceClaims.change.hidden")),

          ActionItemViewModel(
            "site.remove",
            routes.ForeignSbaRemoveConfirmationController.onPageLoad(taxYear, index, countryCode).url
          ).withVisuallyHiddenText(messages("foreignStructureBuildingAllowanceClaims.change.hidden"))
        ),
        actionsCss = "w-25"
      )
    }.orElse(Option.empty[SummaryListRow])
}
