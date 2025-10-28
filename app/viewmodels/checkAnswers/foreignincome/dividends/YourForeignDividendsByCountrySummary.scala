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

import models.{ForeignDividendByCountryTableRow, UserAnswers}
import pages.foreignincome.{DividendIncomeSourceCountries, IncomeBeforeForeignTaxDeductedPage}
import service.CountryNamesDataSource

object YourForeignDividendsByCountrySummary {
  def tableRows(taxYear: Int, answers: UserAnswers, currentLang: String): Seq[ForeignDividendByCountryTableRow] =
    answers
      .get(DividendIncomeSourceCountries)
      .map { countries =>
        countries.toSeq.flatMap { country =>
          (
            CountryNamesDataSource.getCountry(country.code, currentLang),
            answers.get(IncomeBeforeForeignTaxDeductedPage(country.code))
          ) match {
            case (Some(country), Some(income)) =>
              Seq(
                ForeignDividendByCountryTableRow(
                  country = country,
                  income = income,
                  changeLink = controllers.foreignincome.dividends.routes.DividendsSectionCheckYourAnswersController.onPageLoad(taxYear, country.code).url,
                  removeLink = controllers.foreignincome.dividends.routes.RemoveForeignDividendController
                    .onPageLoad(taxYear, countries.indexOf(country))
                    .url
                ))
            case _ => Seq.empty
          }
        }
      }
      .getOrElse(Seq.empty)
}
