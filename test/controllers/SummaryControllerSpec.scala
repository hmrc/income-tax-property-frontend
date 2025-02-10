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

package controllers

import base.SpecBase
import connectors.error.{ApiError, SingleErrorBody}
import controllers.session.SessionRecovery
import models.IncomeSourcePropertyType.{ForeignProperty, UKProperty}
import models.backend.{ForeignPropertyDetailsError, PropertyDetails}
import models.requests.OptionalDataRequest
import models.{UKPropertySelect, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.{Country, ForeignPropertySummaryPage}
import pages.ukandforeignproperty.UkAndForeignPropertySummaryPage
import pages.{UKPropertyPage, UKPropertySummaryPage}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.inject.bind
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.{BusinessService, PropertySubmissionService}
import testHelpers.Fixture
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.summary.{TaskListItem, TaskListTag}
import views.html.SummaryView

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class SummaryControllerSpec extends SpecBase with MockitoSugar with Fixture {

  private val taxYear = LocalDate.now.getYear
  val propertyPeriodSubmissionService: PropertySubmissionService = mock[PropertySubmissionService]
  private val countries = List.empty[Country]
  val userAnswers: Option[UserAnswers] = Some(UserAnswers("Test"))
  val fakeSessionRecovery: SessionRecovery = new SessionRecovery {
    override def withUpdatedData(taxYear: Int)(
      block: OptionalDataRequest[AnyContent] => Future[Result]
    )(implicit request: OptionalDataRequest[AnyContent], ec: ExecutionContext, hc: HeaderCarrier): Future[Result] =
      block(request)
  }
  val propertyDetails: Seq[PropertyDetails] = Seq(
    PropertyDetails(Some(UKProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(false), "incomeSourceId"),
    PropertyDetails(Some(ForeignProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(false), "incomeSourceId2")
  )

  when(
    propertyPeriodSubmissionService.getUKPropertySubmission(any(), any())(any())
  ) thenReturn Future.successful(Right(fetchedPropertyData))

  when(
    propertyPeriodSubmissionService.getForeignPropertySubmission(any(), any())(any())
  ) thenReturn Future.successful(Left(ForeignPropertyDetailsError("AA000000A", "1234567890")))

  def propertyAboutItems: Seq[TaskListItem] =
    Seq(
      TaskListItem(
        "summary.about",
        controllers.about.routes.UKPropertyDetailsController.onPageLoad(taxYear),
        TaskListTag.NotStarted,
        "property_about_link"
      )
    )

  def foreignStartItems: Seq[TaskListItem] = Seq(
    TaskListItem(
      "foreign.selectCountry",
      controllers.foreign.routes.ForeignPropertyDetailsController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "foreign_property_select_country"
    )
  )

  def foreignPropertyItems: Map[String, Seq[TaskListItem]] = Map.empty

  def ukAndForeignPropertyItems: Seq[TaskListItem] = Seq(
    TaskListItem(
      "summary.aboutUKAndForeignProperties",
      controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "uk_and_foreign_property_about_link"
    )
  )

  "Summary Controller" - {

    "must return OK and the correct view for a GET" in {

      val year = LocalDate.now().getYear
      val businessService = mock[BusinessService]

      when(businessService.getAllPropertyDetails(any(), any())(any())) thenReturn Future.successful(
        Right(propertyDetails)
      )

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
          UKPropertySummaryPage(
            taxYear,
            propertyAboutItems,
            Seq.empty[TaskListItem],
            Seq.empty[TaskListItem],
            Seq.empty[TaskListItem]
          ),
          ForeignPropertySummaryPage(taxYear, foreignStartItems, foreignPropertyItems, countries, userAnswers),
          UkAndForeignPropertySummaryPage(taxYear = taxYear, startItems = ukAndForeignPropertyItems)
        )(request, messages(application)).toString
      }
    }

    "must fail with PropertyDataError when encountering an issue retrieving property data" in {

      val year = LocalDate.now().getYear
      val businessService = mock[BusinessService]

      when(businessService.getAllPropertyDetails(any(), any())(any())) thenReturn
        Future.successful(Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError))
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
        .overrides(bind[BusinessService].toInstance(businessService))
        .overrides(bind[PropertySubmissionService].toInstance(propertyPeriodSubmissionService))
        .build()

      running(application) {
        val failure = intercept[Exception] {
          val request = FakeRequest(GET, routes.SummaryController.show(year).url)
          status(route(application, request).value)
        }
        failure.getMessage mustBe "Encountered an issue retrieving property data from the business API"
      }
    }

    "must fail with PropertyDataError when no property data is found" in {

      val year = LocalDate.now().getYear
      val businessService = mock[BusinessService]

      when(businessService.getAllPropertyDetails(any(), any())(any())) thenReturn Future.successful(
        Right(Seq.empty)
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
        .overrides(bind[BusinessService].toInstance(businessService))
        .overrides(bind[PropertySubmissionService].toInstance(propertyPeriodSubmissionService))
        .build()

      running(application) {
        val failure = intercept[Exception] {
          val request = FakeRequest(GET, routes.SummaryController.show(year).url)
          status(route(application, request).value)
        }
        failure.getMessage mustBe "Encountered an issue retrieving property data from the business API"
      }
    }




    "must display the property rentals section if property rentals is selected in the about section" in {
      val year = LocalDate.now().getYear
      val businessService = mock[BusinessService]
      val propertyRentalsItems: Seq[TaskListItem] = Seq(
        TaskListItem(
          "summary.about",
          controllers.propertyrentals.routes.PropertyRentalsStartController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "rentals_about_link"
        )
      )

      val userAnswersWithPropertyRentals = emptyUserAnswers
        .set(
          UKPropertyPage,
          Set[UKPropertySelect](UKPropertySelect.PropertyRentals)
        )
        .success
        .value

      when(businessService.getAllPropertyDetails(any(), any())(any())) thenReturn Future.successful(
        Right(propertyDetails)
      )

      val application = applicationBuilder(userAnswers = Some(userAnswersWithPropertyRentals), isAgent = false)
        .overrides(bind[BusinessService].toInstance(businessService))
        .overrides(bind[SessionRecovery].toInstance(fakeSessionRecovery))
        .overrides(bind[PropertySubmissionService].toInstance(propertyPeriodSubmissionService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.SummaryController.show(year).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SummaryView]

        status(result) mustEqual OK
        contentAsString(result) must include("UK property rentals")
        contentAsString(result) mustEqual view(
          UKPropertySummaryPage(
            taxYear,
            propertyAboutItems,
            propertyRentalsItems,
            Seq.empty[TaskListItem],
            Seq.empty[TaskListItem]
          ),
          ForeignPropertySummaryPage(taxYear, foreignStartItems, foreignPropertyItems, countries, userAnswers),
          UkAndForeignPropertySummaryPage(taxYear = taxYear, startItems = ukAndForeignPropertyItems)
        )(request, messages(application)).toString
      }
    }

    "must NOT display the property rentals section if property rentals is not selected in the about section" in {
      val year = LocalDate.now().getYear

      val businessService = mock[BusinessService]
      val userAnswersWithoutPropertyRentals = emptyUserAnswers
        .set(
          UKPropertyPage,
          Set[UKPropertySelect]()
        )
        .success
        .value
      when(businessService.getAllPropertyDetails(any(), any())(any())) thenReturn Future.successful(
        Right(propertyDetails)
      )

      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutPropertyRentals), isAgent = false)
        .overrides(bind[BusinessService].toInstance(businessService))
        .overrides(bind[PropertySubmissionService].toInstance(propertyPeriodSubmissionService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.SummaryController.show(year).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SummaryView]

        status(result) mustEqual OK
        contentAsString(result) mustNot include("UK property rentals")
        contentAsString(result) mustEqual view(
          UKPropertySummaryPage(
            taxYear,
            propertyAboutItems,
            Seq.empty[TaskListItem],
            Seq.empty[TaskListItem],
            Seq.empty[TaskListItem]
          ),
          ForeignPropertySummaryPage(taxYear, foreignStartItems, foreignPropertyItems, countries, userAnswers),
          UkAndForeignPropertySummaryPage(taxYear = taxYear, startItems = ukAndForeignPropertyItems)
        )(request, messages(application)).toString
      }
    }

    "must display the UK rent a room section if rent a room is selected in the about section" in {
      val year = LocalDate.now().getYear
      val businessService = mock[BusinessService]

      when(businessService.getAllPropertyDetails(any(), any())(any())) thenReturn Future.successful(
        Right(propertyDetails)
      )

      val ukRentARoomItems: Seq[TaskListItem] = Seq(
        TaskListItem(
          "summary.about",
          controllers.ukrentaroom.routes.RentARoomStartController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "rent_a_room_about_link"
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
        .overrides(bind[SessionRecovery].toInstance(fakeSessionRecovery))
        .overrides(bind[PropertySubmissionService].toInstance(propertyPeriodSubmissionService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.SummaryController.show(year).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SummaryView]

        status(result) mustEqual OK
        contentAsString(result) must include("UK rent a room")
        contentAsString(result) mustEqual view(
          UKPropertySummaryPage(
            taxYear,
            propertyAboutItems,
            Seq.empty[TaskListItem],
            ukRentARoomItems,
            Seq.empty[TaskListItem]
          ),
          ForeignPropertySummaryPage(taxYear, foreignStartItems, foreignPropertyItems, countries, userAnswers),
          UkAndForeignPropertySummaryPage(taxYear = taxYear, startItems = ukAndForeignPropertyItems)
        )(request, messages(application)).toString
      }
    }

    "must display the UK combined section if rent a room  & rentals are selected in the about section" in {
      val year = LocalDate.now().getYear
      val businessService = mock[BusinessService]

      when(businessService.getAllPropertyDetails(any(), any())(any())) thenReturn Future.successful(
        Right(propertyDetails)
      )

      val combinedItems: Seq[TaskListItem] = Seq(
        TaskListItem(
          "summary.about",
          controllers.rentalsandrentaroom.routes.RentalsRentARoomStartController.onPageLoad(taxYear),
          TaskListTag.NotStarted,
          "rentals_and_rent_a_room_about_link"
        )
      )

      val userAnswersWithUkRentARoom = emptyUserAnswers
        .set(
          UKPropertyPage,
          Set[UKPropertySelect](UKPropertySelect.RentARoom, UKPropertySelect.PropertyRentals)
        )
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithUkRentARoom), isAgent = false)
        .overrides(bind[BusinessService].toInstance(businessService))
        .overrides(bind[SessionRecovery].toInstance(fakeSessionRecovery))
        .overrides(bind[PropertySubmissionService].toInstance(propertyPeriodSubmissionService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.SummaryController.show(year).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SummaryView]

        status(result) mustEqual OK
        contentAsString(result) must include("UK property rentals and rent a room")
        contentAsString(result) mustEqual view(
          UKPropertySummaryPage(
            taxYear,
            propertyAboutItems,
            Seq.empty[TaskListItem],
            Seq.empty[TaskListItem],
            combinedItems
          ),
          ForeignPropertySummaryPage(taxYear, foreignStartItems, foreignPropertyItems, countries, userAnswers),
          UkAndForeignPropertySummaryPage(taxYear = taxYear, startItems = ukAndForeignPropertyItems)
        )(request, messages(application)).toString
      }
    }

  }
}

