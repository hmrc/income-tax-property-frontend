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

package viewmodels.checkAnswers.structurebuildingallowance

import controllers.structuresbuildingallowance.routes
import models.{CheckMode, PropertyType, UserAnswers}
import pages.structurebuildingallowance.StructureBuildingQualifyingAmountPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.bigDecimalCurrency
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object StructureBuildingQualifyingAmountSummary {

  def row(taxYear: Int, idx: Int, answers: UserAnswers, propertyType: PropertyType)(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    answers.get(StructureBuildingQualifyingAmountPage(idx, propertyType)).map { answer =>
      SummaryListRowViewModel(
        key = "structureBuildingQualifyingAmount.checkYourAnswersLabel",
        value = ValueViewModel(bigDecimalCurrency(answer)),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.StructureBuildingQualifyingAmountController.onPageLoad(taxYear, CheckMode, idx, propertyType).url
          )
            .withVisuallyHiddenText(messages("structureBuildingQualifyingAmount.change.hidden"))
        )
      )
    }
}
