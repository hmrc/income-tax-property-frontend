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

package pages.ukandforeignproperty

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.JsPath

class ReportIncomePageSpec extends AnyFreeSpec with Matchers {

  "ReportIncomePage" - {

    "must have the correct path" in {
      ReportIncomePage.path mustEqual JsPath \ "ukAndForeignPropertyAbout" \ "reportIncome"
    }

    "must have the correct toString" in {
      ReportIncomePage.toString mustEqual "reportIncome"
    }
  }
}