/*
 * Copyright 2024 HM Revenue & Customs
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

package viewmodels.checkAnswers.foreign

import models.{UserAnswers, CheckMode, CrpOnIndex}
import pages.foreign.SelectIncomeCountryPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import viewmodels.checkAnswers.FormatUtils.keyCssClass

object CountriesRentedPropertySummary  {

  def row(taxYear: Int, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(SelectIncomeCountryPage).map {
      answer =>
        val value = s"$answer"
        SummaryListRowViewModel(
          key     = KeyViewModel(HtmlContent(value)).withCssClass(keyCssClass),
          value   = ValueViewModel("PLACEHOLDER FOR CATEGORY"),
          //ACTIONS NEED SETTING ONCE PAGES HAVE BEEN IMPLEMENTED
          actions = Seq(
            ActionItemViewModel("site.change", controllers.foreign.routes.CountriesRentedPropertyController.onPageLoad(taxYear, CheckMode).url)
              .withVisuallyHiddenText(messages("countriesRentedProperty.change.hidden")),
            ActionItemViewModel("site.remove", controllers.foreign.routes.CountriesRentedPropertyController.onPageLoad(taxYear, CheckMode).url)
              .withVisuallyHiddenText(messages("countriesRentedProperty.change.hidden")),
          )
        )
    }

  //TO BE USED ONCE PAGE TO ADD COUNTRIES IS IMPLEMENTED
//  def row(taxYear: Int, index: Int, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
//    answers.get(CrpOnIndex(index)).map {
//      answer =>
//
//        val value = s"$answer"
//
//        SummaryListRowViewModel(
//          key     = KeyViewModel(HtmlContent(value)).withCssClass(keyCssClass),
//          value   = ValueViewModel(value),
//          actions = Seq(
//            ActionItemViewModel("site.change", controllers.foreign.routes.CountriesRentedPropertyController.onPageLoad(taxYear, CheckMode).url)
//              .withVisuallyHiddenText(messages("countriesRentedProperty.change.hidden"))
//          )
//        )
//    }
}
