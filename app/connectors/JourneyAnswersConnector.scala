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

import config.FrontendAppConfig
import connectors.error.ApiError
import models.{FetchedBackendData, User}
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class UpdateStatusResponse(httpResponse: HttpResponse, result: Either[ApiError, FetchedBackendData])

object UpdateStatusResponse {
  implicit val getPropertyPeriodicSubmissionResponseReads: HttpReads[UpdateStatusResponse] =
    new HttpReads[UpdateStatusResponse] with Parser {

      override protected[connectors] val parserName: String = this.getClass.getSimpleName

      override def read(method: String, url: String, response: HttpResponse): UpdateStatusResponse =
        response.status match {
          case OK => UpdateStatusResponse(response, extractResult(response))
          case NOT_FOUND | INTERNAL_SERVER_ERROR | SERVICE_UNAVAILABLE | BAD_REQUEST =>
            UpdateStatusResponse(response, handleError(response, response.status))
          case _ => UpdateStatusResponse(response, handleError(response, INTERNAL_SERVER_ERROR))
        }

      private def extractResult(response: HttpResponse): Either[ApiError, FetchedBackendData] =
        response.json
          .validate[FetchedBackendData]
          .fold[Either[ApiError, FetchedBackendData]](_ => badSuccessJsonResponse, parsedModel => Right(parsedModel))
    }
}

class JourneyAnswersConnector @Inject() (httpClient: HttpClientV2, appConfig: FrontendAppConfig)(implicit
  ec: ExecutionContext
) extends Logging {

  def setStatus(taxYear: Int, incomeSourceId: String, journeyName: String, status: String, user: User)(implicit
    hc: HeaderCarrier
  ): Future[Either[ApiError, FetchedBackendData]] = {
    val updateStatusUrl =
      s"${appConfig.propertyServiceBaseUrl}/completed-section/$incomeSourceId/$journeyName/$taxYear"

    httpClient
      .put(url"$updateStatusUrl")
      .setHeader("mtditid" -> user.mtditid)
      .withBody(Json.obj("status" -> JsString(status)))
      .execute[UpdateStatusResponse]
      .map { response: UpdateStatusResponse =>
        if (response.result.isLeft) {
          logger.error(
            s"Error updating the status of the journey" +
              s" status: ${response.httpResponse.status}; Body:${response.httpResponse.body}"
          )
        }
        response.result
      }
  }
}
