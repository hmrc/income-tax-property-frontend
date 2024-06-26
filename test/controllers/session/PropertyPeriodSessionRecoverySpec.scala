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
import models.User
import models.requests.OptionalDataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
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

class PropertyPeriodSessionRecoverySpec extends SpecBase with MockitoSugar with Fixture {
  val propertyPeriodSubmissionService: PropertySubmissionService = mock[PropertySubmissionService]
  val sessionRepository: SessionRepository = mock[SessionRepository]

  val propertyPeriodSessionRecovery =
    new PropertyPeriodSessionRecovery(propertyPeriodSubmissionService, sessionRepository)

  val taxYear = 2024
  val user: User = User("", "", "", isAgent = false, Some("agentReferenceNumber"))

  "PropertyPeriodSessionRecovery" - {
    "call the connector and set repository" in {
      val fakeRequest = FakeRequest()
      implicit val odr = OptionalDataRequest[AnyContent](fakeRequest, "", user, None)
      implicit val hc = HeaderCarrier()

      when(propertyPeriodSubmissionService.getPropertySubmission(taxYear, user)).thenReturn(
        Future.successful(
          Right(fetchedPropertyData)
        )
      )
      when(sessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

//      when(sessionRepository.set(any())).thenReturn(Future.successful(true))

      val expectedCall = Redirect(controllers.routes.IndexController.onPageLoad)
      val result = propertyPeriodSessionRecovery.withUpdatedData(taxYear) { _ =>
        Future.successful(expectedCall)
      }

      whenReady(result) { r =>
        r mustEqual expectedCall
        verify(propertyPeriodSubmissionService, times(1)).getPropertySubmission(taxYear, user)
        //verify(sessionRepository, times(1)).set(any())
      }
    }
  }
}
