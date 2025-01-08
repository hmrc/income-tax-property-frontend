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

package controllers.foreign.structurebuildingallowance

import base.SpecBase
import controllers.foreign.structuresbuildingallowance.routes
import models.requests.DataRequest
import models.{Rentals, User}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import pages.foreign.structurebuildingallowance.ForeignStructureBuildingAllowancePage
import views.html.foreign.structurebuildingallowance.ForeignStructureBuildingAllowanceView

class ForeignAddClaimStructureBuildingAllowanceControllerSpec extends SpecBase {

  val taxYear = 2024
  val isAgent = true
  val nextIndex = 0
  val countryCode = "AUS"

  s"must return OK and the correct view for a GET" in {

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
        controllers.foreign.structuresbuildingallowance.routes.ForeignAddClaimStructureBuildingAllowanceController.onPageLoad(taxYear, countryCode).url
      )

      val result = route(application, request).value
      val dataRequest =
        DataRequest(request, "", user, emptyUserAnswers)

      val view = application.injector.instanceOf[ForeignStructureBuildingAllowanceView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(
        ForeignStructureBuildingAllowancePage(
          taxYear = taxYear,
          countryCode = countryCode,
          nextIndex = nextIndex,
          individualOrAgent = "agent"
        )
      )(
        dataRequest,
        messages(application)
      ).toString
    }
  }
}
