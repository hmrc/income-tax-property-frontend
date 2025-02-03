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

package service

import connectors.BusinessConnector
import connectors.error.ApiError
import models.IncomeSourcePropertyType._
import models.User
import models.backend.{BusinessDetails, HttpParserError, PropertyDetails}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessService @Inject() (businessConnector: BusinessConnector)(implicit val ec: ExecutionContext)
    extends Logging {

  def getBusinessDetails(user: User)(implicit hc: HeaderCarrier): Future[Either[HttpParserError, BusinessDetails]] =
    businessConnector.getBusinessDetails(user.nino, user.mtditid).map {
      case Left(error: ApiError)  => Left(HttpParserError(error.status))
      case Right(businessDetails) => Right(businessDetails)
    }

  def getUkPropertyDetails(nino: String, mtditid: String)(implicit
    hc: HeaderCarrier
  ): Future[Either[ApiError, Option[PropertyDetails]]] =
    getPropertyDetails(nino, mtditid, isUkProperty)

  def getForeignPropertyDetails(nino: String, mtditid: String)(implicit
    hc: HeaderCarrier
  ): Future[Either[ApiError, Option[PropertyDetails]]] =
    getPropertyDetails(nino, mtditid, isForeignProperty)

  def getAllPropertyDetails(nino: String, mtditid: String)(implicit
    hc: HeaderCarrier
  ): Future[Either[ApiError, Seq[PropertyDetails]]] =
    getPropertyDetailsAll(nino, mtditid,isValidProperty)



  private def getPropertyDetails(
    nino: String,
    mtditid: String,
    isPropertyType: PropertyDetails => Boolean
  )(implicit hc: HeaderCarrier): Future[Either[ApiError, Option[PropertyDetails]]] =
    businessConnector.getBusinessDetails(nino, mtditid).map {
      case Left(error: ApiError)  => Left(error)
      case Right(businessDetails) => Right(businessDetails.propertyData.find(isPropertyType))
    }

  private def getPropertyDetailsAll(
    nino: String,
    mtditid: String,
    isValidProperty: PropertyDetails => Boolean
  )(implicit hc: HeaderCarrier): Future[Either[ApiError, Seq[PropertyDetails]]] =
    businessConnector.getBusinessDetails(nino, mtditid).map {
      case Left(error: ApiError)  => Left(error)
      case Right(businessDetails) => Right(businessDetails.propertyData.filter(isValidProperty))
    }

  private def isUkProperty(property: PropertyDetails): Boolean =
    property.incomeSourceType.contains(
      UKProperty.toString
    ) && property.tradingStartDate.isDefined && property.accrualsOrCash.isDefined

  private def isForeignProperty(property: PropertyDetails): Boolean =
    property.incomeSourceType.contains(
      ForeignProperty.toString
    ) && property.tradingStartDate.isDefined && property.accrualsOrCash.isDefined

  private def isValidProperty(property: PropertyDetails): Boolean =
    property.incomeSourceType.exists(str => str.contains(UKProperty.toString)  ||  str.contains(ForeignProperty.toString) ) &&
      property.tradingStartDate.isDefined &&
      property.accrualsOrCash.isDefined
  }
