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

import connectors.error.ApiError

trait ServiceError

case class HttpParserError(status: Int) extends ServiceError

case class UKPropertyDetailsError(nino: String, mtditid: String) extends ServiceError {
  override def toString: String = s"Unable to fetch UK property details for user with nino: $nino and mtditid: $mtditid"
}

case class ForeignPropertyDetailsError(nino: String, mtditid: String) extends ServiceError {
  override def toString: String = s"Unable to fetch Foreign property details for user with nino: $nino and mtditid: $mtditid"
}

case class NoPropertyDataError(nino: String, mtditid: String) extends ServiceError{
  override def toString: String = s"No property data received from downstream for user with nino: $nino and mtditid: $mtditid"
}
case class UnexpectedPropertyDataError(nino: String, mtditid: String, error:Either[ApiError, Seq[PropertyDetails]]) extends ServiceError{
  override def toString: String = s"Unexpected scenario when retrieving data: $error for user with nino: $nino and mtditid: $mtditid"
}

case class ConnectorError(statusCode: Int, message: String) extends ServiceError
