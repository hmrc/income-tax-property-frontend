package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.OtherAllowablePropertyExpensesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object OtherAllowablePropertyExpensesSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(OtherAllowablePropertyExpensesPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "otherAllowablePropertyExpenses.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.OtherAllowablePropertyExpensesController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("otherAllowablePropertyExpenses.change.hidden"))
          )
        )
    }
}
