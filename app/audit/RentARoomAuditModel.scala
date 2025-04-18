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

package audit

import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.http.HeaderCarrier

case class RentARoomAuditModel[T](
  clientIP: String,
  clientPort: String,
  nino: String,
  userType: String,
  mtdItId: String,
  agentReferenceNumber: Option[String],
  taxYear: Int,
  isUpdate: Boolean,
  sectionName: String,
  userEnteredRentARoomDetails: T
)

object RentARoomAuditModel {
  implicit def format[T](implicit rentARoomAuditModelFormat: Format[T]): OFormat[RentARoomAuditModel[T]] =
    Json.format[RentARoomAuditModel[T]]

  def apply[T](nino: String,
            userType: String,
            mtdItId: String,
            agentReferenceNumber: Option[String],
            taxYear: Int,
            isUpdate: Boolean,
            sectionName: String,
            userEnteredRentARoomDetails: T)(implicit hc: HeaderCarrier): RentARoomAuditModel[T] = {
    RentARoomAuditModel(
      clientIP = hc.trueClientIp.getOrElse("-"),
      clientPort = hc.trueClientPort.getOrElse("-"),
      nino,
      userType,
      mtdItId,
      agentReferenceNumber,
      taxYear,
      isUpdate,
      sectionName,
      userEnteredRentARoomDetails
    )
  }
}
