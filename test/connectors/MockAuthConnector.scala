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

package connectors

import org.scalamock.handlers.CallHandler4
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockAuthConnector extends MockFactory { _: TestSuite =>

  lazy val mockAuthConnector: AuthConnector = mock[AuthConnector]

  object MockAuthConnector {

    def authorise[T](predicate: Predicate)(response: Future[T]): CallHandler4[Predicate, Retrieval[T], HeaderCarrier, ExecutionContext, Future[T]] =
      (mockAuthConnector
        .authorise[T](_: Predicate, _: Retrieval[T])(_: HeaderCarrier, _: ExecutionContext))
        .expects(predicate, *, *, *)
        .returns(response)
  }

}
