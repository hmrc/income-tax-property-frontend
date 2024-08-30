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

package viewmodels.checkAnswers.structurebuildingallowance

import controllers.structuresbuildingallowance.routes
import models.{PropertyType, SbaOnIndex, UserAnswers}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object StructureBuildingAllowanceSummary {

  def row(taxYear: Int, index: Int, answers: UserAnswers, propertyType: PropertyType)(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    answers.get(SbaOnIndex(index, propertyType)).map { answer =>
      val value = s"""${HtmlFormat.escape(answer.structuredBuildingAllowanceAddress.buildingName)},
        ${HtmlFormat.escape(answer.structuredBuildingAllowanceAddress.buildingNumber)} <br>
        ${HtmlFormat.escape(answer.structuredBuildingAllowanceAddress.postCode)}"""

      SummaryListRowViewModel(
        key = KeyViewModel(HtmlContent(value)).withCssClass(keyCssClass),
        value = ValueViewModel(
          bigDecimalCurrency(answer.structureBuildingAllowanceClaim)
        ).withCssClass(valueCssClass),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.SbaCheckYourAnswersController.onPageLoad(taxYear, index, propertyType).url
          ).withVisuallyHiddenText(messages("structureBuildingAllowanceClaim.change.hidden")),
          ActionItemViewModel(
            "site.remove",
            routes.SbaRemoveConfirmationController.onPageLoad(taxYear, index, propertyType).url
          )
            .withVisuallyHiddenText(messages("structureBuildingAllowanceClaim.change.hidden"))
        ),
        actionsCss = "w-25"
      )

    }
}
