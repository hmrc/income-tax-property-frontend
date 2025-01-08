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
import pages.foreign.structurebuildingallowance.ForeignStructureBuildingQualifyingDatePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.keyCssClass
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import java.time.format.DateTimeFormatter

object ForeignStructureBuildingQualifyingDateSummary {

  def row(taxYear: Int, countryCode: String, index: Int, answers: UserAnswers)(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    answers.get(ForeignStructureBuildingQualifyingDatePage(countryCode, index)).map { answer =>
      val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

      SummaryListRowViewModel(
        key = KeyViewModel("foreignStructureBuildingQualifyingDate.checkYourAnswersLabel").withCssClass(keyCssClass),
        value = ValueViewModel(answer.format(dateFormatter)),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.ForeignStructureBuildingQualifyingDateController
              .onPageLoad(taxYear, countryCode, index, CheckMode)
              .url
          )
            .withVisuallyHiddenText(messages("foreignStructureBuildingQualifyingDate.change.hidden"))
        )
      )
    }
}
