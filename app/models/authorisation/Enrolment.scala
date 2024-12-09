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

package models.authorisation

sealed abstract class Enrolment(val key: String, val value: String)

object Enrolment {
  case object Individual extends Enrolment(key = "HMRC-MTD-IT", value = "MTDITID")
  case object SupportingAgent extends Enrolment(key = "HMRC-MTD-IT-SUPP", value = "MTDITID")
  case object Agent extends Enrolment(key = "HMRC-AS-AGENT", value = "AgentReferenceNumber")
  case object Nino extends Enrolment(key = "HMRC-NI", value = "NINO")
}

