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
import models.backend.PropertyDetails
import service.BusinessService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait BusinessServiceLike {

  def withUkPropertyDetails(
    businessService: BusinessService,
    nino: String,
    mtditid: String
  )(implicit
    hc: HeaderCarrier,
    executor: ExecutionContext
  ): Future[Either[ApiError, PropertyDetails]] =
    businessService.getUkPropertyDetails(nino, mtditid)(hc).flatMap {
      case Right(Some(propertyData)) =>
        Future.successful(Right(propertyData))
      case Left(error: ApiError) =>
        Future.successful(Left(error))
    }
}
