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

import connectors.PropertySubmissionConnector
import connectors.error.ApiError
import models.backend.{HttpParserError, PropertyDataError, ServiceError}
import models.propertyrentals.income.SaveIncome
import models.{EsbasWithSupportingQuestions, FetchedBackendData, JourneyContext, User}
import play.api.Logging
import play.api.libs.json.Writes
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PropertySubmissionService @Inject()(
                                           propertyConnector: PropertySubmissionConnector,
                                           businessService: BusinessService
                                         )(implicit
                                           val ec: ExecutionContext
                                         ) extends Logging {

  def getPropertySubmission(taxYear: Int, user: User)(implicit
                                                      hc: HeaderCarrier
  ): Future[Either[ApiError, FetchedBackendData]] =
    propertyConnector.getPropertySubmission(taxYear, user.mtditid, user)

  def savePropertyRentalsIncome(ctx: JourneyContext, saveIncome: SaveIncome)(
    implicit hc: HeaderCarrier
  ): Future[Either[ApiError, Unit]] = {
    propertyConnector.saveIncome(ctx, ctx.mtditid, saveIncome)
  }

  def saveEsba( //Todo: Finially this should be integrated into saveAnswers reusage
                ctx: JourneyContext, esbasWithSupportingQuestions: EsbasWithSupportingQuestions
              )
              (
                implicit hc: HeaderCarrier
              ): Future[Either[ApiError, Unit]] = {
    propertyConnector.updateEsba(ctx, ctx.mtditid, esbasWithSupportingQuestions) //Todo: mdtd? income-source-id?
  }

  def saveJourneyAnswers[A: Writes](
    ctx: JourneyContext,
    body: A
  )(implicit hc: HeaderCarrier): Future[Either[ServiceError, Unit]] =
    businessService.getUkPropertyDetails(ctx.nino, ctx.mtditid).flatMap {
      case Left(error: ApiError) => Future.successful(Left(HttpParserError(error.status)))
      case Right(propertyDetails) =>
        propertyDetails
          .map { ukProperty =>
            propertyConnector.saveJourneyAnswers(ctx, ukProperty.incomeSourceId, body).map {
              case Left(error) => Left(HttpParserError(error.status))
              case Right(_)    => Right(())
            }
          }
          .getOrElse(Future.successful(Left(PropertyDataError())))

    }

}
