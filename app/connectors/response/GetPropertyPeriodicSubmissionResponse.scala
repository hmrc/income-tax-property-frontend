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
import models.FetchedPropertyData
import play.api.Logging
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

final case class GetPropertyPeriodicSubmissionResponse(
  httpResponse: HttpResponse,
  result: Either[ApiError, FetchedPropertyData]
)

object GetPropertyPeriodicSubmissionResponse {

  implicit val getPropertyPeriodicSubmissionResponseReads: HttpReads[GetPropertyPeriodicSubmissionResponse] =
    new HttpReads[GetPropertyPeriodicSubmissionResponse] with Parser with Logging {

      override protected[connectors] val parserName: String = this.getClass.getSimpleName

      override def read(method: String, url: String, response: HttpResponse): GetPropertyPeriodicSubmissionResponse =
        response.status match {
          case OK => GetPropertyPeriodicSubmissionResponse(response, extractResult(response))
          case NOT_FOUND | INTERNAL_SERVER_ERROR | SERVICE_UNAVAILABLE | BAD_REQUEST =>
            GetPropertyPeriodicSubmissionResponse(response, handleError(response, response.status))
          case _ => GetPropertyPeriodicSubmissionResponse(response, handleError(response, INTERNAL_SERVER_ERROR))
        }

      private def extractResult(response: HttpResponse): Either[ApiError, FetchedPropertyData] =
        response.json
          .validate[FetchedPropertyData]
          .fold[Either[ApiError, FetchedPropertyData]](
            e => {
              //Todo: Proper specific error to be logged.
              logger.error(s"[GetPropertyPeriodicSubmissionResponse][extractResult]: Error parsing property submission JSON: $e")
              badSuccessJsonResponse
            },
            parsedModel => Right(parsedModel)
          )
    }
}
