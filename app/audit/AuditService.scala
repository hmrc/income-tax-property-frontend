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

package audit

import play.api.libs.json.Writes
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AuditService @Inject() (auditConnector: AuditConnector)(implicit ec: ExecutionContext) {

  private val rentalsAuditType = "CreateOrAmendRentalsUpdate"
  private val rentARoomAuditType = "CreateOrAmendRentARoomUpdate"
  private val auditType = "CreateOrAmendPropertyDetails"

  def sendRentalsAuditEvent[T](
    event: AuditModel[T]
  )(implicit hc: HeaderCarrier, writes: Writes[AuditModel[T]]): Unit =
    auditConnector.sendExplicitAudit(rentalsAuditType, event)

  def sendRentARoomAuditEvent[T](
    event: RentARoomAuditModel[T]
  )(implicit hc: HeaderCarrier, writes: Writes[RentARoomAuditModel[T]]): Unit =
    auditConnector.sendExplicitAudit(rentARoomAuditType, event)

  def sendAuditEvent[T](
    event: AuditModel[T]
  )(implicit hc: HeaderCarrier, writes: Writes[AuditModel[T]]): Unit =
    auditConnector.sendExplicitAudit(auditType, event)

}
