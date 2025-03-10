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

package pages

import models.{RentalsRentARoom, ForeignProperty, UKAndForeignProperty, Rentals, PropertyType, RentARoom}

object PageConstants {

  val structureBuildingFormGroup: String = "structureBuildingFormGroup"
  val sbasWithSupportingQuestions = "sbasWithSupportingQuestions"

  val esbas: String = "enhancedStructureBuildingAllowances"
  val esbasWithSupportingQuestions = "esbasWithSupportingQuestions"

  val foreignSbaFormGroup = "foreignSbaFormGroup"

  val propertyAbout: String = "propertyAbout"

  val ukAndForeignAbout: String = "ukAndForeignAbout"

  val labelForPropertyType: (PropertyType, String) => String = (propertyType, suffix) =>
    propertyType match {
      case Rentals          => s"rentals$suffix"
      case RentARoom        => s"rentARoom$suffix"
      case RentalsRentARoom => s"rentalsRentARoom$suffix"
      case ForeignProperty  => s"foreignProperty$suffix"
      case UKAndForeignProperty => s"ukAndForeignProperty$suffix"
    }

  val countriesRentedPropertyGroup: String = "countriesRentedPropertyGroup"

  // About
  val aboutPath: PropertyType => String = labelForPropertyType(_, "About")
  // Allowance
  val allowancesPath: PropertyType => String = labelForPropertyType(_, "Allowances")
  // Adjustments
  val adjustmentsPath: PropertyType => String = labelForPropertyType(_, "Adjustments")
  // Expenses
  val expensesPath: PropertyType => String = labelForPropertyType(_, "Expenses")
  // Income
  val incomePath: PropertyType => String = labelForPropertyType(_, "Income")

  val sbaPath: PropertyType => String = labelForPropertyType(_, "SBA")

  val eSbaPath: PropertyType => String = labelForPropertyType(_, "ESBA")

  val propertyRentalSectionFinished: String = "propertyRentalSectionFinished"

  val rentARoomSectionFinished: String = "rentARoomSectionFinished"

  val selectCountryPath: PropertyType => String = labelForPropertyType(_, "SelectCountry")

  val foreignTaxPath: PropertyType => String = labelForPropertyType(_, "ForeignTax")

}
