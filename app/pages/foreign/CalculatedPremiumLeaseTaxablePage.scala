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

package pages.foreign

import models.{ForeignProperty, PremiumCalculated, UserAnswers}
import pages.PageConstants.incomePath
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.Try

case class CalculatedPremiumLeaseTaxablePage(countryCode: String) extends QuestionPage[PremiumCalculated] {

  override def path: JsPath = JsPath \ incomePath(ForeignProperty) \ countryCode.toUpperCase \ toString

  override def toString: String = "calculatedPremiumLeaseTaxable"

  override def cleanup(value: Option[PremiumCalculated], userAnswers: UserAnswers): Try[UserAnswers] = {
    val isPremiumCalculated = value.map(_.calculatedPremiumLeaseTaxable)
    isPremiumCalculated
      .map {
        case true =>
          for {
            userAnswersWithoutFRGLA <- userAnswers.remove(ForeignReceivedGrantLeaseAmountPage(countryCode))
            userAnswersWithoutFYLA  <- userAnswersWithoutFRGLA.remove(TwelveMonthPeriodsInLeasePage(countryCode))
            userAnswersWithoutFPGL  <- userAnswersWithoutFYLA.remove(ForeignPremiumsGrantLeasePage(countryCode))
          } yield userAnswersWithoutFPGL
        case false => super.cleanup(value, userAnswers)
      }
      .getOrElse(super.cleanup(value, userAnswers))
  }
}
