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

import models.{PropertyType, RentARoom, Rentals, RentalsAndRentARoom}

object PageConstants {

  val structureBuildingFormGroup: String = "structureBuildingFormGroup"
  val sbasWithSupportingQuestions = "sbasWithSupportingQuestions"

  val esbaFormGroup: String = "esbas"
  val esbasWithSupportingQuestions = "esbasWithSupportingQuestions"

  // About
  val propertyAbout: String = "propertyAbout"
  val rentalsAbout: String = "rentalsAbout"
  val rentARoomAbout: String = "rentARoomAbout"
  val rentalsAndRentARoomAbout: String = "rentalsAndRentARoomAbout"

  val aboutPath: PropertyType => String = {
    case Rentals             => rentalsAbout
    case RentARoom           => rentARoomAbout
    case RentalsAndRentARoom => rentalsAndRentARoomAbout
  }

  // Allowance
  val rentalsAllowances: String = "rentalsAllowances"
  val rentARoomAllowances: String = "rentARoomAllowances"
  val rentalsAndRentARoomAllowances: String = "rentalsAndRentARoomAllowances"

  val allowancesPath: PropertyType => String = {
    case Rentals             => rentalsAllowances
    case RentARoom           => rentARoomAllowances
    case RentalsAndRentARoom => rentalsAndRentARoomAllowances
  }

  // Adjustments
  val rentalsAdjustments: String = "rentalsAdjustments"
  val rentARoomAdjustments: String = "rentARoomAdjustments"
  val rentalsAndRentARoomAdjustments: String = "rentalsAndRentARoomAdjustments"

  val adjustmentsPath: PropertyType => String = {
    case Rentals             => rentalsAdjustments
    case RentARoom           => rentARoomAdjustments
    case RentalsAndRentARoom => rentalsAndRentARoomAdjustments
  }

  // Expenses
  val rentalsExpenses: String = "rentalsExpenses"
  val rentARoomExpenses: String = "rentARoomExpenses"
  val rentalsAndRentARoomExpenses: String = "rentalsAndRentARoomExpenses"

  val expensesPath: PropertyType => String = {
    case Rentals             => rentalsExpenses
    case RentARoom           => rentARoomExpenses
    case RentalsAndRentARoom => rentalsAndRentARoomExpenses
  }

  val propertyRentalsIncome: String = "propertyRentalsIncome"

  val propertyRentalSectionFinished: String = "propertyRentalSectionFinished"
  val rentARoomSectionFinished: String = "rentARoomSectionFinished"

}
