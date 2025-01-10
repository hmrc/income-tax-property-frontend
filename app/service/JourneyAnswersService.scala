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

import connectors.JourneyAnswersConnector
import connectors.error.ApiError
import models.backend.{ConnectorError, ForeignPropertyDetailsError, HttpParserError, ServiceError, UKPropertyDetailsError}
import models.{JourneyContext, User}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class JourneyAnswersService @Inject() (
  journeyAnswersConnector: JourneyAnswersConnector,
  businessService: BusinessService
)(implicit
  val ec: ExecutionContext
) extends Logging {

  def setStatus(ctx: JourneyContext, status: String, user: User)(implicit
    hc: HeaderCarrier
  ): Future[Either[ServiceError, String]] =
    businessService.getUkPropertyDetails(ctx.nino, ctx.mtditid).flatMap {
      case Left(error: ApiError) => Future.successful(Left(HttpParserError(error.status)))
      case Right(propertyDetails) =>
        propertyDetails
          .map { ukProperty =>
            journeyAnswersConnector
              .setStatus(ctx.taxYear, ukProperty.incomeSourceId, ctx.journeyPath, status, user)
              .map {
                case Left(error) =>
                  logger.error(s"Unable to access the endpoint that allows the update of the journey status: $error")
                  Left(ConnectorError(error.status, "Error while calling set status on backend"))
                case Right(r) => Right(r)
              }
          }
          .getOrElse(Future.successful(Left(UKPropertyDetailsError(ctx.nino, ctx.mtditid))))

    }

  def setForeignStatus(ctx: JourneyContext, status: String, user: User, countryCode: String)(implicit
    hc: HeaderCarrier
  ): Future[Either[ServiceError, String]] =
    businessService.getForeignPropertyDetails(ctx.nino, ctx.mtditid).flatMap {
      case Left(error: ApiError) => Future.successful(Left(HttpParserError(error.status)))
      case Right(propertyDetails) =>
        propertyDetails
          .map { foreignProperty =>
            journeyAnswersConnector
              .setForeignStatus(ctx.taxYear, foreignProperty.incomeSourceId, ctx.journeyPath, status, user, countryCode)
              .map {
                case Left(error) =>
                  logger.error(s"Unable to access the endpoint that allows the update of the journey status: $error")
                  Left(ConnectorError(error.status, "Error while calling set status on backend"))
                case Right(r) => Right(r)
              }
          }
          .getOrElse(Future.successful(Left(ForeignPropertyDetailsError(ctx.nino, ctx.mtditid))))

    }
}
