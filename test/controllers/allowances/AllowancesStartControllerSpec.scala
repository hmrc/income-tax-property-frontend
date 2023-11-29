/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.allowances

import base.SpecBase
import models.backend.{BusinessDetails, HttpParserError, PropertyDetails}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.BusinessService
import views.html.allowances.AllowancesStartView

import java.time.LocalDate
import scala.concurrent.Future

class AllowancesStartControllerSpec extends SpecBase with MockitoSugar {
  val taxYear = 2023

  "AllowancesStart Controller" - {

    "must return OK and the correct view for a GET" in {

      val propertyDetails = PropertyDetails(Some("uk-property"), Some(LocalDate.now), cashOrAccruals = Some(false))
      val businessDetails = BusinessDetails(List(propertyDetails))

      val businessService = mock[BusinessService]

      when(businessService.getBusinessDetails(any())(any())) thenReturn Future.successful(Right(businessDetails))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
        .overrides(bind[BusinessService].toInstance(businessService)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AllowancesStartController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AllowancesStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear, "agent", cashOrAccruals = false, "businessDetails.cash")(request, messages(application)).toString
      }
    }

    "must redirect to the overview if there is no result" in {
      val businessService = mock[BusinessService]

      when(businessService.getBusinessDetails(any())(any())) thenReturn Future.successful(Left(HttpParserError(NOT_FOUND)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
        .overrides(bind[BusinessService].toInstance(businessService)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AllowancesStartController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SummaryController.show(taxYear).url)
      }
    }
  }
}
