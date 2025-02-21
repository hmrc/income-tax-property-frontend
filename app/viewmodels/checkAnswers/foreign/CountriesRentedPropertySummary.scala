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

import models.{CheckMode, UserAnswers}
import pages.foreign.{Country, IncomeSourceCountries, SelectIncomeCountryPage}
import play.api.i18n.Messages
import service.CountryNamesDataSource
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{keyCssClass, selectCountriesValueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CountriesRentedPropertySummary {

  def row(taxYear: Int, index: Int, answers: UserAnswers, currentLang: String)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(SelectIncomeCountryPage(index)).map { country =>
      val countryChosen = CountryNamesDataSource.getCountry(country.code, currentLang).getOrElse(Country("", ""))
      val value = s"${countryChosen.name}"
      SummaryListRowViewModel(
        key = KeyViewModel(HtmlContent(value)),
        value = ValueViewModel(HtmlContent(messages("countriesRentedProperty.staticContent"))),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.foreign.routes.SelectIncomeCountryController.onPageLoad(taxYear, index, CheckMode).url
          )
            .withVisuallyHiddenText(messages("countriesRentedProperty.change.hidden")),
          ActionItemViewModel(
            "site.remove",
            controllers.foreign.routes.DoYouWantToRemoveCountryController.onPageLoad(taxYear, index, CheckMode).url
          )
            .withVisuallyHiddenText(messages("countriesRentedProperty.change.hidden"))
        ),
        actionsCss = "w-25"
      )
    }

  def rowList(taxYear: Int, answers: UserAnswers, currentLang: String)(implicit messages: Messages): Option[SummaryListRow] = {


    answers.get(IncomeSourceCountries).map { country: Array[Country] =>
      val value: Seq[String] = country.toList.flatMap {
        country => CountryNamesDataSource.getCountry(country.code, currentLang).map(c => c.name).toSeq
      }
      val countryList: String = value.mkString("<br>")
      SummaryListRowViewModel(
        key = KeyViewModel("countriesRentedProperty.checkYourAnswersLabel").withCssClass(keyCssClass),
        value = ValueViewModel(HtmlContent(countryList)).withCssClass(selectCountriesValueCssClass),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.foreign.routes.CountriesRentedPropertyController.onPageLoad(taxYear, CheckMode).url
          )
            .withVisuallyHiddenText(messages("countriesRentedProperty.change.hidden"))
        ),
        actionsCss = "w-25"
      )
    }
  }

}
