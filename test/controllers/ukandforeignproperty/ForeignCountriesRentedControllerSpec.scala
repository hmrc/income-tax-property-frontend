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

package controllers.ukandforeignproperty

import base.SpecBase
import forms.ukandforeignproperty.ForeignCountriesRentedFormProvider
import models.{Index, NormalMode, UkAndForeignPropertyRentalTypeUk, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.UkAndForeignPropertyRentalTypeUkPage
import pages.foreign.Country
import pages.ukandforeignproperty.SelectCountryPage
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.all.SummaryListViewModel
import views.html.ukandforeignproperty.ForeignCountriesRentedView

import scala.concurrent.Future

class ForeignCountriesRentedControllerSpec extends SpecBase with MockitoSugar {

  val formProvider: ForeignCountriesRentedFormProvider = new ForeignCountriesRentedFormProvider()
  val form: Form[Boolean] = formProvider()

  val taxYear: Int = 2024
  lazy val foreignCountriesRentedRoute: String =
    controllers.ukandforeignproperty.routes.ForeignCountriesRentedController.onPageLoad(taxYear, mode = NormalMode).url

  lazy val selectCountryRoute: Int => String =
    index => routes.SelectCountryController.onPageLoad(taxYear, Index(index), NormalMode).url

  val list: SummaryList = SummaryListViewModel(Seq.empty)
  val isAgentMessageKey: String = "individual"
  val index: Int = 0
  val testCountry: Country = Country("Greece", "GRC")
  val _testUserAnswersWith1Country: UserAnswers = emptyUserAnswers.set(SelectCountryPage, List(testCountry)).success.value
  val testUserAnswersWithRentARoom = _testUserAnswersWith1Country.set(UkAndForeignPropertyRentalTypeUkPage,Set[UkAndForeignPropertyRentalTypeUk](UkAndForeignPropertyRentalTypeUk.RentARoom)).toOption
  val testUserAnswersPropertyRentals = _testUserAnswersWith1Country.set(UkAndForeignPropertyRentalTypeUkPage,Set[UkAndForeignPropertyRentalTypeUk](UkAndForeignPropertyRentalTypeUk.PropertyRentals)).toOption

  def onwardRoute: Call =
    Call("GET", "/")

  def addCountryRoute(): Call =
    Call("GET", "/property/uk-foreign-property/countries")

  "ForeignCountriesRented Controller" - {

    "GET" - {

      "must return OK and the list should display the country" in {
        val application: Application = applicationBuilder(userAnswers = testUserAnswersWithRentARoom, isAgent = false).build()

        running(application) {
          val controller = application.injector.instanceOf[ForeignCountriesRentedController]
          val result = controller.onPageLoad(taxYear, NormalMode)(FakeRequest())

          val doc = Jsoup.parse(contentAsString(result))

          doc.select("main .govuk-summary-list__key").get(0).text() mustEqual "Greece"
        }
      }

      "must redirect to the Select Country page when the user hasn't selected any countries" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()

        running(application) {
          val request = FakeRequest(GET, foreignCountriesRentedRoute)
          val controller = application.injector.instanceOf[ForeignCountriesRentedController]
          val view = application.injector.instanceOf[ForeignCountriesRentedView]

          val result = controller.onPageLoad(taxYear, NormalMode)(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) must contain(routes.SelectCountryController.onPageLoad(taxYear, Index(1), NormalMode).url)
        }
      }

    }

    "POST" - {
      "must redirect to next page when 'yes' is submitted" in {
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = testUserAnswersWithRentARoom, isAgent = true)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(addCountryRoute())),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        val request = FakeRequest(POST, selectCountryRoute(1))
          .withFormUrlEncodedBody(("addAnother", "true"))

        running(application) {
          val controller = application.injector.instanceOf[ForeignCountriesRentedController]

          val result = controller.onSubmit(taxYear, NormalMode)(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual selectCountryRoute(2)
        }
      }

      "must redirect to the next page when 'no' is selected With RentARoom" in {
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = testUserAnswersWithRentARoom, isAgent = true)
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        val request =
          FakeRequest(POST, foreignCountriesRentedRoute)
            .withFormUrlEncodedBody(("addAnother", "false"))

        running(application) {
          val controller = application.injector.instanceOf[ForeignCountriesRentedController]

          val result = controller.onSubmit(taxYear, NormalMode)(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.UkAndForeignPropertyClaimExpensesOrReliefController.onPageLoad(taxYear, NormalMode).url
        }
      }

      "must redirect to the next page when 'no' is selected With PropertyRentals" in {
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = testUserAnswersPropertyRentals, isAgent = true)
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        val request =
          FakeRequest(POST, foreignCountriesRentedRoute)
            .withFormUrlEncodedBody(("addAnother", "false"))

        running(application) {
          val controller = application.injector.instanceOf[ForeignCountriesRentedController]

          val result = controller.onSubmit(taxYear, NormalMode)(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesController.onPageLoad(taxYear, NormalMode).url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {
        val application = applicationBuilder(userAnswers = None, isAgent = true).build()

        val request =
          FakeRequest(POST, foreignCountriesRentedRoute)
            .withFormUrlEncodedBody(("value", "true"))

        running(application) {
          val controller = application.injector.instanceOf[ForeignCountriesRentedController]

          val result = controller.onSubmit(taxYear, NormalMode)(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

  }
}
