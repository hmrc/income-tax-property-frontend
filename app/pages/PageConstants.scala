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

import models.{PropertyType, RentARoom, RentalsAndRentARoom}

object PageConstants {

  val structureBuildingFormGroup: String = "structureBuildingFormGroup"
  val sbasWithSupportingQuestions = "sbasWithSupportingQuestions"

  val esbaFormGroup: String = "esbas"
  val esbasWithSupportingQuestions = "esbasWithSupportingQuestions"

  val propertyAbout: String = "propertyAbout"
  val propertyRentalsAbout: String = "propertyRentalsAbout"

  val propertyRentalsAllowance: String = "propertyRentalsAllowance"
  val propertyRentalsAdjustment: String = "propertyRentalsAdjustment"

  val propertyRentalsIncome: String = "propertyRentalsIncome"
  val propertyRentalsExpense: String = "propertyRentalsExpense"

  val rentARoomAbout: String = "rentARoomAbout"
  val rentalsAndRentARoomAbout: String = "rentalsAndRentARoomAbout"
  val rentARoomExpense: String = "rentARoomExpenses"
  val rentalsAndRentARoomExpense: String = "rentalsAndRentARoomExpenses"
  val rentARoomAdjustment: String = "rentARoomAdjustments"
  val rentalsAndRentARoomAdjustment: String = "rentalsAndRentARoomAdjustments"
  val rentARoomAllowance: String = "rentARoomAllowances"
  val rentalsAndRentARoomAllowance: String = "rentalsAndRentARoomAllowances"

  val propertyRentalSectionFinished: String = "propertyRentalSectionFinished"
  val rentARoomSectionFinished: String = "rentARoomSectionFinished"

  val aboutPath: PropertyType => String = {
    case RentARoom => PageConstants.rentARoomAbout
    case RentalsAndRentARoom => PageConstants.rentalsAndRentARoomAbout
  }

  val expensesPath: PropertyType => String = {
    case RentARoom => PageConstants.rentARoomExpense
    case RentalsAndRentARoom => PageConstants.rentalsAndRentARoomExpense
  }

  val adjustmentsPath: PropertyType => String = {
    case RentARoom => PageConstants.rentARoomAdjustment
    case RentalsAndRentARoom => PageConstants.rentalsAndRentARoomAdjustment
  }

  val allowancePath: PropertyType => String = {
    case RentARoom => PageConstants.rentARoomAllowance
    case RentalsAndRentARoom => PageConstants.rentalsAndRentARoomAllowance
  }
}
