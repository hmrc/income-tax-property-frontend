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
import models.{CalculatedFigureYourself, PremiumsGrantLease, Rentals}
import pages.premiumlease.{CalculatedFigureYourselfPage, PremiumsGrantLeasePage, ReceivedGrantLeaseAmountPage, YearLeaseAmountPage}

class CalculatedFigureYoureselfPageSpec extends SpecBase {

  "must remove the correct data when the answer is yes" in {

    val userData = emptyUserAnswers
      .set(ReceivedGrantLeaseAmountPage(Rentals), BigDecimal(10.11))
      .success
      .value
      .set(YearLeaseAmountPage(Rentals), 10)
      .success
      .value
      .set(
        PremiumsGrantLeasePage(Rentals),
        PremiumsGrantLease(premiumsGrantLeaseReceived = true, Some(BigDecimal(10.12)))
      )
      .success
      .value

    val result =
      userData.set(CalculatedFigureYourselfPage(Rentals), CalculatedFigureYourself(true, Some(10.13))).success.value

    result.get(CalculatedFigureYourselfPage(Rentals)) must be(defined)
    result.get(ReceivedGrantLeaseAmountPage(Rentals)) must not be defined
    result.get(YearLeaseAmountPage(Rentals)) must not be defined
    result.get(PremiumsGrantLeasePage(Rentals)) must not be defined

  }

  "must keep that data value when the answer is yes" in {

    val userData = emptyUserAnswers
      .set(ReceivedGrantLeaseAmountPage(Rentals), BigDecimal(10.10))
      .get
      .set(YearLeaseAmountPage(Rentals), 10)
      .get
      .set(
        PremiumsGrantLeasePage(Rentals),
        PremiumsGrantLease(premiumsGrantLeaseReceived = true, Some(BigDecimal(10.12)))
      )
      .get

    val result =
      userData.set(CalculatedFigureYourselfPage(Rentals), CalculatedFigureYourself(false, None)).success.value

    result.get(CalculatedFigureYourselfPage(Rentals)) must be(defined)
    result.get(ReceivedGrantLeaseAmountPage(Rentals)) must be(defined)
    result.get(YearLeaseAmountPage(Rentals)) must be(defined)
    result.get(PremiumsGrantLeasePage(Rentals)) must be(defined)
  }

}
