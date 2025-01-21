package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.ForeignRentalPropertyIncomePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ForeignRentalPropertyIncomeSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ForeignRentalPropertyIncomePage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "foreignRentalPropertyIncome.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.ForeignRentalPropertyIncomeController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("foreignRentalPropertyIncome.change.hidden"))
          )
        )
    }
}
