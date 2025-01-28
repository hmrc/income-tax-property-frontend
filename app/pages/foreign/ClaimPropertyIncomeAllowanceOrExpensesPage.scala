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

package pages.foreign

import models.{ForeignProperty, ForeignPropertySelectCountry, UserAnswers}
import pages.PageConstants.selectCountryPath
import pages.QuestionPage
import pages.foreign.adjustments._
import play.api.libs.json.JsPath

import scala.util.Try

case object ClaimPropertyIncomeAllowanceOrExpensesPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ selectCountryPath(ForeignProperty) \ toString

  override def toString: String = "claimPropertyIncomeAllowance"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    val countryCodes: Array[String] =
      userAnswers
        .get(ForeignPropertySelectCountry)
        .flatMap(_.incomeCountries.map(_.map(_.code)))
        .getOrElse(Array.empty)

    countryCodes.foldLeft(Try(userAnswers)) { (acc, countryCode) =>
      acc.flatMap { ua =>
        ua.remove(ForeignPrivateUseAdjustmentPage(countryCode))
          .flatMap(_.remove(ForeignAdjustmentsSectionAddCountryCode(countryCode)))
          .flatMap(_.remove(ForeignBalancingChargePage(countryCode)))
          .flatMap(_.remove(PropertyIncomeAllowanceClaimPage(countryCode)))
          .flatMap(_.remove(ForeignResidentialFinanceCostsPage(countryCode)))
          .flatMap(_.remove(ForeignUnusedResidentialFinanceCostPage(countryCode)))
          .flatMap(_.remove(ForeignUnusedLossesPreviousYearsPage(countryCode)))
          .flatMap(_.remove(ForeignWhenYouReportedTheLossPage(countryCode)))
          .flatMap(_.remove(ForeignAdjustmentsCompletePage(countryCode)))
      }
    }
  }
}
