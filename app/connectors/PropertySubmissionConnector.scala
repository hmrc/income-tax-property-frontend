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
import connectors.response.{CreateOrUpdateJourneyAnswersResponse, DeleteJourneyAnswersResponse, GetForeignIncomeSubmissionResponse, GetPropertyPeriodicSubmissionResponse}
import models.{DeleteJourneyAnswers, FetchedData, JourneyContext, User}
import play.api.Logging
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PropertySubmissionConnector @Inject() (httpClient: HttpClientV2, appConfig: FrontendAppConfig)(implicit
  ec: ExecutionContext
) extends Logging {

  def getPropertySubmission(taxYear: Int, incomeSourceId: String, user: User)(implicit
    hc: HeaderCarrier
  ): Future[Either[ApiError, FetchedData]] = {

    val propertyUrl =
      s"${appConfig.propertyServiceBaseUrl}/property/$taxYear/income/${user.nino}/$incomeSourceId"

    httpClient
      .get(url"$propertyUrl")
      .setHeader("mtditid" -> user.mtditid)
      .execute[GetPropertyPeriodicSubmissionResponse]
      .map { response: GetPropertyPeriodicSubmissionResponse =>
        if (response.result.isLeft) {
          val correlationId =
            response.httpResponse.header(key = "CorrelationId").map(id => s" CorrelationId: $id").getOrElse("")
          logger.error(
            s"[getPropertySubmission] Error getting property data from the backend: " +
              s"correlationId: $correlationId; url: $propertyUrl " +
              s"status: ${response.httpResponse.status}; Response Body:${response.httpResponse.body}"
          )
        }
        response.result
      }
  }

  def getForeignIncomeSubmission(taxYear: Int, incomeSourceId: String, user: User)(implicit
                                                                                   hc: HeaderCarrier
  ): Future[Either[ApiError, FetchedData]] = {

    val propertyUrl =
      s"${appConfig.propertyServiceBaseUrl}/income/$taxYear/dividends/${user.nino}/$incomeSourceId"

    httpClient
      .get(url"$propertyUrl")
      .setHeader("mtditid" -> user.mtditid)
      .execute[GetForeignIncomeSubmissionResponse]
      .map { response: GetForeignIncomeSubmissionResponse =>
        if (response.result.isLeft) {
          val correlationId =
            response.httpResponse.header(key = "CorrelationId").map(id => s" CorrelationId: $id").getOrElse("")
          logger.error(
            s"[getPropertySubmission] Error getting property data from the backend: " +
              s"correlationId: $correlationId; url: $propertyUrl " +
              s"status: ${response.httpResponse.status}; Response Body:${response.httpResponse.body}"
          )
        }
        response.result
      }
  }

  private def saveJourneyAnswers[A: Writes](
    prefix: String,
    ctx: JourneyContext,
    body: A,
    incomeSourceId: String
  )(implicit hc: HeaderCarrier): Future[Either[ApiError, Unit]] = {

    val propertyUrl =
      s"${appConfig.propertyServiceBaseUrl}/$prefix/${ctx.taxYear}/$incomeSourceId/${ctx.journeyPath}/${ctx.nino}/answers"

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
            "[saveJourneyAnswers] Error posting journey answers to income-tax-property:" +
              s"correlationId: $correlationId; url: $propertyUrl " +
              s"status: ${response.httpResponse.status}; Response Body:${response.httpResponse.body}"
          )
        }
        response.result
      }
  }

  def saveUkPropertyJourneyAnswers[A: Writes](
    ctx: JourneyContext,
    body: A,
    incomeSourceId: String
  )(implicit hc: HeaderCarrier): Future[Either[ApiError, Unit]] =
    saveJourneyAnswers("uk-property", ctx, body, incomeSourceId)

  def saveForeignPropertyJourneyAnswers[A: Writes](
                                     ctx: JourneyContext,
                                     body: A,
                                     incomeSourceId: String
                                   )(implicit hc: HeaderCarrier): Future[Either[ApiError, Unit]] =
    saveJourneyAnswers("foreign-property", ctx, body, incomeSourceId)

  def saveUkAndForeignPropertyJourneyAnswers[A: Writes](
                                            ctx: JourneyContext,
                                            body: A,
                                            incomeSourceId: String
                                          )(implicit hc: HeaderCarrier): Future[Either[ApiError, Unit]] =
    saveJourneyAnswers("uk-and-foreign-property", ctx, body, incomeSourceId)

  def deleteForeignPropertyJourneyAnswers(
    ctx: JourneyContext,
    deleteJourneyAnswers: DeleteJourneyAnswers,
    incomeSourceId: String
  )(implicit hc: HeaderCarrier): Future[Either[ApiError, Unit]] = {
    val propertyUrl =
      s"${appConfig.propertyServiceBaseUrl}/foreign-property/${ctx.taxYear}/$incomeSourceId/foreign-property-delete/${ctx.nino}/answers"

    httpClient
      .post(url"$propertyUrl")
      .setHeader("mtditid" -> ctx.mtditid)
      .setHeader("CorrelationId" -> UUID.randomUUID().toString)
      .withBody(Json.toJson(deleteJourneyAnswers))
      .execute[DeleteJourneyAnswersResponse]
      .map { response: DeleteJourneyAnswersResponse =>
        if (response.result.isLeft) {
          val correlationId =
            response.httpResponse.header(key = "CorrelationId").map(id => s" CorrelationId: $id").getOrElse("")
          logger.error(
            "[deleteJourneyAnswers] Error deleting journey answers from income-tax-property:" +
              s"correlationId: $correlationId; url: $propertyUrl " +
              s"status: ${response.httpResponse.status}; Response Body:${response.httpResponse.body}"
          )
        }
        response.result
      }
  }

  def saveForeignDividendsJourneyAnswers[A: Writes](
                                               ctx: JourneyContext,
                                               body: A,
                                               incomeSourceId: String
                                             )(implicit hc: HeaderCarrier): Future[Either[ApiError, Unit]] = {
    saveJourneyAnswers("foreign-income", ctx, body, incomeSourceId)
  }
}
