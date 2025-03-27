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

package audit

import models.TotalIncome.{Between, Over, Under}
import pages.foreign.Country
import play.api.libs.json.{Format, Json}

final case class ForeignPropertySelectCountry(
  isTotalIncomeUnder1k: Boolean,
  isTotalIncomeBetween1kAnd85k: Boolean,
  isTotalIncomeOver85k: Boolean,
  isReportPropertyIncome: Option[Boolean],
  incomeCountries: Option[Array[Country]],
  isAddAnotherCountry: Option[Boolean],
  isClaimPropertyIncomeAllowance: Option[Boolean]
)
object ForeignPropertySelectCountry {
  implicit val format: Format[ForeignPropertySelectCountry] = Json.format[ForeignPropertySelectCountry]

  def apply(foreignPropertySelectCountry: models.ForeignPropertySelectCountry): ForeignPropertySelectCountry = {
    val (under, between, over) = foreignPropertySelectCountry.totalIncome match {
      case Under =>   (true, false, false)
      case Between => (false, true, false)
      case Over =>    (false, false, true)
    }
    ForeignPropertySelectCountry(
      isTotalIncomeUnder1k = under,
      isTotalIncomeBetween1kAnd85k = between,
      isTotalIncomeOver85k = over,
      isReportPropertyIncome = foreignPropertySelectCountry.isReportPropertyIncome,
      incomeCountries = foreignPropertySelectCountry.incomeCountries,
      isAddAnotherCountry = foreignPropertySelectCountry.isAddAnotherCountry,
      isClaimPropertyIncomeAllowance = foreignPropertySelectCountry.isClaimPropertyIncomeAllowance
    )
  }
}
