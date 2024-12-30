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
import models.{CheckMode, UserAnswers}
import pages.foreign.structurebuildingallowance.ForeignStructureBuildingAllowanceClaimPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.bigDecimalCurrency
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ForeignStructureBuildingAllowanceClaimSummary {

  def row(taxYear: Int, countryCode: String, index: Int, answers: UserAnswers)(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    answers.get(ForeignStructureBuildingAllowanceClaimPage(countryCode, index)).map { answer =>
      SummaryListRowViewModel(
        key = "foreignStructureBuildingAllowanceClaim.checkYourAnswersLabel",
        value = ValueViewModel(bigDecimalCurrency(answer)),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.ForeignStructureBuildingAllowanceClaimController
              .onPageLoad(taxYear, countryCode, index, CheckMode)
              .url
          )
            .withVisuallyHiddenText(messages("foreignStructureBuildingAllowanceClaim.change.hidden"))
        )
      )
    }

}
