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

package controllers.furnishedholidaylettings

import base.SpecBase
import models.User
import models.requests.DataRequest
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.furnishedholidaylettings.FhlIntroView

class FhlIntroControllerSpec extends SpecBase {
  val taxYear = 2024
  val scenarios = Table[Boolean](
    ("isAgent"),
    (false),
    (true))

  forAll(scenarios) { (isAgent: Boolean) =>
    val user = User(
      "",
      "",
      "",
      isAgent,
      Some("agentReferenceNumber")
    )
    s"FhlIntro Controller isAgent:$isAgent" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent).build()

        running(application) {
          val fakeRequest = FakeRequest(GET, controllers.furnishedholidaylettings.routes.FhlIntroController.onPageLoad(taxYear).url)

          val request = DataRequest(fakeRequest, "", user, emptyUserAnswers)

          val result = route(application, request).value

          val view = application.injector.instanceOf[FhlIntroView]

          status(result) mustEqual OK
          val contentText = contentAsString(result)
          contentText mustEqual view(taxYear)(request, messages(application)).toString
          val doesContainAgentRelatedWord = contentText.contains("client")
          if (isAgent) {
            doesContainAgentRelatedWord mustBe true
          } else {
            doesContainAgentRelatedWord mustBe false
          }
        }
      }
    }
  }
}
