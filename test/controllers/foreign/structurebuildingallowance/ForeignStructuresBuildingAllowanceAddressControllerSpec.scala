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

package controllers.foreign.structurebuildingallowance

import base.SpecBase
import controllers.foreign.structuresbuildingallowance.routes.ForeignStructuresBuildingAllowanceAddressController
import controllers.routes
import forms.foreign.structurebuildingallowance.ForeignStructuresBuildingAllowanceAddressFormProvider
import models.{ForeignStructuresBuildingAllowanceAddress, NormalMode, UserAnswers}
import navigation.{FakeForeignPropertyNavigator, ForeignPropertyNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.structurebuildingallowance.ForeignStructuresBuildingAllowanceAddressPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.foreign.structurebuildingallowance.ForeignStructuresBuildingAllowanceAddressView

import scala.concurrent.Future

class ForeignStructuresBuildingAllowanceAddressControllerSpec extends SpecBase with MockitoSugar {

  val taxYear: Int = 2024
  val countryCode: String = "AUS"
  val index: Int = 0
  val validAnswer: String = "Building"
  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ForeignStructuresBuildingAllowanceAddressFormProvider

  private def form: Form[ForeignStructuresBuildingAllowanceAddress] = formProvider(emptyUserAnswers, countryCode, index)
  lazy val foreignStructuresBuildingAllowanceAddressRoute: String =
    ForeignStructuresBuildingAllowanceAddressController.onPageLoad(taxYear, index, countryCode, NormalMode).url

  "ForeignStructuresBuildingAllowanceAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignStructuresBuildingAllowanceAddressRoute)

        val view = application.injector.instanceOf[ForeignStructuresBuildingAllowanceAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, index, countryCode, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = UserAnswers(userAnswersId)
        .set(
          ForeignStructuresBuildingAllowanceAddressPage(index, countryCode),
          ForeignStructuresBuildingAllowanceAddress("building-name", "building-number", "post-code")
        )
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, foreignStructuresBuildingAllowanceAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignStructuresBuildingAllowanceAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(ForeignStructuresBuildingAllowanceAddress("building-name", "building-number", "post-code")),
          taxYear,
          index,
          countryCode,
          NormalMode
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false)
          .overrides(
            bind[ForeignPropertyNavigator].toInstance(new FakeForeignPropertyNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, foreignStructuresBuildingAllowanceAddressRoute)
            .withFormUrlEncodedBody(
              ("buildingName", validAnswer),
              ("buildingNumber", validAnswer),
              ("postcode", validAnswer)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignStructuresBuildingAllowanceAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[ForeignStructuresBuildingAllowanceAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, index, countryCode, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request = FakeRequest(GET, foreignStructuresBuildingAllowanceAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, true).build()

      running(application) {
        val request =
          FakeRequest(POST, foreignStructuresBuildingAllowanceAddressRoute)
            .withFormUrlEncodedBody(("Building name", "value 1"), ("Building number", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
