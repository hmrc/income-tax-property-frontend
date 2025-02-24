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
import controllers.foreign.structuresbuildingallowance.routes.ForeignClaimSbaCheckYourAnswersController
import models.JourneyPath.ForeignStructureBuildingAllowance
import models.{JourneyContext, UserAnswers}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import pages.foreign.structurebuildingallowance.{ForeignClaimStructureBuildingAllowancePage, ForeignSbaCompletePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.{BusinessService, PropertySubmissionService}
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.structurebuildingallowance.ForeignClaimSbaCheckYourAnswersView

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class ForeignClaimSbaCheckYourAnswersControllerSpec extends SpecBase {

  val taxYear: Int = 2024
  val countryCode: String = "AUS"
  val onwardRoute: Call = Call(
    "GET",
    s"/update-and-submit-income-tax-return/property/$taxYear/foreign-property/structures-buildings-allowance/$countryCode/complete-yes-no"
  )

  "ForeignClaimSbaCheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()
      val list = SummaryListViewModel(Seq.empty)

      running(application) {
        val request = FakeRequest(GET, ForeignClaimSbaCheckYourAnswersController.onPageLoad(taxYear, countryCode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignClaimSbaCheckYourAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, taxYear, countryCode)(request, messages(application)).toString
      }
    }
    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, ForeignClaimSbaCheckYourAnswersController.onPageLoad(taxYear, countryCode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the POST for onSubmit() should redirect to the correct URL" in {

      val userAnswers = UserAnswers("test").set(ForeignSbaCompletePage(countryCode), value = true).get

      val userAnswersSba = userAnswers
        .set(ForeignClaimStructureBuildingAllowancePage(countryCode),false)
        .toOption

      val context =
        JourneyContext(
          taxYear = taxYear,
          mtditid = "mtditid",
          nino = "nino",
          journeyPath = ForeignStructureBuildingAllowance
        )

      when(
        propertySubmissionService
          .saveForeignPropertyJourneyAnswers(ArgumentMatchers.eq(context), any)(
            any(),
            any()
          )
      ) thenReturn Future(Right())

      when(businessService.getForeignPropertyDetails(any(), any())(any())) thenReturn Future(
        Right(Some(foreignPropertyDetails))
      )

      val application = applicationBuilder(userAnswers = userAnswersSba, isAgent = true)
        .overrides(bind[PropertySubmissionService].toInstance(propertySubmissionService))
        .overrides(bind[BusinessService].toInstance(businessService))
        .overrides(bind[AuditService].toInstance(audit))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, ForeignClaimSbaCheckYourAnswersController.onSubmit(taxYear, countryCode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(audit, times(1)).sendAuditEvent(any())(any(), any())
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }
}
