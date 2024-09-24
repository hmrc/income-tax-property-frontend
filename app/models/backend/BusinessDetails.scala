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

package models.backend

import controllers.exceptions.NotFoundException
import models.AccountingMethod
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate
import scala.concurrent.Future

case class BusinessDetails(propertyData: Seq[PropertyDetails])

object BusinessDetails {
  implicit val format: OFormat[BusinessDetails] = Json.format[BusinessDetails]
}

// accrualsOrCash - false = "CASH"
case class PropertyDetails(
  incomeSourceType: Option[String],
  tradingStartDate: Option[LocalDate],
  accrualsOrCash: Option[Boolean],
  incomeSourceId: String
) {

  def getAccountingMethod: Option[AccountingMethod] =
    accrualsOrCash.map {
      case true  => AccountingMethod.Traditional
      case false => AccountingMethod.Cash
    }

  def accountingMethod: Future[AccountingMethod] =
    getAccountingMethod.fold[Future[AccountingMethod]](
      Future.failed(NotFoundException("Accounting method not found"))
    )(Future.successful)
}

object PropertyDetails {
  implicit val format: OFormat[PropertyDetails] = Json.format[PropertyDetails]
}
