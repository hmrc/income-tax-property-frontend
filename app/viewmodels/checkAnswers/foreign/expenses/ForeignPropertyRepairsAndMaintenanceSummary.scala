package viewmodels.checkAnswers.foreign.expenses

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.foreign.expenses.ForeignPropertyRepairsAndMaintenancePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ForeignPropertyRepairsAndMaintenanceSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ForeignPropertyRepairsAndMaintenancePage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "foreignPropertyRepairsAndMaintenance.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.ForeignPropertyRepairsAndMaintenanceController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("foreignPropertyRepairsAndMaintenance.change.hidden"))
          )
        )
    }
}
