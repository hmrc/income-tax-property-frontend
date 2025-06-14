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

package viewmodels

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.{Lang, Messages}
import play.api.test.Helpers.{stubLangs, stubMessagesApi}

import java.time.LocalDate
import java.util.Locale

class PropertyDetailsPageSpec extends AnyWordSpec with Matchers {
  implicit val messages: Messages = stubMessagesApi().preferred(List(Lang(Locale.ENGLISH)))

  "cashOrAccrualsMessageKey" should {
    val taxYear = LocalDate.now().getYear

    "output the cash message key when it is false" in {
      PropertyDetailsPage(
        taxYear,
        "individual",
        LocalDate.now,
        accrualsOrCash = true
      ).cashOrAccrualsMessageKey shouldBe "businessDetails.accruals"
    }

    "output the cash message key when it is true" in {
      PropertyDetailsPage(
        taxYear,
        "individual",
        LocalDate.now,
        accrualsOrCash = false
      ).cashOrAccrualsMessageKey shouldBe "businessDetails.cash"
    }

    "tradingStartDateFormatted" should {
      "output the English message Date" in {
        PropertyDetailsPage(
          taxYear,
          "individual",
          LocalDate.of(2020, 10, 10),
          accrualsOrCash = false
        ).tradingStartDateFormatted shouldBe "10 Oct 2020"
      }
      "output the Welsh message Date" in {
        implicit val messages: Messages =
          stubMessagesApi(langs = stubLangs(List(Lang("cy")))).preferred(List(Lang("cy")))
        PropertyDetailsPage(
          taxYear,
          "individual",
          LocalDate.of(2020, 10, 15),
          accrualsOrCash = false
        ).tradingStartDateFormatted shouldBe "15 Hyd 2020"
      }

    }
  }
}
