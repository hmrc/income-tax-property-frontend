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

package service

import base.SpecBase
import connectors.PropertyPeriodicSubmissionConnector
import connectors.error.{ApiError, SingleErrorBody}
import models.{FetchedPropertyData, User}
import play.api.libs.json.JsObject
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock

class PropertyPeriodSubmissionServiceSpec extends SpecBase {
  val propertyPeriodicSubmissionConnector = mock[PropertyPeriodicSubmissionConnector]
  val taxYear = 2024
  val user = User("", "", "", false)
  implicit val hc = HeaderCarrier()
  val propertyPeriodSubmissionService = new PropertyPeriodSubmissionService(propertyPeriodicSubmissionConnector)

  "PropertyPeriodSubmissionService" - {
    "return success when connector returns success" in {
      val resultFromConnector = FetchedPropertyData(new JsObject(Map()))
      when(
        propertyPeriodicSubmissionConnector.getPropertyPeriodicSubmission(taxYear, user.mtditid, user)) thenReturn Future.successful(Right(resultFromConnector))

      val resultFromService = propertyPeriodSubmissionService.getPropertyPeriodicSubmission(taxYear, user)

      whenReady(resultFromService) { result =>
        result match {
          case Right(r) => r mustBe resultFromConnector
          case Left(_) => fail("Service should return success when connector returns success")
        }
      }
    }

    "return failure when connector returns failure" in {
      val resultFromConnector = ApiError(500, SingleErrorBody("500", "Some error"))

      when(
        propertyPeriodicSubmissionConnector.getPropertyPeriodicSubmission(taxYear, user.mtditid, user)
      ).thenReturn(Future.successful(Left(resultFromConnector)))

      val resultFromService = propertyPeriodSubmissionService.getPropertyPeriodicSubmission(taxYear, user)

      whenReady(resultFromService) { result =>
        result match {
          case Right(_) => fail("Service should return failure when connector returns failure")
          case Left(error) => error mustBe resultFromConnector
        }
      }
    }
  }
}
