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

package controllers.structurebuildingallowance

import base.SpecBase
import connectors.error.ApiError
import controllers.structuresbuildingallowance.routes
import forms.structurebuildingallowance.SbaClaimsFormProvider
import models.PropertyType.toPath
import models.backend.PropertyDetails
import models.{PropertyType, Rentals, RentalsRentARoom, StructuredBuildingAllowanceAddress, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.structurebuildingallowance.{ClaimStructureBuildingAllowancePage, StructureBuildingAllowance, StructureBuildingAllowanceGroup}
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import service.{BusinessService, PropertySubmissionService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.summarylist._
import views.html.structurebuildingallowance.SbaClaimsView

import java.time.LocalDate
import scala.concurrent.Future

class SbaClaimsControllerSpec extends SpecBase with MockitoSugar {

  lazy val rentalsSbaClaimsRoute: String = routes.SbaClaimsController.onPageLoad(taxYear, Rentals).url
  lazy val rentalsRentARoomSbaClaimsRoute: String = routes.SbaClaimsController.onPageLoad(taxYear, RentalsRentARoom).url
  val formProvider = new SbaClaimsFormProvider()
  val form: Form[Boolean] = formProvider("agent")
  val taxYear = 2024
  val agent = "agent"
  val list: SummaryList = SummaryListViewModel(Seq.empty)
  val structureBuildingQualifyingAmount = 100
  val structureBuildingAllowanceClaim = 200

  def rentalsOnwardRouteAddClaim(propertyType: PropertyType): Call =
    Call(
      "GET",
      s"/update-and-submit-income-tax-return/property/$taxYear/${toPath(propertyType)}/structures-buildings-allowance/add-claim"
    )
  def onwardRouteNoOtherClaim(propertyType: PropertyType): Call =
    Call(
      "GET",
      s"/update-and-submit-income-tax-return/property/$taxYear/${toPath(propertyType)}/structures-buildings-allowance/complete-yes-no"
    )

  "SbaClaims Controller" - {

    "must return OK and the correct view for a GET for both the Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      val view = application.injector.instanceOf[SbaClaimsView]

      running(application) {
        // Rentals
        val rentalsRequest = FakeRequest(GET, rentalsSbaClaimsRoute)
        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual OK
        contentAsString(rentalsResult) mustEqual view(form, list, taxYear, agent, Rentals)(
          rentalsRequest,
          messages(application)
        ).toString

        // Rentals and Rent a Room
        val rentalsAndRentARoomRequest = FakeRequest(GET, rentalsRentARoomSbaClaimsRoute)
        val rentalsRentARoomResult = route(application, rentalsAndRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual OK
        contentAsString(rentalsRentARoomResult) mustEqual view(form, list, taxYear, agent, RentalsRentARoom)(
          rentalsAndRentARoomRequest,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted for both the Rentals and Rentals and Rent a Room journeys" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(rentalsOnwardRouteAddClaim(Rentals))),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        // Rentals
        val rentalsRequest =
          FakeRequest(POST, rentalsSbaClaimsRoute)
            .withFormUrlEncodedBody(("anotherClaim", "true"))

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual rentalsOnwardRouteAddClaim(Rentals).url

        // Rentals and Rent a Room
        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomSbaClaimsRoute)
            .withFormUrlEncodedBody(("anotherClaim", "true"))

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value
        status(rentalsRentARoomResult) mustEqual SEE_OTHER
      }
    }

    "must return a Bad Request and errors when invalid data is submitted for both the Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()
      val boundForm = form.bind(Map("anotherClaim" -> ""))
      val view = application.injector.instanceOf[SbaClaimsView]

      running(application) {
        // Rentals
        val rentalsRequest =
          FakeRequest(POST, rentalsSbaClaimsRoute)
            .withFormUrlEncodedBody(("anotherClaim", ""))

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual BAD_REQUEST
        contentAsString(rentalsResult) mustEqual view(boundForm, list, taxYear, agent, Rentals)(
          rentalsRequest,
          messages(application)
        ).toString

        // Rentals and Rent a Room
        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomSbaClaimsRoute)
            .withFormUrlEncodedBody(("anotherClaim", ""))

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual BAD_REQUEST
        contentAsString(rentalsRentARoomResult) mustEqual view(boundForm, list, taxYear, agent, RentalsRentARoom)(
          rentalsRentARoomRequest,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found for both the Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        // Rentals
        val rentalsRequest = FakeRequest(GET, rentalsSbaClaimsRoute)
        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        // Rentals and Rent a Room
        val rentalsRentARoomRequest = FakeRequest(GET, rentalsRentARoomSbaClaimsRoute)
        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual SEE_OTHER
        redirectLocation(rentalsRentARoomResult).value mustEqual controllers.routes.JourneyRecoveryController
          .onPageLoad()
          .url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found for both the Rentals and Rentals and Rent a Room journeys" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        // Rentals
        val rentalsRequest =
          FakeRequest(POST, rentalsSbaClaimsRoute)
            .withFormUrlEncodedBody(("anotherClaim", "true"))

        val rentalsResult = route(application, rentalsRequest).value

        status(rentalsResult) mustEqual SEE_OTHER
        redirectLocation(rentalsResult).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        // Rentals and Rent a Room
        val rentalsRentARoomRequest =
          FakeRequest(POST, rentalsRentARoomSbaClaimsRoute)
            .withFormUrlEncodedBody(("anotherClaim", "true"))

        val rentalsRentARoomResult = route(application, rentalsRentARoomRequest).value

        status(rentalsRentARoomResult) mustEqual SEE_OTHER
        redirectLocation(rentalsRentARoomResult).value mustEqual controllers.routes.JourneyRecoveryController
          .onPageLoad()
          .url
      }
    }

    "must return OK and the POST for onSubmit() should redirect to the correct URL for both the Rentals and Rentals and Rent a Room journeys" in {

      def userAnswers(propertyType: PropertyType): Option[UserAnswers] =
        UserAnswers("structures-buildings-allowance-user-answers")
          .set(
            page = StructureBuildingAllowanceGroup(propertyType),
            value = Array(
              StructureBuildingAllowance(
                structureBuildingQualifyingDate = LocalDate.now,
                structureBuildingQualifyingAmount = structureBuildingQualifyingAmount,
                structureBuildingAllowanceClaim = structureBuildingAllowanceClaim,
                structuredBuildingAllowanceAddress = StructuredBuildingAllowanceAddress(
                  buildingName = "Park View",
                  buildingNumber = "9",
                  postCode = "SE13 5FG"
                )
              )
            )
          )
          .get
          .set(ClaimStructureBuildingAllowancePage(propertyType), true)
          .toOption

      val mockPropertySubmissionService = mock[PropertySubmissionService]
      val mockBusinessService = mock[BusinessService]

      when(mockPropertySubmissionService.saveJourneyAnswers(any(), any(), any())(any(), any())) thenReturn Future
        .successful(
          Right(())
        )
      when(mockBusinessService.getUkPropertyDetails(any(), any())(any())) thenReturn Future
        .successful[Either[ApiError, Option[PropertyDetails]]](
          Right(
            Some(
              PropertyDetails(
                Some("incomeSourceType"),
                Some(LocalDate.now()),
                accrualsOrCash = Some(true), // true -> Accruals,false -> Cash
                incomeSourceId = "incomeSourceId"
              )
            )
          )
        )

      // Rentals
      val rentalsApplication: Application =
        applicationBuilder(userAnswers = userAnswers(Rentals), isAgent = false)
          .overrides(
            bind[PropertySubmissionService].toInstance(mockPropertySubmissionService),
            bind[BusinessService].toInstance(mockBusinessService)
          )
          .build()

      running(rentalsApplication) {
        val addOtherClaimRequest = FakeRequest(POST, routes.SbaClaimsController.onPageLoad(taxYear, Rentals).url)
          .withFormUrlEncodedBody(("anotherClaim", "true"))

        val addOtherClaimResult = route(rentalsApplication, addOtherClaimRequest).value

        status(addOtherClaimResult) mustEqual SEE_OTHER
        redirectLocation(addOtherClaimResult).value mustEqual rentalsOnwardRouteAddClaim(Rentals).url

        val noOtherClaimRequest = FakeRequest(POST, routes.SbaClaimsController.onPageLoad(taxYear, Rentals).url)
          .withFormUrlEncodedBody(("anotherClaim", "false"))

        val noOtherClaimResult = route(rentalsApplication, noOtherClaimRequest).value

        status(noOtherClaimResult) mustEqual SEE_OTHER
        redirectLocation(noOtherClaimResult).value mustEqual onwardRouteNoOtherClaim(Rentals).url
      }
    }
  }
}
