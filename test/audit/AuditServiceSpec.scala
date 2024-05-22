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

import models.{TotalIncome, UKPropertySelect}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{times, verify}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Writes
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext.Implicits.global

class AuditServiceSpec extends AnyWordSpec with MockitoSugar {

  val mockAuditConnector: AuditConnector = mock[AuditConnector]
  val service = new AuditService(mockAuditConnector)

  "trigger audit event" in {
    val hc = HeaderCarrier()
    val propertyAbout = PropertyAbout(TotalIncome.Under, UKPropertySelect.values, Some(true))
    val auditModel = AuditModel(
      "NINO",
      "Agent",
      "mtdItId",
      agentRef = Some("agentReferenceNumber"),
      2024,
      isUpdate = false,
      "PropertyAbout",
      propertyAbout
    )

    service.sendRentalsAuditEvent(auditModel)(hc, implicitly[Writes[AuditModel[PropertyAbout]]])

    verify(mockAuditConnector, times(1))
      .sendExplicitAudit(eqTo("CreateOrAmendRentalsUpdate"), eqTo(auditModel))(eqTo(hc), any(), any())

  }

}
