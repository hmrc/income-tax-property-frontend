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

import models.AccountingMethod.Traditional
import models.{AuditPropertyType, JourneyName, SectionName, TotalIncome, UKPropertySelect, PropertyAbout}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{times, verify}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.Country
import play.api.libs.json.Writes
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import audit.{PropertyAbout => PropertyAboutAudit}


import scala.concurrent.ExecutionContext.Implicits.global

class AuditServiceSpec extends AnyWordSpec with MockitoSugar {

  val mockAuditConnector: AuditConnector = mock[AuditConnector]
  val service = new AuditService(mockAuditConnector)

  "trigger audit event" in {
    val hc = HeaderCarrier()
    val propertyAbout = PropertyAbout(TotalIncome.Under, Some(UKPropertySelect.values), Some(true))
    val auditModel = AuditModel(
      "Agent",
      "NINO",
      "mtdItId",
      2024,
      propertyType = AuditPropertyType.UKProperty,
      countryCode = Country.UK.code,
      journeyName = JourneyName.Rentals,
      sectionName = SectionName.About,
      accountingMethod = Traditional,
      isUpdate = false,
      isFailed = false,
      agentReferenceNumber = Some("agentReferenceNumber"),
      PropertyAboutAudit(propertyAbout)
    )

    service.sendRentalsAuditEvent(auditModel)(hc, implicitly[Writes[AuditModel[PropertyAboutAudit]]])

    verify(mockAuditConnector, times(1))
      .sendExplicitAudit(eqTo("CreateOrAmendRentalsUpdate"), eqTo(auditModel))(eqTo(hc), any(), any())

  }

}
