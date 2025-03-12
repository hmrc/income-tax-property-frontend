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

package pages

import models.TotalIncome.{Between, Over}
import models.{PropertyType, RentARoom, Rentals, RentalsRentARoom, TotalIncome, UKPropertySelect, UserAnswers}
import pages.propertyrentals.expenses.{ConsolidatedExpensesPage, ExpensesSectionFinishedPage}
import pages.rentalsandrentaroom.expenses.RentalsRaRExpensesCompletePage
import pages.ukrentaroom.expenses.{ConsolidatedExpensesRRPage, ExpensesRRSectionCompletePage}
import play.api.libs.json.JsPath

import scala.util.{Success, Try}

case object TotalIncomePage extends QuestionPage[TotalIncome] {

  override def path: JsPath = JsPath \ PageConstants.propertyAbout \ toString

  override def toString: String = "totalIncome"

  override def cleanup(totalIncome: Option[TotalIncome], userAnswers: UserAnswers): Try[UserAnswers] = {

    val updatedUserAnswers: Try[UserAnswers] = totalIncome
      .filter(income => income == Between || income == Over)
      .map(_ => userAnswers.remove(ReportPropertyIncomePage))
      .getOrElse(super.cleanup(totalIncome, userAnswers))

    val propertyType: Option[PropertyType] = userAnswers.get(UKPropertyPage).map(_.toSeq).collect {
      case Seq(UKPropertySelect.PropertyRentals) => Rentals
      case Seq(UKPropertySelect.RentARoom)       => RentARoom
      case seq if seq.nonEmpty                   => RentalsRentARoom
    }

    propertyType match {
      case Some(Rentals) => for {
        answers                             <- updatedUserAnswers
        withoutRentalsConsolidatedExpenses  <- if(totalIncome.contains(Over)) answers.remove(ConsolidatedExpensesPage(Rentals)) else Success(answers)
        withoutRentalsSectionComplete       <- withoutRentalsConsolidatedExpenses.remove(ExpensesSectionFinishedPage)
      } yield withoutRentalsSectionComplete
      case Some(RentARoom) => for {
        answers                       <- updatedUserAnswers
        withoutRRConsolidatedExpenses <- if(totalIncome.contains(Over)) answers.remove(ConsolidatedExpensesRRPage) else Success(answers)
        withoutRRSectionComplete      <- withoutRRConsolidatedExpenses.remove(ExpensesRRSectionCompletePage)
      } yield withoutRRSectionComplete
      case Some(RentalsRentARoom) => for {
        answers                                   <- updatedUserAnswers
        withoutCombinedConsolidatedExpenses       <- if(totalIncome.contains(Over)) answers.remove(ConsolidatedExpensesPage(RentalsRentARoom)) else Success(answers)
        withoutCombinedSectionComplete            <- withoutCombinedConsolidatedExpenses.remove(RentalsRaRExpensesCompletePage)
      } yield withoutCombinedSectionComplete
      case _ => super.cleanup(totalIncome, userAnswers)
    }
  }

}
