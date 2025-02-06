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

package pages.ukandforeignproperty

import models.{UKAndForeignProperty, UserAnswers}
import pages.PageConstants.aboutPath
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.Try

case object ForeignPremiumsForTheGrantOfALeasePage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ aboutPath(UKAndForeignProperty) \ toString

  override def toString: String = "foreignPremiumsForTheGrantOfALease"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    value
      .map {
        case true => super.cleanup(value, userAnswers)
        case false =>
          for {
            calcPremiums <- userAnswers.remove(UkAndForeignCalculatedForeignPremiumGrantLeaseTaxablePage)
            leaseAmount  <- calcPremiums.remove(ForeignLeaseGrantAmountReceivedPage)
            leaseYears   <- leaseAmount.remove(ForeignYearLeaseAmountPage)
            premiums     <- leaseYears.remove(UkAndForeignPropertyForeignPremiumsGrantLeasePage)
          } yield premiums
      }
      .getOrElse(super.cleanup(value, userAnswers))
}
