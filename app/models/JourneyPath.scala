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

package models

sealed trait JourneyPath

object JourneyPath {

  case object PropertyAbout extends WithName("property-about") with JourneyPath

  case object PropertyRentalAdjustments extends WithName("property-rental-adjustments") with JourneyPath

  case object RentARoomAbout extends WithName("rent-a-room-about") with JourneyPath

  case object RentalAdjustments extends WithName("rental-adjustments") with JourneyPath

  case object PropertyRentalAllowances extends WithName("property-rental-allowances") with JourneyPath

  case object RentalAllowances extends WithName("rental-allowances") with JourneyPath

  case object ESBA extends WithName("esba") with JourneyPath

  case object RentalESBA extends WithName("rental-esba") with JourneyPath

  case object PropertyRentalsAndRentARoomESBA extends WithName("property-rentals-and-rent-a-room-esba") with JourneyPath

  case object PropertyRentalAbout extends WithName("property-rental-about") with JourneyPath

  case object RentalExpenses extends WithName("rental-expenses") with JourneyPath

  case object RentalIncome extends WithName("rental-income") with JourneyPath

  case object PropertyRentalExpenses extends WithName("property-rental-expenses") with JourneyPath

  case object RentalsAndRentARoomAbout extends WithName("rentals-and-rent-a-room-about") with JourneyPath

  case object PropertyRentalsAndRentARoomAbout
      extends WithName("property-rentals-and-rent-a-room-about") with JourneyPath

  case object RentalsAndRentARoomAdjustments extends WithName("rentals-and-rent-a-room-adjustments") with JourneyPath

  case object PropertyRentalsAndRentARoomAdjustments
      extends WithName("property-rentals-and-rent-a-room-adjustments") with JourneyPath

  case object RentalsAndRentARoomAllowances extends WithName("rentals-and-rent-a-room-allowances") with JourneyPath

  case object RentalsAndRentARoomExpenses extends WithName("rentals-and-rent-a-room-expenses") with JourneyPath

  case object PropertyRentalsAndRentARoomAllowances
      extends WithName("property-rentals-and-rent-a-room-allowances") with JourneyPath

  case object PropertyRentalsAndRentARoomExpenses
      extends WithName("property-rentals-and-rent-a-room-expenses") with JourneyPath

  case object PropertyRentalsAndRentARoomIncome
      extends WithName("property-rentals-and-rent-a-room-income") with JourneyPath

  case object RentalsAndRentARoomIncome extends WithName("rentals-and-rent-a-room-income") with JourneyPath

  case object PropertyRentalSBA extends WithName("property-rental-sba") with JourneyPath

  case object RentalsAndRentARoomSBA extends WithName("rentals-and-rent-a-room-sba") with JourneyPath

  case object RentalSBA extends WithName("rental-sba") with JourneyPath

  case object PropertyRentalsAndRentARoomSBA extends WithName("property-rentals-and-rent-a-room-sba") with JourneyPath

  case object RentARoomAdjustments extends WithName("rent-a-room-adjustments") with JourneyPath

  case object RentARoomAllowances extends WithName("rent-a-room-allowances") with JourneyPath

  case object RentARoomExpenses extends WithName("rent-a-room-expenses") with JourneyPath

  case object RentalsAndRentARoomESBA extends WithName("rentals-and-rent-a-room-esba") with JourneyPath

  // ####################### Foreign Property #######################
  case object ForeignSelectCountry extends WithName("foreign-property-select-country") with JourneyPath

  case object ForeignPropertyTax extends WithName("foreign-property-tax") with JourneyPath

  case object ForeignPropertyIncome extends WithName("foreign-property-income") with JourneyPath

  case object ForeignPropertyExpenses extends WithName("foreign-property-expenses") with JourneyPath

  case object ForeignPropertyAllowances extends WithName("foreign-property-allowances") with JourneyPath

  case object ForeignStructureBuildingAllowance extends WithName("foreign-property-sba") with JourneyPath

  case object ForeignPropertyAdjustments extends WithName("foreign-property-adjustments") with JourneyPath

  // ####################### Foreign Property #######################
  case object UkAndForeignPropertyAbout extends WithName("uk-foreign-property-about") with JourneyPath

}
