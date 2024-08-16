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
import controllers.structuresbuildingallowance.routes
import forms.structurebuildingallowance.SbaClaimsFormProvider
import models.{StructuredBuildingAllowanceAddress, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.structurebuildingallowance.StructureBuildingAllowance
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.summarylist._
import views.html.structurebuildingallowance.SbaClaimsView

import java.time.LocalDate
import scala.concurrent.Future

class SbaClaimsControllerSpec extends SpecBase with MockitoSugar {

  lazy val sbaClaimsRoute: String = routes.SbaClaimsController.onPageLoad(taxYear).url
  val formProvider = new SbaClaimsFormProvider()
  val form: Form[Boolean] = formProvider("agent")
  val taxYear = 2024
  val agent = "agent"
  val list: SummaryList = SummaryListViewModel(Seq.empty)
  val structureBuildingQualifyingAmount = 100
  val structureBuildingAllowanceClaim = 200

  def onwardRouteAddClaim: Call =
    Call("GET", s"/update-and-submit-income-tax-return/property/$taxYear/rentals/structures-buildings-allowance/add-claim")

  def onwardRouteNoOtherClaim: Call =
    Call("GET", s"/update-and-submit-income-tax-return/property/$taxYear/rentals/structures-buildings-allowance/complete-yes-no")

  "SbaClaims Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, sbaClaimsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SbaClaimsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, list, taxYear, agent)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRouteAddClaim)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, sbaClaimsRoute)
            .withFormUrlEncodedBody(("anotherClaim", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRouteAddClaim.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, sbaClaimsRoute)
            .withFormUrlEncodedBody(("anotherClaim", ""))

        val boundForm = form.bind(Map("anotherClaim" -> ""))

        val view = application.injector.instanceOf[SbaClaimsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, list, taxYear, agent)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, sbaClaimsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, sbaClaimsRoute)
            .withFormUrlEncodedBody(("anotherClaim", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the POST for onSubmit() should redirect to the correct URL" in {

      val userAnswers: Option[UserAnswers] =
        UserAnswers("structures-buildings-allowance-user-answers")
          .set(
            page = StructureBuildingAllowance,
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
          .toOption

      val application: Application = applicationBuilder(userAnswers = userAnswers, isAgent = false).build()

      running(application) {
        val addOtherClaimRequest = FakeRequest(POST, routes.SbaClaimsController.onPageLoad(taxYear).url)
          .withFormUrlEncodedBody(("anotherClaim", "true"))

        val addOtherClaimResult = route(application, addOtherClaimRequest).value

        status(addOtherClaimResult) mustEqual SEE_OTHER
        redirectLocation(addOtherClaimResult).value mustEqual onwardRouteAddClaim.url

        val noOtherClaimRequest = FakeRequest(POST, routes.SbaClaimsController.onPageLoad(taxYear).url)
          .withFormUrlEncodedBody(("anotherClaim", "false"))

        val noOtherClaimResult = route(application, noOtherClaimRequest).value

        status(noOtherClaimResult) mustEqual SEE_OTHER
        redirectLocation(noOtherClaimResult).value mustEqual onwardRouteNoOtherClaim.url
      }
    }
  }
}
