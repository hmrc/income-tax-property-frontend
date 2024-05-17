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

package controllers

import base.SpecBase
import models.backend.{BusinessDetails, PropertyDetails}
import models.{FetchedBackendData, NormalMode, UKPropertySelect}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import pages.UKPropertyPage
import pages.ukrentaroom.UkRentARoomJointlyLetPage
import play.api.inject.bind
import play.api.libs.json.JsObject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.{BusinessService, PropertySubmissionService}
import viewmodels.summary.{TaskListItem, TaskListTag}
import views.html.SummaryView

import java.time.LocalDate
import scala.concurrent.Future

class SummaryControllerSpec extends SpecBase with MockitoSugar {

  private val taxYear = LocalDate.now.getYear
  val propertyPeriodSubmissionService = mock[PropertySubmissionService]

  when(
    propertyPeriodSubmissionService.getPropertyPeriodicSubmission(any(), any())(any())
  ) thenReturn Future.successful(Right(FetchedBackendData(new JsObject(Map()))))

  "Summary Controller" - {

    "must return OK and the correct view for a GET" in {

      val year = LocalDate.now().getYear
      val propertyDetails =
        PropertyDetails(Some("uk-property"), Some(LocalDate.now), cashOrAccruals = Some(false), "incomeSourceId")
      val businessDetails = BusinessDetails(List(propertyDetails))
      val businessService = mock[BusinessService]

      when(businessService.getBusinessDetails(any())(any())) thenReturn Future.successful(Right(businessDetails))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
        .overrides(bind[BusinessService].toInstance(businessService))
        .overrides(bind[PropertySubmissionService].toInstance(propertyPeriodSubmissionService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.SummaryController.show(year).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SummaryView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          taxYear,
          Seq.empty[TaskListItem],
          Seq.empty[TaskListItem],
          Seq.empty[TaskListItem]
        )(request, messages(application)).toString
      }
    }

    "must display the property rentals section if property rentals is selected in the about section" in {
      val year = LocalDate.now().getYear
      val propertyDetails =
        PropertyDetails(Some("uk-property"), Some(LocalDate.now), cashOrAccruals = Some(false), "incomeSourceId")
      val businessDetails = BusinessDetails(List(propertyDetails))
      val businessService = mock[BusinessService]
      val propertyRentalsItems: Seq[TaskListItem] = Seq(
        TaskListItem(
          "summary.about",
          controllers.propertyrentals.routes.PropertyRentalsStartController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "about_link"
        )
      )
      val userAnswersWithPropertyRentals = emptyUserAnswers
        .set(
          UKPropertyPage,
          Set[UKPropertySelect](UKPropertySelect.PropertyRentals)
        )
        .success
        .value
      when(businessService.getBusinessDetails(any())(any())) thenReturn Future.successful(Right(businessDetails))

      val application = applicationBuilder(userAnswers = Some(userAnswersWithPropertyRentals), isAgent = false)
        .overrides(bind[BusinessService].toInstance(businessService))
        .overrides(bind[PropertySubmissionService].toInstance(propertyPeriodSubmissionService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.SummaryController.show(year).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SummaryView]

        status(result) mustEqual OK
        contentAsString(result) must include("Property Rentals")
        contentAsString(result) mustEqual view(
          taxYear,
          propertyRentalsItems,
          Seq.empty[TaskListItem],
          Seq.empty[TaskListItem]
        )(request, messages(application)).toString
      }
    }

    "must NOT display the property rentals section if property rentals is not selected in the about section" in {
      val year = LocalDate.now().getYear
      val propertyDetails =
        PropertyDetails(Some("uk-property"), Some(LocalDate.now), cashOrAccruals = Some(false), "incomeSourceId")
      val businessDetails = BusinessDetails(List(propertyDetails))
      val businessService = mock[BusinessService]
      val userAnswersWithoutPropertyRentals = emptyUserAnswers
        .set(
          UKPropertyPage,
          Set[UKPropertySelect]()
        )
        .success
        .value
      when(businessService.getBusinessDetails(any())(any())) thenReturn Future.successful(Right(businessDetails))

      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutPropertyRentals), isAgent = false)
        .overrides(bind[BusinessService].toInstance(businessService))
        .overrides(bind[PropertySubmissionService].toInstance(propertyPeriodSubmissionService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.SummaryController.show(year).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SummaryView]

        status(result) mustEqual OK
        contentAsString(result) mustNot include("Property Rentals")
        contentAsString(result) mustEqual view(
          taxYear,
          Seq.empty[TaskListItem],
          Seq.empty[TaskListItem],
          Seq.empty[TaskListItem]
        )(request, messages(application)).toString
      }
    }

    "must display the FHL section if fhl is selected in the about section" in {
      val year = LocalDate.now().getYear
      val propertyDetails =
        PropertyDetails(Some("uk-property"), Some(LocalDate.now), cashOrAccruals = Some(false), "incomeSourceId")
      val businessDetails = BusinessDetails(List(propertyDetails))
      val businessService = mock[BusinessService]
      val propertyFhlItems: Seq[TaskListItem] = Seq(
        TaskListItem(
          "summary.about",
          controllers.furnishedholidaylettings.routes.FhlIntroController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "about_link"
        )
      )
      val userAnswersWithFhl = emptyUserAnswers
        .set(
          UKPropertyPage,
          Set[UKPropertySelect](UKPropertySelect.FurnishedHolidayLettings)
        )
        .success
        .value
      when(businessService.getBusinessDetails(any())(any())) thenReturn Future.successful(Right(businessDetails))

      val application = applicationBuilder(userAnswers = Some(userAnswersWithFhl), isAgent = false)
        .overrides(bind[BusinessService].toInstance(businessService))
        .overrides(bind[PropertySubmissionService].toInstance(propertyPeriodSubmissionService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.SummaryController.show(year).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SummaryView]

        status(result) mustEqual OK
        contentAsString(result) must include("UK furnished holiday lettings")
        contentAsString(result) mustEqual view(
          taxYear,
          Seq.empty[TaskListItem],
          propertyFhlItems,
          Seq.empty[TaskListItem]
        )(request, messages(application)).toString
      }
    }

    "must display the UK rent a room section if rent a room is selected in the about section" in {
      val year = LocalDate.now().getYear
      val propertyDetails =
        PropertyDetails(Some("uk-property"), Some(LocalDate.now), cashOrAccruals = Some(false), "incomeSourceId")
      val businessDetails = BusinessDetails(List(propertyDetails))
      val businessService = mock[BusinessService]

      when(businessService.getBusinessDetails(any())(any())) thenReturn Future.successful(Right(businessDetails))

      val ukRentARoomItems: Seq[TaskListItem] = Seq(
        TaskListItem(
          "summary.about",
          controllers.ukrentaroom.routes.UkRentARoomJointlyLetController.onPageLoad(taxYear, NormalMode),
          TaskListTag.NotStarted,
          "about_link"
        ),
        TaskListItem(
          "summary.expenses",
          controllers.ukrentaroom.routes.UkRentARoomExpensesIntroController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "expenses_link"
        )
      )

      val userAnswersWithUkRentARoom = emptyUserAnswers
        .set(
          UKPropertyPage,
          Set[UKPropertySelect](UKPropertySelect.RentARoom)
        )
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithUkRentARoom), isAgent = false)
        .overrides(bind[BusinessService].toInstance(businessService))
        .overrides(bind[PropertySubmissionService].toInstance(propertyPeriodSubmissionService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.SummaryController.show(year).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SummaryView]

        status(result) mustEqual OK
        contentAsString(result) must include("UK rent a room")
        contentAsString(result) mustEqual view(
          taxYear,
          Seq.empty[TaskListItem],
          Seq.empty[TaskListItem],
          ukRentARoomItems
        )(request, messages(application)).toString
      }
    }
  }
}
