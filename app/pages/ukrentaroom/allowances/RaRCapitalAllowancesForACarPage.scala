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

package pages.ukrentaroom.allowances

import models.{CapitalAllowancesForACar, RentARoom, UserAnswers}
import pages.PageConstants.allowancesPath
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.Try

case object RaRCapitalAllowancesForACarPage extends QuestionPage[CapitalAllowancesForACar] {

  override def path: JsPath = JsPath \ allowancesPath(RentARoom) \ toString

  override def toString: String = "capitalAllowancesForACar"

  override def cleanup(value: Option[CapitalAllowancesForACar], userAnswers: UserAnswers): Try[UserAnswers] =
    for {
      ua  <- userAnswers.remove(RaRZeroEmissionCarAllowancePage)
      ua1 <- ua.remove(RaRReplacementsOfDomesticGoodsPage)
      ua2 <- ua1.remove(RaROtherCapitalAllowancesPage)
      ua3 <- ua2.remove(RaRAnnualInvestmentAllowancePage)
      ua4 <- ua3.remove(RaRZeroEmissionGoodsVehicleAllowancePage)
    } yield ua4
}
