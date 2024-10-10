/*
 * Copyright 2023 HM Revenue & Customs
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

package viewmodels

import models.{ClaimExpensesOrRelief, NormalMode, PropertyType, UKPropertySelect, UserAnswers}
import pages.isSelected
import pages.propertyrentals.ClaimPropertyIncomeAllowancePage
import pages.ukrentaroom.ClaimExpensesOrReliefPage

case class AllowancesStartPage(
  taxYear: Int,
  individualOrAgent: String,
  cashOrAccruals: Boolean,
  userAnswers: UserAnswers,
  propertyType: PropertyType
) {
  def cashOrAccrualsMessageKey: String = if (cashOrAccruals) "businessDetails.accruals" else "businessDetails.cash"

  def nextPageUrl: String = {

    val isRentARoomSelected = isSelected(Some(userAnswers), UKPropertySelect.RentARoom)
    val isPropertyRentalsSelected = isSelected(Some(userAnswers), UKPropertySelect.PropertyRentals)

    // If it's the combined journey
    if (isPropertyRentalsSelected && isRentARoomSelected) {
      val rentARoomRelief =
        userAnswers.get(ClaimExpensesOrReliefPage(propertyType)).getOrElse(ClaimExpensesOrRelief(false, None))
      val propertyIncomeAllowance =
        userAnswers.get(ClaimPropertyIncomeAllowancePage(propertyType)).getOrElse(false)
      if (cashOrAccruals) {
        (rentARoomRelief, propertyIncomeAllowance) match {
          case (ClaimExpensesOrRelief(false, None), true) =>
            controllers.allowances.routes.ZeroEmissionCarAllowanceController
              .onPageLoad(taxYear, NormalMode, propertyType)
              .url
          case (ClaimExpensesOrRelief(false, None), false) | (ClaimExpensesOrRelief(true, _), false) =>
            controllers.allowances.routes.AnnualInvestmentAllowanceController
              .onPageLoad(taxYear, NormalMode, propertyType)
              .url
        }
      } else {
        controllers.allowances.routes.CapitalAllowancesForACarController
          .onPageLoad(taxYear, NormalMode, propertyType)
          .url
      }
    }
    // If it's NOT the combined journey i.e. property rentals or rent a room
    else {
      if (cashOrAccruals) {
        controllers.allowances.routes.AnnualInvestmentAllowanceController
          .onPageLoad(taxYear, NormalMode, propertyType)
          .url
      } else {
        controllers.allowances.routes.CapitalAllowancesForACarController
          .onPageLoad(taxYear, NormalMode, propertyType)
          .url
      }
    }
  }
}
