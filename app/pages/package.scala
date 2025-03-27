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

import models.{ForeignProperty, PropertyType, Rentals, RentalsRentARoom, UKPropertySelect, UkAndForeignPropertyRentalTypeUk, UserAnswers}
import pages.foreign.ClaimPropertyIncomeAllowanceOrExpensesPage
import pages.propertyrentals.ClaimPropertyIncomeAllowancePage
import pages.ukandforeignproperty.{SectionCompletePage, UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage}

package object pages {
  def isSelected(userAnswers: Option[UserAnswers], select: UKPropertySelect): Boolean =
    userAnswers.exists(_.get(UKPropertyPage).exists(_.contains(select)))

  def isSelected(userAnswers: Option[UserAnswers], select: UkAndForeignPropertyRentalTypeUk): Boolean =
    userAnswers.exists(_.get(UkAndForeignPropertyRentalTypeUkPage).exists(_.contains(select)))

  def isUkAndForeignAboutJourneyComplete(userAnswers: UserAnswers): Boolean =
    userAnswers.get(SectionCompletePage).isDefined

  def getIsClaimPIA(userAnswers: Option[UserAnswers], propertyType: PropertyType): Option[Boolean] =
    userAnswers.map { ua =>
      if (isUkAndForeignAboutJourneyComplete(ua)) {
        ua
          .get(UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage)
          .exists(_.isClaimPropertyIncomeAllowanceOrExpenses)
      } else
        {
          propertyType match {
            case ForeignProperty  => ua.get(ClaimPropertyIncomeAllowanceOrExpensesPage)
            case Rentals          => ua.get(ClaimPropertyIncomeAllowancePage(Rentals))
            case RentalsRentARoom => ua.get(ClaimPropertyIncomeAllowancePage(RentalsRentARoom))
          }
        }.getOrElse(false)
    }

}
