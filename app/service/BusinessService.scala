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

package service

import connectors.BusinessConnector
import connectors.error.ApiError
import models.{User, UserAnswers}
import models.backend.{BusinessDetails, HttpParserError}
import models.requests.DataRequest
import pages.adjustments.BalancingChargePage
import pages.premiumlease.PremiumsGrantLeasePage
import pages.{CalculatedFigureYourselfPage, IncomeFromPropertyRentalsPage, OtherIncomeFromPropertyPage, ReversePremiumsReceivedPage}
import play.api.Logging
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessService @Inject()(businessConnector: BusinessConnector)
                               (implicit val ec: ExecutionContext) extends Logging {

  def getBusinessDetails(user: User)(implicit hc: HeaderCarrier): Future[Either[HttpParserError, BusinessDetails]] = {
    businessConnector.getBusinessDetails(user).map {
      case Left(error: ApiError) => Left(HttpParserError(error.status))
      case Right(businessDetails) => Right(businessDetails)
    }
  }

  def totalIncome(userAnswers: UserAnswers): BigDecimal = {
    val incomeFromPropertyRentals = userAnswers.get(IncomeFromPropertyRentalsPage).getOrElse(BigDecimal(0))
    val leasePremiumCalculated = userAnswers.get(CalculatedFigureYourselfPage).flatMap(_.amount).getOrElse(BigDecimal(0))
    val reversePremiumsReceived = userAnswers.get(ReversePremiumsReceivedPage).flatMap(_.amount).getOrElse(BigDecimal(0))
    val premiumsGrantLease = userAnswers.get(PremiumsGrantLeasePage).getOrElse(BigDecimal(0))
    val otherIncome = userAnswers.get(OtherIncomeFromPropertyPage).map(_.amount).getOrElse(BigDecimal(0))

    incomeFromPropertyRentals + leasePremiumCalculated + premiumsGrantLease + reversePremiumsReceived + otherIncome
  }

  def maxPropertyIncomeAllowanceCombined(userAnswers: UserAnswers): BigDecimal = {
    val balancingCharge = userAnswers.get(BalancingChargePage).flatMap(_.balancingChargeAmount).getOrElse(BigDecimal(0))
    totalIncome(userAnswers) + balancingCharge
  }

}
