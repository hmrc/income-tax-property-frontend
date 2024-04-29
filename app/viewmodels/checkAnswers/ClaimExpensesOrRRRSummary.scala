package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.ClaimExpensesOrRRRPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ClaimExpensesOrRRRSummary  {

  def row(taxYear: Int, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ClaimExpensesOrRRRPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "claimExpensesOrRRR.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.ClaimExpensesOrRRRController.onPageLoad(taxYear, CheckMode).url)
              .withVisuallyHiddenText(messages("claimExpensesOrRRR.change.hidden"))
          )
        )
    }
}
