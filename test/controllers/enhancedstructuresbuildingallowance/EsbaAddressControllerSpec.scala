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

package controllers.enhancedstructuresbuildingallowance

import base.SpecBase
import forms.enhancedstructuresbuildingallowance.EsbaAddressFormProvider
import models.{EsbaAddress, NormalMode, Rentals, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import navigation.{FakeNavigator, Navigator}
import org.scalatestplus.mockito.MockitoSugar
import pages.enhancedstructuresbuildingallowance.EsbaAddressPage
import play.api.data.Form
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.enhancedstructuresbuildingallowance.EsbaAddressView

import scala.concurrent.Future

class EsbaAddressControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new EsbaAddressFormProvider
  private def form: Form[EsbaAddress] = formProvider(emptyUserAnswers, Rentals, 0)

  val taxYear = 2024
  val index = 0
  val validAnswer = "Building"
  val validPostCode = "GV92 8VB"
  def onwardRoute: Call = Call("GET", "/foo")
  private val isAgentMessageKey = "individual"

  lazy val enhancedStructureBuildingAllowanceAddressDateRoute: String =
    routes.EsbaAddressController.onPageLoad(taxYear, NormalMode, index, Rentals).url

  override val emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  def getRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, enhancedStructureBuildingAllowanceAddressDateRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, enhancedStructureBuildingAllowanceAddressDateRoute)
      .withFormUrlEncodedBody(
        "buildingName"   -> "building-name",
        "buildingNumber" -> "building-number",
        "postcode"       -> "postcode"
      )

  "EsbaAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      running(application) {
        val result = route(application, getRequest).value

        val view = application.injector.instanceOf[EsbaAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, NormalMode, index, Rentals)(
          getRequest,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(EsbaAddressPage(index, Rentals), EsbaAddress("building-name", "building-number", "post-code"))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false).build()

      running(application) {
        val view = application.injector.instanceOf[EsbaAddressView]

        val result = route(application, getRequest).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(EsbaAddress("building-name", "building-number", "post-code")),
          taxYear,
          NormalMode,
          index,
          Rentals
        )(getRequest, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), false)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, enhancedStructureBuildingAllowanceAddressDateRoute)
            .withFormUrlEncodedBody(
              ("buildingName", validAnswer),
              ("buildingNumber", validAnswer),
              ("postcode", validPostCode)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

      val request =
        FakeRequest(POST, enhancedStructureBuildingAllowanceAddressDateRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[EsbaAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxYear, NormalMode, index, Rentals)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val result = route(application, getRequest).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
