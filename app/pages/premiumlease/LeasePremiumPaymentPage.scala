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

package pages.premiumlease

import models.{PropertyType, UserAnswers}
import pages.PageConstants.incomePath
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.Try

case class PremiumForLeasePage(propertyType: PropertyType) extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ incomePath(propertyType) \ toString

  override def toString: String = "premiumForLeaseYesOrNo"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    value
      .map {
        case true => super.cleanup(value, userAnswers)
        case false =>
          for {
            rGLAP <- userAnswers.remove(ReceivedGrantLeaseAmountPage(propertyType))
            yLAP  <- rGLAP.remove(YearLeaseAmountPage(propertyType))
            pGLP  <- yLAP.remove(PremiumsGrantLeasePage(propertyType))
            cFYP  <- pGLP.remove(CalculatedFigureYourselfPage(propertyType))
          } yield cFYP
      }
      .getOrElse(super.cleanup(value, userAnswers))
}
