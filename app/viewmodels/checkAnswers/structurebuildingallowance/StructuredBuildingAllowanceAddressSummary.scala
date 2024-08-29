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
import pages.structurebuildingallowance.StructuredBuildingAllowanceAddressPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object StructuredBuildingAllowanceAddressSummary {

  def row(taxYear: Int, idx: Int, answers: UserAnswers, propertyType: PropertyType)(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    answers.get(StructuredBuildingAllowanceAddressPage(idx, propertyType)).map { answer =>
      val value = HtmlFormat.escape(answer.buildingName).toString + "<br/>" +
        HtmlFormat.escape(answer.buildingNumber).toString + "<br/>" + HtmlFormat.escape(answer.postCode).toString

      SummaryListRowViewModel(
        key = "structureBuildingAllowanceAddress.checkYourAnswersLabel",
        value = ValueViewModel(HtmlContent(value)),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.StructuredBuildingAllowanceAddressController.onPageLoad(taxYear, CheckMode, idx, propertyType).url
          )
            .withVisuallyHiddenText(messages("structureBuildingAllowanceAddress.change.hidden"))
        )
      )
    }

}
