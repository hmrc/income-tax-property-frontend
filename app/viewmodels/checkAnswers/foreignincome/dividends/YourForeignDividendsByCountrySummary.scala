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

package viewmodels.checkAnswers.foreignincome.dividends

import models.{CheckMode, UserAnswers, YourForeignDividendsByCountryRow}
import pages.foreign.Country
import pages.foreignincome.{CountryReceiveDividendIncomePage, DividendIncomeSourceCountries, IncomeBeforeForeignTaxDeductedPage}
import play.api.i18n.Messages
import service.CountryNamesDataSource
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{keyCssClass, selectCountriesValueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object YourForeignDividendsByCountrySummary {
  def row(index: Int, answers: UserAnswers, currentLang: String): Option[YourForeignDividendsByCountryRow] =
    answers.get(CountryReceiveDividendIncomePage(index)).flatMap { country =>
      (CountryNamesDataSource.getCountry(country.code, currentLang), answers.get(IncomeBeforeForeignTaxDeductedPage(country.code))) match {
        case (Some(country), Some(income)) =>
          Some(YourForeignDividendsByCountryRow(
            country, income
          ))
        case _ => None
      }
    }

  def rowList(taxYear: Int, answers: UserAnswers, currentLang: String)(implicit messages: Messages): Option[SummaryListRow] = {

    answers.get(DividendIncomeSourceCountries).map { country: Array[Country] =>
      val value: Seq[String] = country.toList.flatMap {
        country => CountryNamesDataSource.getCountry(country.code, currentLang).map(c => c.name).toSeq
      }
      val countryList: String = value.mkString("<br>")
      SummaryListRowViewModel(
        key = KeyViewModel("yourForeignDividendsByCountry.checkYourAnswersLabel").withCssClass(keyCssClass),
        value = ValueViewModel(HtmlContent(countryList)).withCssClass(selectCountriesValueCssClass),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.foreign.routes.CountriesRentedPropertyController.onPageLoad(taxYear, CheckMode).url
          )
            .withVisuallyHiddenText(messages("yourForeignDividendsByCountry.change.hidden"))
        ),
        actionsCss = "w-25"
      )
    }
  }
}