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

package connectors

import config.FrontendAppConfig
import connectors.error.ApiError
import connectors.response.GetBusinessDetailsResponse
import models.User
import models.backend.BusinessDetails
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessConnector @Inject()(httpClient: HttpClient,
                                  appConfig: FrontendAppConfig)(implicit ec: ExecutionContext) extends Logging {

  def getBusinessDetails(user: User)
                        (implicit hc: HeaderCarrier): Future[Either[ApiError, BusinessDetails]] = {

    val propertyBEUrl = appConfig.propertyServiceBaseUrl + s"/business-details/nino/${user.nino}"

    httpClient.GET[GetBusinessDetailsResponse](propertyBEUrl)(
      implicitly[HttpReads[GetBusinessDetailsResponse]],
      hc.withExtraHeaders(headers = "mtditid" -> user.mtditid),
      ec)
      .map { response: GetBusinessDetailsResponse =>
        if(response.result.isLeft) {
          val correlationId = response.httpResponse.header(key = "CorrelationId").map(id => s" CorrelationId: $id").getOrElse("")
          logger.error("Error getting business details from the Integration Framework:" +
            s" correlationId: $correlationId; status: ${response.httpResponse.status}; Body:${response.httpResponse.body}")
        }
        response.result
      }
  }


}
