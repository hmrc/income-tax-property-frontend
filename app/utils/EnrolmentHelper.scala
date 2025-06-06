/*
 * Copyright 2025 HM Revenue & Customs
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

package utils

import common.{DelegatedAuthRules, EnrolmentIdentifiers, EnrolmentKeys}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments, Enrolment => HMRCEnrolment}

object EnrolmentHelper {

  def agentAuthPredicate(mtdId: String): Predicate =
    Enrolment(EnrolmentKeys.Individual)
      .withIdentifier(EnrolmentIdentifiers.individualId, mtdId)
      .withDelegatedAuthRule(DelegatedAuthRules.agentDelegatedAuthRule)

  def secondaryAgentPredicate(mtdId: String): Predicate =
    Enrolment(EnrolmentKeys.Supporting)
      .withIdentifier(EnrolmentIdentifiers.individualId, mtdId)
      .withDelegatedAuthRule(DelegatedAuthRules.supportingAgentDelegatedAuthRule)

  def getEnrolmentValueOpt(checkedKey: String,
                           checkedIdentifier: String,
                           enrolments: Enrolments): Option[String] =
    enrolments.enrolments.collectFirst {
      case HMRCEnrolment(`checkedKey`, enrolmentIdentifiers, _, _) => enrolmentIdentifiers.collectFirst {
        case EnrolmentIdentifier(`checkedIdentifier`, identifierValue) => identifierValue
      }
    }.flatten
}
