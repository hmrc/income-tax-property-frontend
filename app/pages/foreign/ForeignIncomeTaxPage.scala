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

import models.{ForeignIncomeTax, ForeignProperty, UserAnswers}
import pages.PageConstants.foreignTaxPath
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.Try

case class ForeignIncomeTaxPage(countryCode: String) extends QuestionPage[ForeignIncomeTax] {

  override def path: JsPath = JsPath \ foreignTaxPath(ForeignProperty) \ countryCode.toUpperCase \ toString

  override def toString: String = "foreignIncomeTax"

  override def cleanup(value: Option[ForeignIncomeTax], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(foreignIncomeTax) if !foreignIncomeTax.foreignIncomeTaxYesNo =>
        userAnswers.remove(ClaimForeignTaxCreditReliefPage(countryCode))
      case _ => super.cleanup(value, userAnswers)
    }
}
