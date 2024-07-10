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

package connectors.response

import connectors.Parser
import connectors.error.ApiError
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

case class CreateOrUpdateJourneyAnswersResponse(httpResponse: HttpResponse, result: Either[ApiError, Unit])

object CreateOrUpdateJourneyAnswersResponse {

  implicit val createOrUpdateJourneyAnswersResponseReads: HttpReads[CreateOrUpdateJourneyAnswersResponse] =
    new HttpReads[CreateOrUpdateJourneyAnswersResponse] with Parser {

      override protected[connectors] val parserName: String = this.getClass.getSimpleName

      override def read(method: String, url: String, response: HttpResponse): CreateOrUpdateJourneyAnswersResponse =
        response.status match {
          case NO_CONTENT | CREATED => CreateOrUpdateJourneyAnswersResponse(response, Right(()))
          case NOT_FOUND | INTERNAL_SERVER_ERROR | SERVICE_UNAVAILABLE | BAD_REQUEST | UNPROCESSABLE_ENTITY =>
            CreateOrUpdateJourneyAnswersResponse(response, handleError(response, response.status))
          case _ => CreateOrUpdateJourneyAnswersResponse(response, handleError(response, INTERNAL_SERVER_ERROR))
        }
    }
}
