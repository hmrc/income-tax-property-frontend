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

package pages

import models.{RentARoom, Rentals, RentalsRentARoom, ForeignProperty}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PageConstantsSpec extends AnyWordSpec with Matchers {

  ".labelForPropertyType" should {
    "should the correct label for the corresponding property type" in {
      PageConstants.labelForPropertyType(Rentals, "Foo") shouldBe "rentalsFoo"
      PageConstants.labelForPropertyType(RentARoom, "Foo") shouldBe "rentARoomFoo"
      PageConstants.labelForPropertyType(RentalsRentARoom, "Foo") shouldBe "rentalsRentARoomFoo"
      PageConstants.labelForPropertyType(ForeignProperty, "Foo") shouldBe "foreignPropertyFoo"

      PageConstants.expensesPath(Rentals) shouldBe "rentalsExpenses"
      PageConstants.expensesPath(RentARoom) shouldBe "rentARoomExpenses"

      PageConstants.aboutPath(Rentals) shouldBe "rentalsAbout"
      PageConstants.aboutPath(RentARoom) shouldBe "rentARoomAbout"

      PageConstants.incomePath(Rentals) shouldBe "rentalsIncome"
      PageConstants.incomePath(RentARoom) shouldBe "rentARoomIncome"

      PageConstants.adjustmentsPath(Rentals) shouldBe "rentalsAdjustments"
      PageConstants.adjustmentsPath(RentARoom) shouldBe "rentARoomAdjustments"

      PageConstants.allowancesPath(Rentals) shouldBe "rentalsAllowances"
      PageConstants.allowancesPath(RentARoom) shouldBe "rentARoomAllowances"
    }
  }
}
