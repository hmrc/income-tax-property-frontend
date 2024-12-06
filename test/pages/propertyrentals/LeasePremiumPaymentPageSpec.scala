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

package pages.propertyrentals

import base.SpecBase
import models.{CalculatedFigureYourself, PremiumsGrantLease, PropertyType, Rentals, RentalsRentARoom}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import pages.premiumlease._

class PremiumForLeasePageSpec extends SpecBase {
  val scenarios = Table[PropertyType, String](
    ("property type", "type definition"),
    (RentalsRentARoom, "rentalsAndRaR"),
    (Rentals, "rentals")
  )
  forAll(scenarios) { (propertyType: PropertyType, propertyTypeDefinition: String) =>
    s"must remove the correct data when the answer is no for $propertyTypeDefinition" in {

      val userData = emptyUserAnswers
        .set(ReceivedGrantLeaseAmountPage(propertyType), BigDecimal(10.11))
        .get
        .set(YearLeaseAmountPage(propertyType), 10)
        .get
        .set(
          PremiumsGrantLeasePage(propertyType),
          PremiumsGrantLease(premiumsGrantLeaseReceived = true, Some(BigDecimal(10.12)))
        )
        .get
        .set(CalculatedFigureYourselfPage(propertyType), CalculatedFigureYourself(false, None))
        .get

      val result = userData.set(PremiumForLeasePage(propertyType), false).success.value

      result.get(PremiumForLeasePage(propertyType)) must be(defined)
      result.get(CalculatedFigureYourselfPage(propertyType)) must not be defined
      result.get(ReceivedGrantLeaseAmountPage(propertyType)) must not be defined
      result.get(YearLeaseAmountPage(propertyType)) must not be defined
      result.get(PremiumsGrantLeasePage(propertyType)) must not be defined

    }

    s"must keep that data value when the answer is yes for $propertyTypeDefinition" in {

      val userData = emptyUserAnswers
        .set(ReceivedGrantLeaseAmountPage(propertyType), BigDecimal(10.11))
        .get
        .set(YearLeaseAmountPage(propertyType), 10)
        .get
        .set(
          PremiumsGrantLeasePage(propertyType),
          PremiumsGrantLease(premiumsGrantLeaseReceived = true, Some(BigDecimal(10.12)))
        )
        .get
        .set(CalculatedFigureYourselfPage(propertyType), CalculatedFigureYourself(false, None))
        .get

      val result = userData.set(PremiumForLeasePage(propertyType), true).success.value

      result.get(PremiumForLeasePage(propertyType)) must be(defined)
      result.get(CalculatedFigureYourselfPage(propertyType)) must be(defined)
      result.get(ReceivedGrantLeaseAmountPage(propertyType)) must be(defined)
      result.get(YearLeaseAmountPage(propertyType)) must be(defined)
      result.get(PremiumsGrantLeasePage(propertyType)) must be(defined)

    }
  }
}
