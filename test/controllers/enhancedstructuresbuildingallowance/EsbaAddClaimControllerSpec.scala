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

package controllers.enhancedstructuresbuildingallowance

import base.SpecBase
import models.requests.DataRequest
import models.{Rentals, User}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.checkAnswers.enhancedstructurebuildingallowance.EsbaAddClaimPage
import views.html.enhancedstructuresbuildingallowance.EsbaAddClaimView

import scala.util.Random

class EsbaAddClaimControllerSpec extends SpecBase {

  val taxYear = 2024
  private val individualAgent = Array("individual", "agent")
  private val individualOrAgent = individualAgent(Random.nextInt(individualAgent.length))
  private val isAgent = individualOrAgent.equals("agent")

  s"EsbaAddClaimController for an $individualOrAgent must return OK and the correct view for a GET" in {

    val user = User(
      "",
      "",
      "",
      agentRef = Option.when(isAgent)("agentReferenceNumber")
    )

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent).build()

    running(application) {
      val request = FakeRequest(
        GET,
        routes.EsbaAddClaimController.onPageLoad(taxYear, Rentals).url
      )

      val result = route(application, request).value
      val dataRequest =
        DataRequest(request, "", user, emptyUserAnswers)

      val view = application.injector.instanceOf[EsbaAddClaimView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(
        EsbaAddClaimPage(
          taxYear = taxYear,
          nextIndex = 0,
          individualOrAgent = individualOrAgent,
          propertyType = Rentals
        )
      )(
        dataRequest,
        messages(application)
      ).toString
    }
  }
}
