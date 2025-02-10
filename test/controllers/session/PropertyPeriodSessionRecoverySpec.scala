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

package controllers.session

import base.SpecBase
import models.backend.ForeignPropertyDetailsError
import models.requests.OptionalDataRequest
import models.{CapitalAllowancesForACar, ForeignIncomeTax, ForeignPropertyTax, User}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.AnyContent
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import repositories.SessionRepository
import service.PropertySubmissionService
import testHelpers.Fixture
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PropertyPeriodSessionRecoverySpec extends SpecBase with MockitoSugar with Fixture with Matchers {
  val propertyPeriodSubmissionService: PropertySubmissionService = mock[PropertySubmissionService]
  val sessionRepository: SessionRepository = mock[SessionRepository]

  val propertyPeriodSessionRecovery =
    new PropertyPeriodSessionRecovery(propertyPeriodSubmissionService, sessionRepository)

  val taxYear = 2024
  val user: User = User("", "", "", Some("agentReferenceNumber"))

  "PropertyPeriodSessionRecovery" - {
    "call the connector and set repository" in {
      val fakeRequest = FakeRequest()
      implicit val odr: OptionalDataRequest[AnyContent] = OptionalDataRequest[AnyContent](fakeRequest, "", user, None)
      implicit val hc: HeaderCarrier = HeaderCarrier()

      when(propertyPeriodSubmissionService.getUKPropertySubmission(taxYear, user)).thenReturn(
        Future.successful(
          Right(fetchedPropertyData)
        )
      )
      when(propertyPeriodSubmissionService.getForeignPropertySubmission(taxYear, user)).thenReturn(
        Future.successful(
          Left(ForeignPropertyDetailsError("AA000000A", "1234567890"))
        )
      )
      when(sessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

      val expectedCall = Redirect(controllers.routes.IndexController.onPageLoad)
      val result = propertyPeriodSessionRecovery.withUpdatedData(taxYear) { _ =>
        Future.successful(expectedCall)
      }

      whenReady(result) { r =>
        r mustEqual expectedCall
        verify(propertyPeriodSubmissionService, times(1)).getUKPropertySubmission(taxYear, user)
        verify(propertyPeriodSubmissionService, times(1)).getForeignPropertySubmission(taxYear, user)
      }
    }

    "merged uk and foreign fetched data" in {
      val expectedForeignPropertyData = foreignPropertyData.copy(
        foreignPropertyTax = Some(Map("USA" ->
          ForeignPropertyTax(Some(ForeignIncomeTax(foreignIncomeTaxYesNo = true, Some(100))), Some(true))))
      )

      val expectedUkPropertyData = ukPropertyData.copy(
        capitalAllowancesForACar = Some(CapitalAllowancesForACar(capitalAllowancesForACarYesNo = true, Some(3)))
      )

      val expectedForeignFetchedPropertyData = fetchedPropertyData.copy(foreignPropertyData = expectedForeignPropertyData)
      val expectedUkFetchedPropertyData = fetchedPropertyData.copy(ukPropertyData = expectedUkPropertyData)
      val expectedFetchedPropertyData = fetchedPropertyData.copy(ukPropertyData = expectedUkPropertyData, foreignPropertyData = expectedForeignPropertyData)

      propertyPeriodSessionRecovery.mergeFetchedData(eitherUkFetchedData = Right(expectedUkFetchedPropertyData),
          eitherForeignFetchedData = Right(expectedForeignFetchedPropertyData)) mustBe Right(expectedFetchedPropertyData)
    }
  }
}
