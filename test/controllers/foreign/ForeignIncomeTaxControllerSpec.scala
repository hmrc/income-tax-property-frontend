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

package controllers.foreign

import base.SpecBase
import forms.foreign.ForeignIncomeTaxFormProvider
import models.{ForeignIncomeTax, NormalMode, UserAnswers}
import navigation.{FakeForeignPropertyNavigator, ForeignPropertyNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.prop.TableFor1
import org.scalatest.prop.Tables.Table
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.foreign.{Country, ForeignIncomeTaxPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.foreign.ForeignIncomeTaxView

import java.time.LocalDate
import scala.concurrent.Future

class ForeignIncomeTaxControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val scenarios: TableFor1[String] = Table[String]("individualOrAgent", "individual", "agent")
  val taxYear: Int = LocalDate.now().getYear
  val formProvider = new ForeignIncomeTaxFormProvider()
  val country: Country = Country("United States of America", "USA")
  val foreignIncomeTaxAnswers: ForeignIncomeTax = ForeignIncomeTax(isForeignIncomeTax = true, foreignTaxPaidOrDeducted = Some(123.45))
  lazy val foreignIncomeTaxRoute: String = routes.ForeignIncomeTaxController.onPageLoad(taxYear, country.code, NormalMode).url

  forAll(scenarios) { (individualOrAgent: String) =>
    val form = formProvider(individualOrAgent)
    val isAgent: Boolean = individualOrAgent == "agent"


    s"ForeignIncomeTax Controller for an $individualOrAgent" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent).build()

        running(application) {
          val request = FakeRequest(GET, foreignIncomeTaxRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ForeignIncomeTaxView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, taxYear, individualOrAgent, country, NormalMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = UserAnswers(userAnswersId).set(ForeignIncomeTaxPage(country.code), foreignIncomeTaxAnswers).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent).build()

        running(application) {
          val request = FakeRequest(GET, foreignIncomeTaxRoute)

          val view = application.injector.instanceOf[ForeignIncomeTaxView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(foreignIncomeTaxAnswers),
            taxYear, individualOrAgent, country, NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent)
            .overrides(
              bind[ForeignPropertyNavigator].toInstance(new FakeForeignPropertyNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, foreignIncomeTaxRoute)
              .withFormUrlEncodedBody(
                ("isForeignIncomeTax", "true"),
                ("foreignTaxPaidOrDeducted", "123.45")
              )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent).build()

        running(application) {
          val request =
            FakeRequest(POST, foreignIncomeTaxRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[ForeignIncomeTaxView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, taxYear, individualOrAgent, country, NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgent).build()

        running(application) {
          val request = FakeRequest(GET, foreignIncomeTaxRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None, isAgent).build()

        running(application) {
          val request =
            FakeRequest(POST, foreignIncomeTaxRoute)
              .withFormUrlEncodedBody(("isForeignIncomeTax", "true"),
                ("foreignTaxPaidOrDeducted", "123.45"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
