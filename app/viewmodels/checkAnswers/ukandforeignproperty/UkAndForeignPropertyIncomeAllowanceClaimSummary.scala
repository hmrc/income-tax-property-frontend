package viewmodels.checkAnswers.ukandforeignproperty

import controllers.ukandforeignproperty.routes.PropertyIncomeAllowanceClaimController
import models.{CheckMode, UserAnswers}
import pages.ukandforeignproperty.UkAndForeignPropertyIncomeAllowanceClaimPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, keyCssClass, valueCssClass}
import viewmodels.govuk.all.{ActionItemViewModel, FluentActionItem, FluentKey, FluentValue, KeyViewModel, SummaryListRowViewModel, ValueViewModel}
import viewmodels.implicits._

object UkAndForeignPropertyIncomeAllowanceClaimSummary {
  def row(taxYear: Int, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(UkAndForeignPropertyIncomeAllowanceClaimPage).map {
      answer =>
        SummaryListRowViewModel(
          key     = KeyViewModel("uKRentalPropertyIncome.checkYourAnswersLabel").withCssClass(keyCssClass),
          value   = ValueViewModel(bigDecimalCurrency(answer)).withCssClass(valueCssClass),
          actions = Seq(
            ActionItemViewModel("site.change", PropertyIncomeAllowanceClaimController.onPageLoad(taxYear, CheckMode).url)
              .withVisuallyHiddenText(messages("uKRentalPropertyIncome.change.hidden"))
          )
        )
    }
}
