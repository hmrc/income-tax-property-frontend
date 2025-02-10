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

package controllers.ukrentaroom.allowances

import base.SpecBase
import models.IncomeSourcePropertyType.UKProperty
import models.backend.{BusinessDetails, HttpParserError, PropertyDetails}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.BusinessService
import viewmodels.RRAllowancesStartPage
import views.html.ukrentaroom.allowances.RRAllowancesStartView

import java.time.LocalDate
import scala.concurrent.Future

class RRAllowancesStartControllerSpec extends SpecBase with MockitoSugar {
  val taxYear = 2023

  "RRAllowancesStart Controller" - {

    "must return OK and the capital allowances for a car page for a GET if cashOrAccruals is false " in {

      val propertyDetails =
        PropertyDetails(Some(UKProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(false), "incomeSourceId")
      val businessDetails = BusinessDetails(List(propertyDetails))

      val businessService = mock[BusinessService]

      when(businessService.getBusinessDetails(any())(any())) thenReturn Future.successful(Right(businessDetails))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
        .overrides(bind[BusinessService].toInstance(businessService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.RRAllowancesStartController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RRAllowancesStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(RRAllowancesStartPage(taxYear, "agent", cashOrAccruals = false))(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and the annual investment allowances page for a GET if cashOrAccruals is true " in {

      val propertyDetails =
        PropertyDetails(Some(UKProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(true), "incomeSourceId")
      val businessDetails = BusinessDetails(List(propertyDetails))

      val businessService = mock[BusinessService]

      when(businessService.getBusinessDetails(any())(any())) thenReturn Future.successful(Right(businessDetails))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
        .overrides(bind[BusinessService].toInstance(businessService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.RRAllowancesStartController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RRAllowancesStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(RRAllowancesStartPage(taxYear, "agent", cashOrAccruals = true))(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the overview if there is no result" in {
      val businessService = mock[BusinessService]

      when(businessService.getBusinessDetails(any())(any())) thenReturn Future.successful(
        Left(HttpParserError(NOT_FOUND))
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
        .overrides(bind[BusinessService].toInstance(businessService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.RRAllowancesStartController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SummaryController.show(taxYear).url)
      }
    }
  }
}
