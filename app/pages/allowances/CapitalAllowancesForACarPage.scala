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

package pages.allowances

import models.{CapitalAllowancesForACar, PropertyType, UserAnswers}
import pages.PageConstants.allowancesPath
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.Try

case class CapitalAllowancesForACarPage(propertyType: PropertyType) extends QuestionPage[CapitalAllowancesForACar] {

  override def path: JsPath = JsPath \ allowancesPath(propertyType) \ toString

  override def toString: String = "capitalAllowancesForACar"

  override def cleanup(value: Option[CapitalAllowancesForACar], userAnswers: UserAnswers): Try[UserAnswers] =
    for {
      ua  <- userAnswers.remove(AnnualInvestmentAllowancePage(propertyType))
      ua1 <- ua.remove(BusinessPremisesRenovationPage(propertyType))
      ua2 <- ua1.remove(OtherCapitalAllowancePage(propertyType))
      ua3 <- ua2.remove(ReplacementOfDomesticGoodsPage(propertyType))
      ua4 <- ua3.remove(ZeroEmissionCarAllowancePage(propertyType))
      ua5 <- ua4.remove(ZeroEmissionGoodsVehicleAllowancePage(propertyType))
    } yield ua5
}
