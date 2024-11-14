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

package controllers.foreign.income

import base.SpecBase
import org.scalatest.prop.TableFor1
import org.scalatest.prop.Tables.Table
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.foreign.Country
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.foreign.income.ForeignPropertyIncomeStartView

import java.time.LocalDate

class ForeignPropertyIncomeStartControllerSpec extends SpecBase {

  val taxYear: Int = LocalDate.now().getYear
  val country: Country = Country("United States of America", "USA")
  val scenarios: TableFor1[String] = Table[String]("individualOrAgent", "individual", "agent")

  forAll(scenarios) { (individualOrAgent: String) =>
    val isAgent: Boolean = individualOrAgent == "agent"

    s"ForeignPropertyIncome Controller for an $individualOrAgent" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = isAgent).build()

        running(application) {
          val request = FakeRequest(GET, routes.ForeignPropertyIncomeStartController.onPageLoad(taxYear, country.code).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ForeignPropertyIncomeStartView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(taxYear, individualOrAgent, country)(request, messages(application)).toString
        }
      }
    }
  }
}
