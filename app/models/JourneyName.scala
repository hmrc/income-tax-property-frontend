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

sealed trait JourneyName

object JourneyName extends Enumerable.Implicits {

  case object UKProperty extends WithName("UKProperty") with JourneyName
  case object ForeignProperty extends WithName("ForeignProperty") with JourneyName
  case object Rentals extends WithName("Rentals") with JourneyName
  case object RentARoom extends WithName("RentARoom") with JourneyName
  case object RentalsRentARoom extends WithName("RentalsRentARoom") with JourneyName
  case object UkAndForeignProperty extends WithName("UkAndForeignProperty") with JourneyName

  val values: Seq[JourneyName] = Seq(
    Rentals,
    RentARoom,
    RentalsRentARoom,
    UKProperty,
    ForeignProperty,
    UkAndForeignProperty
  )
  implicit val enumerable: Enumerable[JourneyName] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
