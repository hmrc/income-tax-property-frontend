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

package pages.foreignincome.dividends

import models.{ForeignIncome, UserAnswers}
import pages.PageConstants.foreignDividendsPath
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.Try

case class ForeignTaxDeductedFromDividendIncomePage(countryCode: String) extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ foreignDividendsPath(ForeignIncome) \ countryCode.toUpperCase \ toString

  override def toString: String = "foreignTaxDeductedFromDividendIncome"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    if(value.contains(false)){
      for {
        howMuchForeignTaxDeducted <- userAnswers.remove(HowMuchForeignTaxDeductedFromDividendIncomePage(countryCode))
        claimForeignTaxCreditRelief <- howMuchForeignTaxDeducted.remove(ClaimForeignTaxCreditReliefPage(countryCode))
      } yield claimForeignTaxCreditRelief
    } else {
      super.cleanup(value, userAnswers)
    }

  }
}
