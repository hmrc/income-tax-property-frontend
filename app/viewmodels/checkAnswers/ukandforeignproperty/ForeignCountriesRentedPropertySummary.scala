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

import controllers.ukandforeignproperty.routes
import models.{CheckMode, Index, UserAnswers}
import pages.foreign.Country
import pages.ukandforeignproperty.SelectCountryPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{keyCssClass, valueCssClass}
import viewmodels.govuk.all.{ActionItemViewModel, FluentActionItem, FluentKey, FluentValue, KeyViewModel, SummaryListRowViewModel, ValueViewModel}
import viewmodels.implicits._

object ForeignCountriesRentedPropertySummary {

  def rowList(taxYear: Int, answers: UserAnswers, individualOrAgent: String)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(SelectCountryPage).map { country: List[Country] =>
      val value: Seq[String] = country.map { c: Country =>
        s"${c.name}"
      }
      val countryList: String = value.mkString("<br>")
      SummaryListRowViewModel(
        key = KeyViewModel(s"foreignCountriesRentList.checkYourAnswersLabel.$individualOrAgent").withCssClass(keyCssClass),
        value = ValueViewModel(HtmlContent(countryList)).withCssClass(valueCssClass),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.ForeignCountriesRentedController.onPageLoad(taxYear, CheckMode).url
          )
            .withVisuallyHiddenText(messages("countriesRentedProperty.change.hidden"))
        ),
        actionsCss = "w-25"
      )
    }
}
