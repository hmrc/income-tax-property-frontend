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

package navigation

import base.SpecBase
import models.{NormalMode, TotalPropertyIncome, UserAnswers}
import pages.Page
import controllers.ukandforeignproperty.routes
import pages.ukandforeignproperty.TotalPropertyIncomePage

import java.time.LocalDate

class UkAndForeignPropertyNavigatorSpec  extends SpecBase {

  private val navigator = new UkAndForeignPropertyNavigator
  private val taxYear = LocalDate.now.getYear

  "ForeignPropertyNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page

        navigator.nextPage(
          UnknownPage,
          taxYear,
          NormalMode,
          UserAnswers("id"),
          UserAnswers("id")
        ) mustBe controllers.routes.IndexController.onPageLoad
      }

      "must go from a TotalPropertyIncomePage to ReportIncomePage when the option selected is 'less then £1000'" in {

        val ua = UserAnswers("id")
          .set(TotalPropertyIncomePage, TotalPropertyIncome.LessThan).success.value

        navigator.nextPage(
          TotalPropertyIncomePage,
          taxYear,
          NormalMode,
          UserAnswers("id"),
          ua
        ) mustBe routes.ReportIncomeController.onPageLoad(taxYear, NormalMode)
      }

      "must go from a TotalPropertyIncomePage to ReportIncomePage when the option selected is '£1000 or more'" ignore {

        val ua = UserAnswers("id")
          .set(TotalPropertyIncomePage, TotalPropertyIncome.Maximum).success.value

        navigator.nextPage(
          TotalPropertyIncomePage,
          taxYear,
          NormalMode,
          UserAnswers("id"),
          ua
        ) mustBe ???
      }
    }
  }
}