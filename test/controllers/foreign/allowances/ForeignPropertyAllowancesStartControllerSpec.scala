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

package controllers.foreign.allowances

import base.SpecBase
import connectors.error.{ApiError, SingleErrorBody}
import controllers.exceptions.InternalErrorFailure
import controllers.foreign.allowances.routes.ForeignPropertyAllowancesStartController
import models.IncomeSourcePropertyType.ForeignProperty
import models.UserAnswers
import models.backend.PropertyDetails
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.{Country, IncomeSourceCountries}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.BusinessService
import testHelpers.Fixture
import views.html.foreign.allowances.ForeignPropertyAllowancesStartView

import java.time.LocalDate
import scala.concurrent.Future

class ForeignPropertyAllowancesStartControllerSpec extends SpecBase with MockitoSugar with Fixture {

  val taxYear = 2024
  val countryCode = "AUS"
  val countryName = "Australia"
  val isAgentMessageKey = "individual"
  "ForeignAllowancesStart Controller" - {

    "must return OK and the correct view for a GET if Traditional accounting" in {
      val accrualsOrCash = true
      val userAnswers = UserAnswers("test").set(IncomeSourceCountries, Array(Country(countryName, countryCode))).get

      val propertyDetails =
        PropertyDetails(Some(ForeignProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(accrualsOrCash), "incomeSourceId")

      val businessService = mock[BusinessService]

      when(businessService.getForeignPropertyDetails(any(), any())(any())) thenReturn Future.successful(
        Right(Some(propertyDetails))
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false)
        .overrides(bind[BusinessService].toInstance(businessService))
        .build()


      running(application) {
        val request = FakeRequest(GET, ForeignPropertyAllowancesStartController.onPageLoad(taxYear, countryCode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignPropertyAllowancesStartView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(taxYear, countryCode, countryName, isAgentMessageKey, accrualsOrCash)(request, messages(application)).toString
      }
    }
    "must return OK and the correct view for a GET if cash based accounting" in {
      val accrualsOrCash = false
      val userAnswers = UserAnswers("test").set(IncomeSourceCountries, Array(Country(countryName, countryCode))).get

      val propertyDetails =
        PropertyDetails(Some(ForeignProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(accrualsOrCash), "incomeSourceId")

      val businessService = mock[BusinessService]

      when(businessService.getForeignPropertyDetails(any(), any())(any())) thenReturn Future.successful(
        Right(Some(propertyDetails))
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false)
        .overrides(bind[BusinessService].toInstance(businessService))
        .build()


      running(application) {
        val request = FakeRequest(GET, ForeignPropertyAllowancesStartController.onPageLoad(taxYear, countryCode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignPropertyAllowancesStartView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(taxYear, countryCode, countryName, isAgentMessageKey, accrualsOrCash)(request, messages(application)).toString
      }
    }

    "must redirect to the overview if there is no result" in {
      val businessService = mock[BusinessService]
      val userAnswers = UserAnswers("test").set(IncomeSourceCountries, Array(Country(countryName, countryCode))).get
      when(businessService.getForeignPropertyDetails(any(), any())(any())) thenReturn Future.successful(
        Left(ApiError(NOT_FOUND, SingleErrorBody(NOT_FOUND.toString, "No data found")))
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true)
        .overrides(bind[BusinessService].toInstance(businessService))
        .build()

      running(application) {
        val failure = intercept[InternalErrorFailure] {
          val request = FakeRequest(GET, ForeignPropertyAllowancesStartController.onPageLoad(taxYear, countryCode).url)
          status(route(application, request).value)
        }
        failure.getMessage mustBe "Encountered an issue retrieving property data from the business API"
      }
    }
    "must redirect to journey recovery if accrualsOrCash is None" in {
      val accrualsOrCash = None
      val userAnswers = UserAnswers("test").set(IncomeSourceCountries, Array(Country(countryName, countryCode))).get

      val propertyDetails =
        PropertyDetails(Some(ForeignProperty.toString), Some(LocalDate.now), accrualsOrCash = accrualsOrCash, "incomeSourceId")

      val businessService = mock[BusinessService]

      when(businessService.getForeignPropertyDetails(any(), any())(any())) thenReturn Future.successful(
        Right(Some(propertyDetails))
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true)
        .overrides(bind[BusinessService].toInstance(businessService))
        .build()

      running(application) {
        val request = FakeRequest(GET, ForeignPropertyAllowancesStartController.onPageLoad(taxYear, countryCode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
