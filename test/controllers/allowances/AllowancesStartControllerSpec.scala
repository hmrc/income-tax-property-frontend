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
import connectors.error.{ApiError, SingleErrorBody}
import controllers.exceptions.InternalErrorFailure
import models.Rentals
import models.backend.PropertyDetails
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.BusinessService
import viewmodels.AllowancesStartPage
import views.html.allowances.AllowancesStartView

import java.time.LocalDate
import scala.concurrent.Future

class AllowancesStartControllerSpec extends SpecBase with MockitoSugar {
  val taxYear = 2023

  "AllowancesStart Controller" - {

    "must return OK and the capital allowances for a car page for a GET if cashOrAccruals is false " in {

      val propertyDetails =
        PropertyDetails(Some("uk-property"), Some(LocalDate.now), accrualsOrCash = Some(false), "incomeSourceId")

      val businessService = mock[BusinessService]

      when(businessService.getUkPropertyDetails(any(), any())(any())) thenReturn Future.successful(
        Right(Some(propertyDetails))
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
        .overrides(bind[BusinessService].toInstance(businessService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.AllowancesStartController.onPageLoad(taxYear, Rentals).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AllowancesStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(AllowancesStartPage(taxYear, "agent", cashOrAccruals = false, Rentals))(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and the annual investment allowances page for a GET if cashOrAccruals is true " in {

      val propertyDetails =
        PropertyDetails(Some("uk-property"), Some(LocalDate.now), accrualsOrCash = Some(true), "incomeSourceId")

      val businessService = mock[BusinessService]

      when(businessService.getUkPropertyDetails(any(), any())(any())) thenReturn Future.successful(
        Right(Some(propertyDetails))
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
        .overrides(bind[BusinessService].toInstance(businessService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.AllowancesStartController.onPageLoad(taxYear, Rentals).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AllowancesStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(AllowancesStartPage(taxYear, "agent", cashOrAccruals = true, Rentals))(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the overview if there is no result" in {
      val businessService = mock[BusinessService]

      when(businessService.getUkPropertyDetails(any(), any())(any())) thenReturn Future.successful(
        Left(ApiError(NOT_FOUND, SingleErrorBody(NOT_FOUND.toString, "No data found")))
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
        .overrides(bind[BusinessService].toInstance(businessService))
        .build()

      running(application) {
        val failure = intercept[InternalErrorFailure] {
          val request = FakeRequest(GET, routes.AllowancesStartController.onPageLoad(taxYear, Rentals).url)
          status(route(application, request).value)
        }
        failure.getMessage mustBe "Encountered an issue retrieving property data from the business API"
      }
    }
  }
}
