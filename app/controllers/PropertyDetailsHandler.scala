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

package controllers

import connectors.error.ApiError
import controllers.exceptions.InternalErrorFailure
import models.backend.PropertyDetails
import play.api.Logging
import service.BusinessService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait PropertyDetailsHandler extends Logging {

  def withUkPropertyDetails[T](
    businessService: BusinessService,
    nino: String,
    mtditid: String
  )(block: PropertyDetails => Future[T])(implicit
    hc: HeaderCarrier,
    executor: ExecutionContext
  ): Future[T] =
    businessService.getUkPropertyDetails(nino, mtditid)(hc).flatMap {
      case Right(Some(propertyData)) =>
        block(propertyData)
      case Left(apiError: ApiError) =>
        logger.error(s"Encountered an issue retrieving property data from the business API: ${apiError.toMessage}")
        Future.failed(InternalErrorFailure("Encountered an issue retrieving property data from the business API"))
    }

  def withForeignPropertyDetails[T](
    businessService: BusinessService,
    nino: String,
    mtditid: String
  )(block: PropertyDetails => Future[T])(implicit
    hc: HeaderCarrier,
    executor: ExecutionContext
  ): Future[T] =
    businessService.getForeignPropertyDetails(nino, mtditid)(hc).flatMap {
      case Right(Some(propertyData)) =>
        block(propertyData)
      case Left(apiError: ApiError) =>
        logger.error(s"Encountered an issue retrieving property data from the business API: ${apiError.toMessage}")
        Future.failed(InternalErrorFailure("Encountered an issue retrieving property data from the business API"))
    }
}
