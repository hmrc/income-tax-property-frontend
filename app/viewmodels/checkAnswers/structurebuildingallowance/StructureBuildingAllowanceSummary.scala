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

import models.{SbaOnIndex, UserAnswers}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.bigDecimalCurrency
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object StructureBuildingAllowanceSummary {

  def row(taxYear: Int, index: Int, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    answers.get(SbaOnIndex(index)).map { answer =>
      val value = HtmlFormat.escape(answer.structuredBuildingAllowanceAddress.buildingName).toString + ", " +
        HtmlFormat.escape(answer.structuredBuildingAllowanceAddress.buildingNumber).toString + ", " + HtmlFormat
          .escape(answer.structuredBuildingAllowanceAddress.postCode)
          .toString

      SummaryListRowViewModel(
        key = value,
        value = ValueViewModel(
          bigDecimalCurrency(answer.structureBuildingAllowanceClaim)
        ),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.structuresbuildingallowance.routes.SbaCheckYourAnswersController
              .onPageLoad(taxYear, index)
              .url
          )
            .withVisuallyHiddenText(messages("structureBuildingAllowanceAddress.change.hidden"))
        )
      )
    }
  }
}
