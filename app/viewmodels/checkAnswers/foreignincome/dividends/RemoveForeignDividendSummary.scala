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

import models.{UserAnswers, YourForeignDividendsByCountryRow}
import pages.foreignincome.{CountryReceiveDividendIncomePage, IncomeBeforeForeignTaxDeductedPage}
import service.CountryNamesDataSource

object RemoveForeignDividendSummary {
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
}
