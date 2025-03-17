/*
 * Copyright 2025 HM Revenue & Customs
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

import audit.AuditService
import base.SpecBase
import connectors.error.ApiError
import controllers.foreign.structuresbuildingallowance.routes.ForeignStructureBuildingAllowanceClaimsController
import controllers.routes
import forms.ForeignStructureBuildingAllowanceClaimsFormProvider
import models.backend.PropertyDetails
import models.{ForeignStructuresBuildingAllowanceAddress, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.structurebuildingallowance._
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import service.{BusinessService, PropertySubmissionService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.structurebuildingallowance.ForeignStructureBuildingAllowanceClaimsView

import java.time.LocalDate
import scala.concurrent.Future

class ForeignStructureBuildingAllowanceClaimsControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new ForeignStructureBuildingAllowanceClaimsFormProvider()
  private val isAgentMessageKey = "individual"
  val form: Form[Boolean] = formProvider()
  val taxYear = 2024
  val countryCode = "AUS"
  val list: SummaryList = SummaryListViewModel(Seq.empty)
  val index = 1

  lazy val foreignStructureBuildingAllowanceClaimsRoute: String =
    ForeignStructureBuildingAllowanceClaimsController.onPageLoad(taxYear, countryCode).url
  val onwardRoute: Call = Call(
    "GET",
    s"/update-and-submit-income-tax-return/property/$taxYear/foreign-property/structures-buildings-allowance/$countryCode/complete-yes-no"
  )
  val addAnotherClaimOnwardRoute: Call = Call(
    "GET",
    s"/update-and-submit-income-tax-return/property/$taxYear/foreign-property/structures-buildings-allowance/$countryCode/$index/qualifying-date"
  )

  "ForeignStructureBuildingAllowanceClaims Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignStructureBuildingAllowanceClaimsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignStructureBuildingAllowanceClaimsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, list, taxYear, countryCode, isAgentMessageKey)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignStructureBuildingAllowanceClaimsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER

      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignStructureBuildingAllowanceClaimsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ForeignStructureBuildingAllowanceClaimsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, list, taxYear, countryCode, isAgentMessageKey)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, foreignStructureBuildingAllowanceClaimsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignStructureBuildingAllowanceClaimsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the POST for onSubmit() should redirect to the correct URL" in {
      val ua = UserAnswers("test")
        .set(
          page = ForeignStructureBuildingAllowanceGroup(countryCode),
          value = Array(
            ForeignStructureBuildingAllowance(
              20.00,
              LocalDate.now,
              56.00,
              ForeignStructuresBuildingAllowanceAddress("Building 1", "1", "BR1 1RR")
            )
          )
        )
        .get
        .set(ForeignClaimStructureBuildingAllowancePage(countryCode), true)
        .toOption

      val mockPropertySubmissionService = mock[PropertySubmissionService]
      val mockBusinessService = mock[BusinessService]

      when(mockPropertySubmissionService.saveForeignPropertyJourneyAnswers(any(),any())(any(), any())) thenReturn Future
        .successful(
          Right(())
        )
      when(mockBusinessService.getForeignPropertyDetails(any(), any())(any())) thenReturn Future
        .successful[Either[ApiError, Option[PropertyDetails]]](
          Right(
            Some(
              PropertyDetails(
                Some("incomeSourceType"),
                Some(LocalDate.now()),
                accrualsOrCash = Some(true),
                incomeSourceId = "incomeSourceId"
              )
            )
          )
        )

      val application: Application =
        applicationBuilder(userAnswers = ua, isAgent = false)
          .overrides(
            bind[PropertySubmissionService].toInstance(mockPropertySubmissionService),
            bind[BusinessService].toInstance(mockBusinessService)
          )
          .overrides(bind[AuditService].toInstance(audit))
          .build()

      running(application) {
        val addOtherClaimRequest = FakeRequest(POST, foreignStructureBuildingAllowanceClaimsRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val addOtherClaimResult = route(application, addOtherClaimRequest).value

        status(addOtherClaimResult) mustEqual SEE_OTHER
        redirectLocation(addOtherClaimResult).value mustEqual addAnotherClaimOnwardRoute.url

        val noOtherClaimRequest = FakeRequest(POST, foreignStructureBuildingAllowanceClaimsRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val noOtherClaimResult = route(application, noOtherClaimRequest).value

        status(noOtherClaimResult) mustEqual SEE_OTHER
        verify(audit, times(1)).sendAuditEvent(any())(any(), any())
        redirectLocation(noOtherClaimResult).value mustEqual onwardRoute.url
      }
    }

  }
}
