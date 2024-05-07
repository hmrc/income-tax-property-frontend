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

package controllers

import base.SpecBase
import models.requests.DataRequest
import models.{TotalIncome, User, UserAnswers}
import org.scalatest.prop.TableFor4
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import pages.TotalIncomePage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.JourneyRecoveryContinueView
import views.html.ukrentaroom.UkRentARoomExpensesIntroView

class UkRentARoomExpensesIntroControllerSpec extends SpecBase {
  val taxYear = 2024

  val userAnswersWithoutPropertyIncome = emptyUserAnswers
  val userAnswersWithPropertyIncomeMoreThanEightyFiveThousand = userAnswersWithoutPropertyIncome.set(TotalIncomePage, TotalIncome.Over).get
  val userAnswersWithPropertyIncomeLessThanEightyFiveThousand = userAnswersWithoutPropertyIncome.set(TotalIncomePage, TotalIncome.Between).get

  val withLinks = "https://www.gov.uk/government/publications/self-assessment-uk-property-sa105/uk-property-notes-2022#property-expenses"
  val withNoLinks = "Rent a room expenses"

  val scenarios = Table[Boolean, String, UserAnswers, Option[(Boolean, String)]](
    ("Is Agent", "AgencyOrIndividual", "Property Income", "IsLessThanEightyFiveThousandWithContainingString"),
    (true, "agent", userAnswersWithoutPropertyIncome, None),
    (true, "agent", userAnswersWithPropertyIncomeMoreThanEightyFiveThousand, Some((false, withNoLinks))),
    (true, "agent", userAnswersWithPropertyIncomeLessThanEightyFiveThousand, Some((true, withLinks))),
    (false, "individual", userAnswersWithoutPropertyIncome, None),
    (false, "individual", userAnswersWithPropertyIncomeMoreThanEightyFiveThousand, Some((false, withNoLinks))),
    (false, "individual", userAnswersWithPropertyIncomeLessThanEightyFiveThousand, Some((true, withLinks)))
  )

  forAll(scenarios) { (isAgent: Boolean, agencyOrIndividual: String, userAnswers: UserAnswers, isLessThanEightyFiveThousandWithContainingString: Option[(Boolean, String)]) => {
    val user = User(
      "",
      "",
      "",
      isAgent
    )
    s"UkRentARoomExpensesIntro Controller isAgent: $isAgent property income: ${(isLessThanEightyFiveThousandWithContainingString.fold("Does not contain")(r => if (r._1) "More" else "Less"))}" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent).build()
        val ukRentARoomExpensesIntroRouteUrl = controllers.ukrentaroom.routes.UkRentARoomExpensesIntroController.onPageLoad(taxYear).url

        running(application) {
          val dataRequest = DataRequest(FakeRequest(GET, ukRentARoomExpensesIntroRouteUrl), "", user, emptyUserAnswers)

          val result = route(application, dataRequest).value

          val view = application.injector.instanceOf[UkRentARoomExpensesIntroView]

          isLessThanEightyFiveThousandWithContainingString.fold({
            status(result) mustEqual SEE_OTHER
          })(r => {
            val (isLessThanEightyFiveThousand, containingString) = r
            status(result) mustEqual OK
            contentAsString(result) mustEqual view(isLessThanEightyFiveThousand)(dataRequest, messages(application)).toString
            contentAsString(result).contains(containingString) mustBe true
          })

        }
      }
    }
  }
  }
}
