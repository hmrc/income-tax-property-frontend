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
import models.{CalculatedFigureYourself, PremiumsGrantLease}
import pages.premiumlease.{CalculatedFigureYourselfPage, LeasePremiumPaymentPage, PremiumsGrantLeasePage, ReceivedGrantLeaseAmountPage, YearLeaseAmountPage}

class LeasePremiumPaymentPageSpec extends SpecBase {

  "must remove the correct data when the answer is no" in {

    val userData = emptyUserAnswers
      .set(ReceivedGrantLeaseAmountPage, BigDecimal(10.11)).get
      .set(YearLeaseAmountPage, 10).get
      .set(PremiumsGrantLeasePage, PremiumsGrantLease(yesOrNo = true, Some(BigDecimal(10.12)))).get
      .set(CalculatedFigureYourselfPage, CalculatedFigureYourself(false, None)).get

    val result = userData.set(LeasePremiumPaymentPage, false).success.value

    result.get(LeasePremiumPaymentPage) must be(defined)
    result.get(CalculatedFigureYourselfPage) must not be defined
    result.get(ReceivedGrantLeaseAmountPage) must not be defined
    result.get(YearLeaseAmountPage) must not be defined
    result.get(PremiumsGrantLeasePage) must not be defined

  }

  "must keep that data value when the answer is yes" in {

    val userData = emptyUserAnswers.set(ReceivedGrantLeaseAmountPage, BigDecimal(10.11)).get
      .set(YearLeaseAmountPage, 10).get
      .set(PremiumsGrantLeasePage,  PremiumsGrantLease(yesOrNo = true, Some(BigDecimal(10.12)))).get
      .set(CalculatedFigureYourselfPage, CalculatedFigureYourself(false, None)).get

    val result = userData.set(LeasePremiumPaymentPage, true).success.value

    result.get(LeasePremiumPaymentPage) must be(defined)
    result.get(CalculatedFigureYourselfPage) must be(defined)
    result.get(ReceivedGrantLeaseAmountPage) must be(defined)
    result.get(YearLeaseAmountPage) must be(defined)
    result.get(PremiumsGrantLeasePage) must be(defined)
  }
}
