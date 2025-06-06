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

package service

import audit.RentalsIncome
import connectors.PropertySubmissionConnector
import connectors.error.ApiError
import models.backend.{ForeignPropertyDetailsError, HttpParserError, ServiceError, UKPropertyDetailsError}
import models.{DeleteJourneyAnswers, FetchedData, FetchedPropertyData, JourneyContext, User}
import play.api.Logging
import play.api.libs.json.Writes
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PropertySubmissionService @Inject() (
  propertyConnector: PropertySubmissionConnector,
  businessService: BusinessService
)(implicit
  val ec: ExecutionContext
) extends Logging {

  def getUKPropertySubmission(taxYear: Int, user: User)(implicit
    hc: HeaderCarrier
  ): Future[Either[ServiceError, FetchedData]] =
    businessService.getUkPropertyDetails(user.nino, user.mtditid).flatMap {
      case Left(error: ApiError) => Future.successful(Left(HttpParserError(error.status)))
      case Right(ukPropertyDetails) =>
        ukPropertyDetails
          .map { ukProperty =>
            propertyConnector.getPropertySubmission(taxYear, ukProperty.incomeSourceId, user).map {
              case Left(error) =>
                Left(HttpParserError(error.status))
              case Right(r) =>
                Right(r)
            }
          }
          .getOrElse(Future.successful(Left(UKPropertyDetailsError(user.nino, user.mtditid))))
    }

  def getForeignPropertySubmission(taxYear: Int, user: User)(implicit
                                                      hc: HeaderCarrier
  ): Future[Either[ServiceError, FetchedData]] =
    businessService.getForeignPropertyDetails(user.nino, user.mtditid).flatMap {
      case Left(error: ApiError) => Future.successful(Left(HttpParserError(error.status)))
      case Right(foreignPropertyDetails) =>
        foreignPropertyDetails
          .map { foreignProperty =>
            propertyConnector.getPropertySubmission(taxYear, foreignProperty.incomeSourceId, user).map {
              case Left(error) =>
                Left(HttpParserError(error.status))
              case Right(r) =>
                Right(r)
            }
          }
          .getOrElse(Future.successful(Left(ForeignPropertyDetailsError(user.nino, user.mtditid))))
    }

  def getForeignIncomeSubmission(taxYear: Int, user: User)(implicit
                                                           hc: HeaderCarrier
  ): Future[Either[ServiceError, FetchedData]] =
    businessService.getForeignPropertyDetails(user.nino, user.mtditid).flatMap {
      case Left(error: ApiError) => Future.successful(Left(HttpParserError(error.status)))
      case Right(foreignPropertyDetails) =>
        foreignPropertyDetails
          .map { foreignProperty =>
            propertyConnector.getForeignIncomeSubmission(taxYear, foreignProperty.incomeSourceId, user).map {
              case Left(error) =>
                Left(HttpParserError(error.status))
              case Right(r) =>
                Right(r)
            }
          }.getOrElse(Future.successful(Left(ForeignPropertyDetailsError(user.nino, user.mtditid))))
    }

  def savePropertyRentalsIncome(ctx: JourneyContext, propertyRentalsIncome: RentalsIncome)(implicit
    hc: HeaderCarrier
  ): Future[Either[ServiceError, Unit]] =
    saveUkPropertyJourneyAnswers(ctx, propertyRentalsIncome)

  def saveUkPropertyJourneyAnswers[A: Writes](ctx: JourneyContext, body: A, incomeSourceId: String)(implicit
                                                                                                    hc: HeaderCarrier
  ): Future[Either[ServiceError, Unit]] =
    propertyConnector.saveUkPropertyJourneyAnswers(ctx, body, incomeSourceId).map {
      case Left(error) => Left(HttpParserError(error.status))
      case Right(_)    => Right()
    }

  def saveUkPropertyJourneyAnswers[A: Writes](
    ctx: JourneyContext,
    body: A
  )(implicit hc: HeaderCarrier): Future[Either[ServiceError, Unit]] =
    businessService.getUkPropertyDetails(ctx.nino, ctx.mtditid).flatMap {
      case Left(error: ApiError) => Future.successful(Left(HttpParserError(error.status)))
      case Right(propertyDetails) =>
        propertyDetails
          .map { ukProperty =>
            propertyConnector.saveUkPropertyJourneyAnswers(ctx, body, ukProperty.incomeSourceId).map {
              case Left(error) => Left(HttpParserError(error.status))
              case Right(_)    => Right(())
            }
          }
          .getOrElse(Future.successful(Left(UKPropertyDetailsError(ctx.nino, ctx.mtditid))))
    }

  def saveForeignPropertyJourneyAnswers[A: Writes](
    ctx: JourneyContext,
    body: A
  )(implicit hc: HeaderCarrier): Future[Either[ServiceError, Unit]] =
    businessService.getForeignPropertyDetails(ctx.nino, ctx.mtditid).flatMap {
      case Left(error: ApiError) => Future.successful(Left(HttpParserError(error.status)))
      case Right(propertyDetails) =>
        propertyDetails
          .map { foreignProperty =>
            propertyConnector.saveForeignPropertyJourneyAnswers(ctx, body, foreignProperty.incomeSourceId).map {
              case Left(error) => Left(HttpParserError(error.status))
              case Right(_)    => Right(())
            }
          }
          .getOrElse(Future.successful(Left(ForeignPropertyDetailsError(ctx.nino, ctx.mtditid))))
    }

  def saveUkAndForeignPropertyJourneyAnswers[A: Writes](
    ctx: JourneyContext,
    body: A
  )(implicit hc: HeaderCarrier): Future[Either[ServiceError, Unit]] =
    businessService.getUkPropertyDetails(ctx.nino, ctx.mtditid).flatMap {
      case Left(error: ApiError) => Future.successful(Left(HttpParserError(error.status)))
      case Right(propertyDetails) =>
        propertyDetails
          .map { ukProperty =>
            propertyConnector.saveUkAndForeignPropertyJourneyAnswers(ctx, body, ukProperty.incomeSourceId).map {
              case Left(error) => Left(HttpParserError(error.status))
              case Right(_)    => Right(())
            }
          }
          .getOrElse(Future.successful(Left(UKPropertyDetailsError(ctx.nino, ctx.mtditid))))
    }

  def deleteForeignPropertyJourneyAnswers(
    ctx: JourneyContext,
    deleteJourneyAnswers: DeleteJourneyAnswers
  )(implicit hc: HeaderCarrier): Future[Either[ServiceError, Unit]] =
    businessService.getForeignPropertyDetails(ctx.nino, ctx.mtditid).flatMap {
      case Left(error: ApiError) => Future.successful(Left(HttpParserError(error.status)))
      case Right(propertyDetails) =>
        propertyDetails
          .map { foreignProperty =>
            propertyConnector.deleteForeignPropertyJourneyAnswers(ctx, deleteJourneyAnswers, foreignProperty.incomeSourceId).map {
              case Left(error) => Left(HttpParserError(error.status))
              case Right(_)    => Right(())
            }
          }
          .getOrElse(Future.successful(Left(ForeignPropertyDetailsError(ctx.nino, ctx.mtditid))))
    }

  def saveForeignDividendsJourneyAnswers[A: Writes](
                                   ctx: JourneyContext,
                                   body: A
                                 )(implicit hc: HeaderCarrier): Future[Either[ServiceError, Unit]] =
    businessService.getForeignPropertyDetails(ctx.nino, ctx.mtditid).flatMap {
      case Left(error: ApiError) => Future.successful(Left(HttpParserError(error.status)))
      case Right(propertyDetails) =>
        propertyDetails
          .map { foreignProperty =>
            propertyConnector.saveForeignDividendsJourneyAnswers(ctx, body, foreignProperty.incomeSourceId).map {
              case Left(error) => Left(HttpParserError(error.status))
              case Right(_)    => Right(())
            }
          }
          .getOrElse(Future.successful(Left(ForeignPropertyDetailsError(ctx.nino, ctx.mtditid))))
    }
}
