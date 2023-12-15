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

package viewmodels.checkAnswers.allowances

import controllers.allowances.routes
import models.{CheckMode, UserAnswers}
import pages.allowances.ZeroEmissionGoodsVehicleAllowancePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ZeroEmissionGoodsVehicleAllowanceSummary {

  def row(taxYear: Int, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ZeroEmissionGoodsVehicleAllowancePage).map {
      answer =>

        SummaryListRowViewModel(
          key = KeyViewModel("zeroEmissionGoodsVehicleAllowance.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel(bigDecimalCurrency(answer)).withCssClass(valueCssClass),
          actions = Seq(
            ActionItemViewModel("site.change", routes.ZeroEmissionGoodsVehicleAllowanceController.onPageLoad(taxYear, CheckMode).url)
              .withVisuallyHiddenText(messages("zeroEmissionGoodsVehicleAllowance.change.hidden"))
          )
        )
    }
}

