/*
 * Copyright 2025 HM Revenue & Customs
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
import connectors.response.SessionDataHttpResponse.{SessionDataResponse, SessionDataResponseReads}
import play.api.Logging
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, JsValidationException, StringContextOps, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SessionDataConnector @Inject()(config: FrontendAppConfig,
                                     httpClient: HttpClientV2)(implicit ec: ExecutionContext) extends Logging {

  def getSessionData(implicit hc: HeaderCarrier): Future[SessionDataResponse] =
    httpClient
      .get(url"${config.vcSessionServiceBaseUrl}/income-tax-session-data")
      .execute[SessionDataResponse]
      .recoverWith {
        case e: UpstreamErrorResponse =>
          logger.warn(s"[SessionDataConnector] - Received error status ${e.statusCode} with requestId: ${hc.requestId}")
          Future.failed(e)
        case e: JsValidationException =>
          logger.warn(s"[SessionDataConnector] - Unable to parse the content of a response with requestId: ${hc.requestId}")
          Future.failed(e)
        case e =>
          logger.warn(s"[SessionDataConnector] - Received an error with requestId: ${hc.requestId}")
          Future.failed(e)
      }

}
