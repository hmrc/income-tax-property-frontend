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
import connectors.response.{CreateOrUpdateJourneyAnswersResponse, GetPropertyPeriodicSubmissionResponse}
import models.propertyrentals.income.SaveIncome
import models.{EsbasWithSupportingQuestions, FetchedBackendData, JourneyContext, User}
import play.api.Logging
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PropertySubmissionConnector @Inject()(httpClient: HttpClientV2, appConfig: FrontendAppConfig)(implicit
                                                                                                    ec: ExecutionContext
) extends Logging {

  def getPropertySubmission(taxYear: Int, incomeSourceId: String, user: User)(implicit
                                                                              hc: HeaderCarrier
  ): Future[Either[ApiError, FetchedBackendData]] = {
    val propertyPeriodicSubmissionUrl =
      s"${appConfig.propertyServiceBaseUrl}/property/$taxYear/income/${user.nino}/$incomeSourceId"

    httpClient
      .get(url"$propertyPeriodicSubmissionUrl")
      .setHeader("mtditid" -> user.mtditid)
      .execute[GetPropertyPeriodicSubmissionResponse]
      .map { response: GetPropertyPeriodicSubmissionResponse =>
        if (response.result.isLeft) {
          val correlationId =
            response.httpResponse.header(key = "CorrelationId").map(id => s" CorrelationId: $id").getOrElse("")
          logger.error(
            s"Error getting property periodic submission from the Integration Framework:" +
              s" correlationId: $correlationId; status: ${response.httpResponse.status}; Body:${response.httpResponse.body}"
          )
        }
        response.result
      }
  }

  def saveJourneyAnswers[A: Writes](
    ctx: JourneyContext,
    incomeSourceId: String,
    body: A
  )(implicit hc: HeaderCarrier): Future[Either[ApiError, Unit]] = {
    val propertyUrl =
      s"${appConfig.propertyServiceBaseUrl}/property/${ctx.taxYear}/$incomeSourceId/${ctx.journeyName}/${ctx.nino}/answers"

    httpClient
      .post(url"$propertyUrl")
      .setHeader("mtditid" -> ctx.mtditid)
      .setHeader("CorrelationId" -> UUID.randomUUID().toString)
      .withBody(Json.toJson(body))
      .execute[CreateOrUpdateJourneyAnswersResponse]
      .map { response: CreateOrUpdateJourneyAnswersResponse =>
        if (response.result.isLeft) {
          val correlationId =
            response.httpResponse.header(key = "CorrelationId").map(id => s" CorrelationId: $id").getOrElse("")
          logger.error(
            "Error posting journey answers to income-tax-property:" +
              s" correlationId: $correlationId; status: ${response.httpResponse.status}; Body:${response.httpResponse.body}"
          )
        } else {
          logger.info(
            "Journey answers successfully posted to income-tax-property:" +
              s" status: ${response.httpResponse.status}; Body:${response.httpResponse.body}"
          )
        }
        response.result
      }
  }

  def updateEsba(
                  ctx: JourneyContext,
                  incomeSourceId: String,
                  esbasWithSupportingQuestions: EsbasWithSupportingQuestions
                )
                (
                  implicit hc: HeaderCarrier
                ): Future[Either[ApiError, Unit]] = {

    val propertyUrl = s"${appConfig.propertyServiceBaseUrl}/property/${ctx.taxYear}/${ctx.nino}/$incomeSourceId/esba/answers"

    httpClient
      .put(url"$propertyUrl")
      .setHeader("mtditid" -> ctx.mtditid)
      .setHeader("CorrelationId" -> UUID.randomUUID().toString)
      .withBody(Json.toJson(esbasWithSupportingQuestions))
      .execute[CreateOrUpdateJourneyAnswersResponse]
      .map { response: CreateOrUpdateJourneyAnswersResponse =>
        if (response.result.isLeft) {
          val correlationId =
            response.httpResponse.header(key = "CorrelationId").map(id => s" CorrelationId: $id").getOrElse("")
          logger.error(
            "Error posting journey answers to income-tax-property:" +
              s" correlationId: $correlationId; status: ${response.httpResponse.status}; Body:${response.httpResponse.body}"
          )
        }
        response.result
      }
  }

  def saveIncome(
                  ctx: JourneyContext,
                  incomeSourceId: String,
                  saveIncome: SaveIncome
                )
                (
                  implicit hc: HeaderCarrier
                ): Future[Either[ApiError, Unit]] = {

    val propertyUrl = s"${appConfig.propertyServiceBaseUrl}/property/${ctx.taxYear}/$incomeSourceId/income/${ctx.nino}/answers"

    httpClient
      .post(url"$propertyUrl")
      .setHeader("mtditid" -> ctx.mtditid)
      .setHeader("CorrelationId" -> UUID.randomUUID().toString)
      .withBody(Json.toJson(saveIncome))
      .execute[CreateOrUpdateJourneyAnswersResponse]
      .map { response: CreateOrUpdateJourneyAnswersResponse =>
        if (response.result.isLeft) {
          val correlationId =
            response.httpResponse.header(key = "CorrelationId").map(id => s" CorrelationId: $id").getOrElse("")
          logger.error(
            "Error posting journey answers to income-tax-property:" +
              s" correlationId: $correlationId; status: ${response.httpResponse.status}; Body:${response.httpResponse.body}"
          )
        }
        response.result
      }
  }
}
