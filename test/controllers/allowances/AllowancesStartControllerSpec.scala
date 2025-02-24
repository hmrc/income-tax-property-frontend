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
import models.IncomeSourcePropertyType.UKProperty
import models.backend.PropertyDetails
import models.{ClaimExpensesOrRelief, NormalMode, Rentals, RentalsRentARoom, UKPropertySelect, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import pages.UKPropertyPage
import pages.propertyrentals.ClaimPropertyIncomeAllowancePage
import pages.ukrentaroom.ClaimExpensesOrReliefPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.BusinessService
import viewmodels.AllowancesStartPage
import views.html.allowances.AllowancesStartView

import java.time.LocalDate
import scala.concurrent.Future

class AllowancesStartControllerSpec extends SpecBase with MockitoSugar {
  val taxYear: Int = 2023
  val isPIA: Boolean = true
  "AllowancesStart Controller" - {

    "must return OK and the capital allowances for a car page for a GET if cashOrAccruals is false " in {

      val propertyDetails =
        PropertyDetails(Some(UKProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(false), "incomeSourceId")

      val businessService = mock[BusinessService]

      when(businessService.getUkPropertyDetails(any(), any())(any())) thenReturn Future.successful(
        Right(Some(propertyDetails))
      )
      val userAnswers = UserAnswers("test").set(ClaimPropertyIncomeAllowancePage(RentalsRentARoom), true).get
      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true)
        .overrides(bind[BusinessService].toInstance(businessService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.AllowancesStartController.onPageLoad(taxYear, Rentals).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AllowancesStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          AllowancesStartPage(taxYear, "agent", cashOrAccruals = false, emptyUserAnswers, Rentals), isPIA
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must return a link to the Annual Investment Allowance when Rent a Room Relief = true, Property Income Allowance = No, claim expenses " +
      "and cashOrAccruals = true" in {

        val propertyDetails =
          PropertyDetails(Some(UKProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(true), "incomeSourceId")

        val businessService = mock[BusinessService]

        val userAnswers = UserAnswers(userAnswersId)
          .set(
            UKPropertyPage,
            UKPropertySelect.values.toSet
          )
          .success
          .value
          .set(ClaimExpensesOrReliefPage(RentalsRentARoom), ClaimExpensesOrRelief(true, Some(100)))
          .success
          .value
          .set(ClaimPropertyIncomeAllowancePage(RentalsRentARoom), false)
          .success
          .value

        when(businessService.getUkPropertyDetails(any(), any())(any())) thenReturn Future.successful(
          Right(Some(propertyDetails))
        )

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true)
          .overrides(bind[BusinessService].toInstance(businessService))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.AllowancesStartController.onPageLoad(taxYear, RentalsRentARoom).url)
          val result = route(application, request).value

          status(result) mustEqual OK

          AllowancesStartPage(
            taxYear,
            "agent",
            cashOrAccruals = true,
            userAnswers = userAnswers,
            propertyType = RentalsRentARoom
          ).nextPageUrl shouldBe controllers.allowances.routes.AnnualInvestmentAllowanceController
            .onPageLoad(taxYear, NormalMode, RentalsRentARoom)
            .url
        }
      }

    "must return a link to the Annual Investment Allowance when Rent a Room Relief = false, Property Income Allowance = No, claim expenses " +
      "and cashOrAccruals = true" in {

        val propertyDetails =
          PropertyDetails(Some(UKProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(true), "incomeSourceId")

        val businessService = mock[BusinessService]

        val userAnswers = UserAnswers(userAnswersId)
          .set(
            UKPropertyPage,
            UKPropertySelect.values.toSet
          )
          .success
          .value
          .set(ClaimExpensesOrReliefPage(RentalsRentARoom), ClaimExpensesOrRelief(false, None))
          .success
          .value
          .set(ClaimPropertyIncomeAllowancePage(RentalsRentARoom), false)
          .success
          .value

        when(businessService.getUkPropertyDetails(any(), any())(any())) thenReturn Future.successful(
          Right(Some(propertyDetails))
        )

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true)
          .overrides(bind[BusinessService].toInstance(businessService))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.AllowancesStartController.onPageLoad(taxYear, RentalsRentARoom).url)
          val result = route(application, request).value

          status(result) mustEqual OK

          AllowancesStartPage(
            taxYear,
            "agent",
            cashOrAccruals = true,
            userAnswers = userAnswers,
            propertyType = RentalsRentARoom
          ).nextPageUrl shouldBe controllers.allowances.routes.AnnualInvestmentAllowanceController
            .onPageLoad(taxYear, NormalMode, RentalsRentARoom)
            .url
        }
      }

    "must return a link to the Annual Investment Allowance when Rent a Room Relief = false, Property Income Allowance = Yes, claim property income allowance " +
      "and cashOrAccruals = false" in {

        val propertyDetails =
          PropertyDetails(Some(UKProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(false), "incomeSourceId")

        val businessService = mock[BusinessService]

        val userAnswers = UserAnswers(userAnswersId)
          .set(
            UKPropertyPage,
            UKPropertySelect.values.toSet
          )
          .success
          .value
          .set(ClaimExpensesOrReliefPage(RentalsRentARoom), ClaimExpensesOrRelief(false, None))
          .success
          .value
          .set(ClaimPropertyIncomeAllowancePage(RentalsRentARoom), true)
          .success
          .value

        when(businessService.getUkPropertyDetails(any(), any())(any())) thenReturn Future.successful(
          Right(Some(propertyDetails))
        )

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true)
          .overrides(bind[BusinessService].toInstance(businessService))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.AllowancesStartController.onPageLoad(taxYear, Rentals).url)
          val result = route(application, request).value

          status(result) mustEqual OK

          AllowancesStartPage(
            taxYear,
            "agent",
            cashOrAccruals = false,
            userAnswers = userAnswers,
            propertyType = RentalsRentARoom
          ).nextPageUrl shouldBe controllers.allowances.routes.CapitalAllowancesForACarController
            .onPageLoad(taxYear, NormalMode, RentalsRentARoom)
            .url
        }
      }

    "must return OK and the annual investment allowances page for a GET if cashOrAccruals is true " in {

      val propertyDetails =
        PropertyDetails(Some(UKProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(true), "incomeSourceId")

      val businessService = mock[BusinessService]

      when(businessService.getUkPropertyDetails(any(), any())(any())) thenReturn Future.successful(
        Right(Some(propertyDetails))
      )
      val userAnswers = UserAnswers("test").set(ClaimPropertyIncomeAllowancePage(Rentals), true).get
      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true)
        .overrides(bind[BusinessService].toInstance(businessService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.AllowancesStartController.onPageLoad(taxYear, Rentals).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AllowancesStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          AllowancesStartPage(taxYear, "agent", cashOrAccruals = true, emptyUserAnswers, Rentals), isPIA
        )(
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
    "must redirect to journey recovery if accrualsOrCash is None" in {
      val accrualsOrCash = None

      val propertyDetails =
        PropertyDetails(Some(UKProperty.toString), Some(LocalDate.now), accrualsOrCash = accrualsOrCash, "incomeSourceId")

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

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
