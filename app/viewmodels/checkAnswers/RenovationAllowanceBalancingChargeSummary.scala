package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.RenovationAllowanceBalancingChargePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object RenovationAllowanceBalancingChargeSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(RenovationAllowanceBalancingChargePage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "renovationAllowanceBalancingCharge.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", routes.RenovationAllowanceBalancingChargeController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("renovationAllowanceBalancingCharge.change.hidden"))
          )
        )
    }
}
