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

package viewmodels.checkAnswers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class FormatUtilsSpec extends AnyWordSpec with Matchers {

  ".bigDecimalCurrency" should {
    "Place comma in appropriate place when given amount over 999" in {
      FormatUtils.bigDecimalCurrency(45000.10) shouldBe "£45,000.10"
    }
  }
}