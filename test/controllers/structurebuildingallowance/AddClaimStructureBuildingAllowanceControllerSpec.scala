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

package controllers.structurebuildingallowance

import base.SpecBase
import controllers.structuresbuildingallowance.routes
import models.requests.DataRequest
import models.{Rentals, User}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.checkAnswers.structurebuildingallowance.StructureBuildingAllowancePage
import views.html.structurebuildingallowance.StructureBuildingAllowanceView

class AddClaimStructureBuildingAllowanceControllerSpec extends SpecBase {

  val taxYear = 2024
  val isAgent = true

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
        routes.AddClaimStructureBuildingAllowanceController.onPageLoad(taxYear, Rentals).url
      )

      val result = route(application, request).value
      val dataRequest =
        DataRequest(request, "", user, emptyUserAnswers)

      val view = application.injector.instanceOf[StructureBuildingAllowanceView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(
        StructureBuildingAllowancePage(
          taxYear = taxYear,
          nextIndex = 0,
          individualOrAgent = "agent",
          propertyType = Rentals
        )
      )(
        dataRequest,
        messages(application)
      ).toString
    }
  }
}
