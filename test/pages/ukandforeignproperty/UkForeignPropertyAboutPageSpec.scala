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

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.libs.json.JsPath

class UkForeignPropertyAboutPageSpec extends AnyFreeSpec with Matchers with OptionValues {

  "UkForeignPropertyAboutPage" - {
    "have the correct path" in {
      UkForeignPropertyAboutPage.path mustEqual JsPath \ "ukAndForeignAbout"
    }

    "have the correct toString value" in {
      UkForeignPropertyAboutPage.toString shouldEqual "ukAndForeignAbout"
    }
  }
}