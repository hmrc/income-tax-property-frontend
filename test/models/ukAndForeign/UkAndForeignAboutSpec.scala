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

package models.ukAndForeign

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsSuccess, Json}
import models.{ReportIncome, TotalPropertyIncome}
import pages.ukandforeignproperty.UkRentalPropertyIncomePage

class UkAndForeignAboutSpec extends PlaySpec {

  "UkAndForeignAbout" should {

    "write and read correctly" in {
      val totalPropertyIncome = TotalPropertyIncome.LessThan
      val reportIncome = Some(ReportIncome.WantToReport)

      val aboutUkAndForeign = AboutUkAndForeign(
        totalPropertyIncome = totalPropertyIncome,
        reportIncome = reportIncome,
        ukPropertyRentalType = None,
        countries = None,
        claimExpensesOrRelief = None,
        claimPropertyIncomeAllowanceOrExpenses = None
      )

      val ukAndForeignAbout = UkAndForeignAbout(aboutUkAndForeign, None, None)
      val json = Json.toJson(ukAndForeignAbout)
      json.validate[UkAndForeignAbout] mustEqual JsSuccess(ukAndForeignAbout)
    }

    "fail to read invalid JSON" in {
      val invalidJson = Json.parse("""{"totalPropertyIncome": "invalid"}""")

      invalidJson.validate[UkAndForeignAbout].isError mustBe true
    }
  }
}