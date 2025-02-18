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

package forms.mappings

import models.{UserAnswers, ForeignAddressable, Addressable}
import pages.foreign.IncomeSourceCountries
import play.api.data.validation.{Valid, Constraint, Invalid}
import service.CountryNamesDataSource.loadCountriesEn

import java.time.LocalDate

trait Constraints {

  protected def firstError[A](constraints: Constraint[A]*): Constraint[A] =
    Constraint {
      input =>
        constraints
          .map(_.apply(input))
          .find(_ != Valid)
          .getOrElse(Valid)
    }

  protected def minimumValue[A](minimum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev._

        if (input >= minimum) {
          Valid
        } else {
          Invalid(errorKey, minimum)
        }
    }

  protected def maximumValue[A](maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev._

        if (input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, maximum)
        }
    }

  protected def inRange[A](minimum: A, maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev._

        if (input >= minimum && input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, minimum, maximum)
        }
    }

  protected def regexp(regex: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.matches(regex) =>
        Valid
      case _ =>
        Invalid(errorKey, regex)
    }

  protected def maxLength(maximum: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.length <= maximum =>
        Valid
      case _ =>
        Invalid(errorKey, maximum)
    }

  protected def maxDate(maximum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isAfter(maximum) =>
        Invalid(errorKey, args: _*)
      case _ =>
        Valid
    }

  protected def minDate(minimum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isBefore(minimum) =>
        Invalid(errorKey, args: _*)
      case _ =>
        Valid
    }

  protected def nonEmptySet(errorKey: String): Constraint[Set[_]] =
    Constraint {
      case set if set.nonEmpty =>
        Valid
      case _ =>
        Invalid(errorKey)
    }

  protected def minimumValueWithCustomArgument[A](minimum: A, errorKey: String, arg: A)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev._

        if (input >= minimum) {
          Valid
        } else {
          Invalid(errorKey, minimum, arg)
        }
    }

  def checkIfAddressAlreadyEntered[T, U](allAddresses: List[U], errorKey: String)
                                        (implicit addressableChecked: Addressable[T], addressableInList: Addressable[U]): Constraint[T] =
    Constraint[T] {
      address: T => {
        if (allAddresses.exists(a => Addressable.checkAddresses[T, U](address, a))) {
          Invalid(errorKey)
        } else {
          Valid
        }
      }
    }

  def checkIfForeignAddressAlreadyEntered[T, U](allAddresses: List[U], errorKey: String)
                                        (implicit addressableChecked: ForeignAddressable[T], addressableInList: ForeignAddressable[U]): Constraint[T] =
    Constraint[T] {
      address: T => {
        if (allAddresses.exists(a => ForeignAddressable.checkForeignAddresses[T, U](address, a))) {
          Invalid(errorKey)
        } else {
          Valid
        }
      }
    }

   def validCountry(errorMsg: String): Constraint[String] =
    Constraint {
      case countryCode if loadCountriesEn.map(_.code).contains(countryCode) =>
        Valid
      case _ =>
        Invalid(errorMsg)
    }

  def countryAlreadySelected(errorMsg: String, userAnswers: UserAnswers): Constraint[String] =
    Constraint {
      case countryCode
        if !userAnswers.get(IncomeSourceCountries).toSeq.flatten.map(_.code).contains(countryCode) =>
        Valid
      case _ =>
        Invalid(errorMsg)
    }
}
