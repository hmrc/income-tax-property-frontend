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
import models.UKPropertySelect
import pages.UKPropertyPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.summary.{TaskListItem, TaskListTag}
import views.html.SummaryView

import java.time.LocalDate

class SummaryControllerSpec extends SpecBase {

  private val taxYear = LocalDate.now.getYear

  "Summary Controller" - {

    "must return OK and the correct view for a GET" in {

      val year = LocalDate.now().getYear
      val application = applicationBuilder(userAnswers = None, isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, routes.SummaryController.show(year).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SummaryView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(taxYear, Seq.empty[TaskListItem])(request, messages(application)).toString
      }
    }

    "must display the property rentals section if property rentals is selected in the about section" in {
      val year = LocalDate.now().getYear
      val propertyRentalsItems: Seq[TaskListItem] = Seq(TaskListItem(
        "summary.about",
        controllers.propertyrentals.routes.PropertyRentalsStartController.onPageLoad(taxYear),
        TaskListTag.NotStarted,
        "about_link"
      ))
      val userAnswersWithPropertyRentals = emptyUserAnswers.set(
        UKPropertyPage,
        Set[UKPropertySelect](UKPropertySelect.PropertyRentals)
      ).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswersWithPropertyRentals), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, routes.SummaryController.show(year).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SummaryView]

        status(result) mustEqual OK
        contentAsString(result) must  include("Property Rentals")
        contentAsString(result) mustEqual view(taxYear, propertyRentalsItems)(request, messages(application)).toString
      }
    }

    "must NOT display the property rentals section if property rentals is not selected in the about section" in {
      val year = LocalDate.now().getYear
      val userAnswersWithoutPropertyRentals = emptyUserAnswers.set(
        UKPropertyPage,
        Set[UKPropertySelect](UKPropertySelect.RentARoom)
      ).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutPropertyRentals), isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, routes.SummaryController.show(year).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SummaryView]

        status(result) mustEqual OK
        contentAsString(result) mustNot  include("Property Rentals")
        contentAsString(result) mustEqual view(taxYear, Seq.empty[TaskListItem])(request, messages(application)).toString
      }
    }
  }
}
