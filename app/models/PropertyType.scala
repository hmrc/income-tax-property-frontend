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

package models

import play.api.mvc.JavascriptLiteral

sealed trait PropertyType

case object Rentals extends PropertyType
case object RentARoom extends PropertyType
case object RentalsAndRentARoom extends PropertyType

object PropertyType {

  implicit val jsLiteral: JavascriptLiteral[PropertyType] = new JavascriptLiteral[PropertyType] {
    override def to(value: PropertyType): String = value match {
      case Rentals             => "Rentals"
      case RentARoom           => "RentARoom"
      case RentalsAndRentARoom => "RentalsAndRentARoom"
    }
  }
}
