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

package pages.foreign.income

import models.{ForeignProperty, UserAnswers}
import pages.PageConstants.incomePath
import pages.QuestionPage
import pages.foreign._
import play.api.libs.json.JsPath

import scala.util.Try

case class PremiumsGrantLeaseYNPage(countryCode: String) extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ incomePath(ForeignProperty) \ countryCode.toUpperCase \ toString

  override def toString: String = "premiumsGrantLeaseReceived"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    value
      .map {
        case true => super.cleanup(value, userAnswers)
        case false =>
          for {
            userAnswersWithoutCPLT  <- userAnswers.remove(CalculatedPremiumLeaseTaxablePage(countryCode))
            userAnswersWithoutFRGLA <- userAnswersWithoutCPLT.remove(ForeignReceivedGrantLeaseAmountPage(countryCode))
            userAnswersWithoutFYLA  <- userAnswersWithoutFRGLA.remove(TwelveMonthPeriodsInLeasePage(countryCode))
            userAnswersWithoutFPGL  <- userAnswersWithoutFYLA.remove(ForeignPremiumsGrantLeasePage(countryCode))
          } yield userAnswersWithoutFPGL
      }
      .getOrElse(super.cleanup(value, userAnswers))
}
