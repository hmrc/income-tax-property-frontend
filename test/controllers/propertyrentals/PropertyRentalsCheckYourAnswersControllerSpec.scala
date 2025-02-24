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

package controllers.propertyrentals

import audit.RentalsAbout
import base.SpecBase
import models.JourneyPath.PropertyRentalAbout
import models.{JourneyContext, Rentals, UserAnswers}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.propertyrentals.ClaimPropertyIncomeAllowancePage
import play.api.inject
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.PropertySubmissionService
import viewmodels.govuk.SummaryListFluency
import views.html.propertyrentals.CheckYourAnswersView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PropertyRentalsCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency {

  val taxYear: Int = 2024
  def onwardRoute: Call =
    Call("GET", "/update-and-submit-income-tax-return/property/2024/rentals/about/complete-yes-no")

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, routes.PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, taxYear)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, routes.PropertyRentalsCheckYourAnswersController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the correct view for a POST (onSubmit)" in {
      val userAnswers =
        UserAnswers("test").set(ClaimPropertyIncomeAllowancePage(Rentals), true).get

      val context =
        JourneyContext(taxYear = taxYear, mtditid = "mtditid", nino = "nino", journeyPath = PropertyRentalAbout)
      val propertyRentalsAbout =
        RentalsAbout(claimPropertyIncomeAllowanceYesOrNo = true)

      when(
        propertySubmissionService
          .saveJourneyAnswers(ArgumentMatchers.eq(context), ArgumentMatchers.eq(propertyRentalsAbout))(
            any(),
            any()
          )
      ) thenReturn Future(Right())

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false)
        .overrides(inject.bind[PropertySubmissionService].toInstance(propertySubmissionService))
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.PropertyRentalsCheckYourAnswersController.onSubmit(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }
}
