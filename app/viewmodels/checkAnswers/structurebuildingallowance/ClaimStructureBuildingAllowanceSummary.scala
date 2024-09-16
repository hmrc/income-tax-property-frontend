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
import pages.structurebuildingallowance.ClaimStructureBuildingAllowancePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ClaimStructureBuildingAllowanceSummary {

  def row(taxYear: Int, answers: UserAnswers, individualOrAgent: String, propertyType: PropertyType)(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    answers.get(ClaimStructureBuildingAllowancePage(propertyType)).map { answer =>
      val value = if (answer) "site.yes" else "site.no"

      SummaryListRowViewModel(
        key = KeyViewModel(s"claimStructureBuildingAllowance.legend.$individualOrAgent").withCssClass(keyCssClass),
        value = ValueViewModel(value).withCssClass(valueCssClass),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.ClaimStructureBuildingAllowanceController.onPageLoad(taxYear, CheckMode, propertyType).url
          )
            .withVisuallyHiddenText(messages("claimStructureBuildingAllowance.change.hidden"))
        )
      )
    }
}
