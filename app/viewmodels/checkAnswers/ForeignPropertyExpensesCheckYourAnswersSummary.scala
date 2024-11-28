package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.ForeignPropertyExpensesCheckYourAnswersPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ForeignPropertyExpensesCheckYourAnswersSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ForeignPropertyExpensesCheckYourAnswersPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "foreignPropertyExpensesCheckYourAnswers.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", routes.ForeignPropertyExpensesCheckYourAnswersController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("foreignPropertyExpensesCheckYourAnswers.change.hidden"))
          )
        )
    }
}
