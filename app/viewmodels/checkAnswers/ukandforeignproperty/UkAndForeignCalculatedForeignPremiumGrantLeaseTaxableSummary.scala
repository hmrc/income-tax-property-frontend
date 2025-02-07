/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels.checkAnswers.ukandforeignproperty

import models.{CheckMode, PremiumCalculated, UserAnswers}
import pages.ukandforeignproperty.UkAndForeignCalculatedForeignPremiumGrantLeaseTaxablePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object UkAndForeignCalculatedForeignPremiumGrantLeaseTaxableSummary  {

  def rows(taxYear: Int, answers: UserAnswers)(implicit messages: Messages): Option[Seq[SummaryListRow]] =
  answers.get(UkAndForeignCalculatedForeignPremiumGrantLeaseTaxablePage).flatMap {
    case PremiumCalculated(true, amount) =>
      Some(
        Seq(
          SummaryListRowViewModel(
            key = KeyViewModel("ukAndForeignCalculatedForeignPremiumGrantLeaseTaxable.checkYourAnswersLabel").withCssClass(keyCssClass),
            value = ValueViewModel("site.yes").withCssClass(valueCssClass),
            actions = Seq(
              ActionItemViewModel("site.change",
                controllers.ukandforeignproperty.routes.UkAndForeignCalculatedForeignPremiumGrantLeaseTaxableController.onPageLoad(taxYear, CheckMode).url)
                .withVisuallyHiddenText(messages("ukAndForeignCalculatedForeignPremiumGrantLeaseTaxable.change.hidden"))
            )
          ),
          SummaryListRowViewModel(
            key = KeyViewModel("ukAndForeignProperty.foreignPremiumsGrantLease.checkYourAnswersLabel").withCssClass(keyCssClass),
            value = ValueViewModel(bigDecimalCurrency(amount.get)).withCssClass(valueCssClass),
            actions = Seq(
              ActionItemViewModel("site.change",
                controllers.ukandforeignproperty.routes.UkAndForeignCalculatedForeignPremiumGrantLeaseTaxableController.onPageLoad(taxYear, CheckMode).url)
                .withVisuallyHiddenText(messages("ukAndForeignCalculatedForeignPremiumGrantLeaseTaxable.change.hidden"))
            )
          )
        )
      )
    case PremiumCalculated(false, _) =>
      Some(Seq(SummaryListRowViewModel(
        key = KeyViewModel("ukAndForeignCalculatedForeignPremiumGrantLeaseTaxable.checkYourAnswersLabel").withCssClass(keyCssClass),
        value = ValueViewModel("site.no").withCssClass(valueCssClass),
        actions = Seq(
          ActionItemViewModel("site.change",
            controllers.ukandforeignproperty.routes.UkAndForeignCalculatedForeignPremiumGrantLeaseTaxableController.onPageLoad(taxYear, CheckMode).url)
            .withVisuallyHiddenText(messages("ukAndForeignCalculatedForeignPremiumGrantLeaseTaxable.change.hidden"))
        )
      )))
    case _ => Option.empty[Seq[SummaryListRow]]
  }
}
