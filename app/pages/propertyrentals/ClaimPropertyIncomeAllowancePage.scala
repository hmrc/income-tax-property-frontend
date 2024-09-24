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

package pages.propertyrentals

import models.{PropertyType, RentalsRentARoom, UserAnswers}
import pages.PageConstants.aboutPath
import pages.QuestionPage
import pages.adjustments._
import pages.rentalsandrentaroom.adjustments.RentalsRaRAdjustmentsCompletePage
import play.api.libs.json.JsPath

import scala.util.Try

case class ClaimPropertyIncomeAllowancePage(propertyType: PropertyType) extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ aboutPath(propertyType) \ toString

  override def toString: String = "claimPropertyIncomeAllowanceYesOrNo"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers
      .remove(PrivateUseAdjustmentPage(RentalsRentARoom))
      .flatMap(_.remove(BalancingChargePage(RentalsRentARoom)))
      .flatMap(_.remove(PropertyIncomeAllowancePage(RentalsRentARoom)))
      .flatMap(_.remove(RenovationAllowanceBalancingChargePage(RentalsRentARoom)))
      .flatMap(_.remove(ResidentialFinanceCostPage(RentalsRentARoom)))
      .flatMap(_.remove(UnusedResidentialFinanceCostPage(RentalsRentARoom)))
      .flatMap(_.remove(RentalsRaRAdjustmentsCompletePage))

}
