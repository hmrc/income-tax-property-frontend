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

package pages.foreign

import models.ForeignTotalIncome.{LessThanOneThousand, OneThousandAndMore}
import models.{ForeignProperty, ForeignTotalIncome, UserAnswers}
import pages.PageConstants.selectCountryPath
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.Try

case object TotalIncomePage extends QuestionPage[ForeignTotalIncome] {

  override def path: JsPath = JsPath \ selectCountryPath(ForeignProperty) \ toString

  override def toString: String = "totalIncome"

  override def cleanup(maybeTotalIncome: Option[ForeignTotalIncome], userAnswers: UserAnswers): Try[UserAnswers] = {
    val updatedAnswers = maybeTotalIncome
      .filter(income => income == OneThousandAndMore)
      .map(_ => userAnswers.remove(IncomeSourceCountries))
      .map(_ => userAnswers.remove(PropertyIncomeReportPage))
      .getOrElse(super.cleanup(maybeTotalIncome, userAnswers))

    maybeTotalIncome match {
      case Some(LessThanOneThousand) =>
        for {
          answers <- updatedAnswers
        } yield answers

      case _ => super.cleanup(maybeTotalIncome, userAnswers)
    }
  }

}
