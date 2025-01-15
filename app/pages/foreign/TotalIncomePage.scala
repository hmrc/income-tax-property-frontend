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

import models.TotalIncome.{Between, Over, Under}
import models.{ForeignProperty, TotalIncome, UserAnswers}
import pages.PageConstants.selectCountryPath
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.Try

case object TotalIncomePage extends QuestionPage[TotalIncome] {

  override def path: JsPath = JsPath \ selectCountryPath(ForeignProperty) \ toString

  override def toString: String = "totalIncome"

  override def cleanup(maybeTotalIncome: Option[TotalIncome], userAnswers: UserAnswers): Try[UserAnswers] = {
    val updatedAnswers = maybeTotalIncome
      .filter(income => income == Between || income == Over)
      .map(_ => userAnswers.remove(IncomeSourceCountries))
      .map(_ => userAnswers.remove(PropertyIncomeReportPage))
      .getOrElse(super.cleanup(maybeTotalIncome, userAnswers))

    (maybeTotalIncome, userAnswers.get(TotalIncomePage)) match {
      case (Some(Over), Some(Between)) | (Some(Between), Some(Over)) | (Some(Under), Some(Between)) | (Some(Under), Some(Over)) =>
        for {
          answers <- updatedAnswers
        } yield answers

      case _ => super.cleanup(maybeTotalIncome, userAnswers)
    }
  }

}
