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

package controllers.about

import base.SpecBase
import connectors.error.{ApiError, SingleErrorBody}
import controllers.exceptions.InternalErrorFailure
import models.IncomeSourcePropertyType.UKProperty
import models.authorisation.Enrolment.Nino
import models.authorisation.SessionValues
import models.backend.PropertyDetails
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.BusinessService
import testHelpers.FakeAuthConnector
import testHelpers.Retrievals.Ops
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual}
import uk.gov.hmrc.auth.core._
import viewmodels.PropertyDetailsPage
import views.html.about.UKPropertyDetailsView

import java.time.LocalDate
import scala.concurrent.Future

class UKPropertyDetailsControllerSpec extends SpecBase with MockitoSugar {

  private val taxYear = LocalDate.now.getYear

  private val enrolments = Enrolments(
    Set(
      Enrolment(Nino.key, Seq(EnrolmentIdentifier(Nino.value, "nino")), "Activated"),
      Enrolment(
        models.authorisation.Enrolment.Individual.key,
        Seq(EnrolmentIdentifier(models.authorisation.Enrolment.Individual.value, "individual")),
        "Activated"
      )
    )
  )

  "UKPropertyDetails Controller" - {

    "must return OK and the correct view for an Individual" in {
      val authConnector =
        new FakeAuthConnector(Some(Individual) ~ Some("internalId") ~ ConfidenceLevel.L250 ~ enrolments)
      val businessService = mock[BusinessService]
      val propertyDetails =
        PropertyDetails(Some(UKProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(false), "incomeSourceId")

      when(
        businessService.getUkPropertyDetails(
          org.mockito.ArgumentMatchers.eq("nino"),
          ArgumentMatchers.eq("mtditid")
        )(any())
      ) thenReturn Future.successful(Right(Some(propertyDetails)))

      val application =
        applicationBuilder(None, isAgent = false)
          .overrides(bind[AuthConnector].toInstance(authConnector))
          .overrides(bind[BusinessService].toInstance(businessService))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.UKPropertyDetailsController.onPageLoad(taxYear).url)
        val result = route(application, request).value

        val view = application.injector.instanceOf[UKPropertyDetailsView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          PropertyDetailsPage(
            taxYear,
            "individual",
            propertyDetails.tradingStartDate.get,
            propertyDetails.accrualsOrCash.get
          )
        )(request, messages(application)).toString

      }
    }

    "must return OK and the correct view for an Agent" in {
      val authConnector = new FakeAuthConnector(Some(Agent) ~ Some("internalId") ~ ConfidenceLevel.L250 ~ enrolments)
      val businessService = mock[BusinessService]
      val propertyDetails =
        PropertyDetails(Some(UKProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(false), "incomeSourceId")

      when(
        businessService.getUkPropertyDetails(
          org.mockito.ArgumentMatchers.eq("nino"),
          ArgumentMatchers.eq("mtditid")
        )(any())
      ) thenReturn Future.successful(Right(Some(propertyDetails)))

      val application =
        applicationBuilder(None, isAgent = true)
          .overrides(bind[AuthConnector].toInstance(authConnector))
          .overrides(bind[BusinessService].toInstance(businessService))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.UKPropertyDetailsController.onPageLoad(taxYear).url)
          .withSession(SessionValues.ClientMtdid -> "mtditid", SessionValues.ClientNino -> "nino")
        val result = route(application, request).value

        val view = application.injector.instanceOf[UKPropertyDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          PropertyDetailsPage(
            taxYear,
            "agent",
            propertyDetails.tradingStartDate.get,
            propertyDetails.accrualsOrCash.get
          )
        )(request, messages(application)).toString
      }
    }

    "must redirect to the overview if there is no result" in {
      val authConnector =
        new FakeAuthConnector(Some(Individual) ~ Some("internalId") ~ ConfidenceLevel.L250 ~ enrolments)
      val businessService = mock[BusinessService]

      when(
        businessService.getUkPropertyDetails(
          org.mockito.ArgumentMatchers.eq("nino"),
          ArgumentMatchers.eq("mtditid")
        )(any())
      ) thenReturn Future.successful(Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError)))

      val application =
        applicationBuilder(None, isAgent = false)
          .overrides(bind[AuthConnector].toInstance(authConnector))
          .overrides(bind[BusinessService].toInstance(businessService))
          .build()

      running(application) {
        val failure = intercept[InternalErrorFailure] {
          val request = FakeRequest(GET, routes.UKPropertyDetailsController.onPageLoad(taxYear).url)
          status(route(application, request).value)
        }
        failure.getMessage mustBe "UK property details not found from 1171 API"
      }
    }
  }
}
