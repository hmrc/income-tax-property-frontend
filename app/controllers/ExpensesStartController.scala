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

package controllers

import controllers.actions._
import models.requests.DataRequest
import pages.premiumlease.PremiumsGrantLeasePage
import pages.{CalculatedFigureYourselfPage, IncomeFromPropertyRentalsPage, OtherIncomeFromPropertyPage, ReversePremiumsReceivedPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ExpensesStartView

import javax.inject.Inject

class ExpensesStartController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: ExpensesStartView
                                       ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val totalIncomeCapped = 85000
      val isUnder85K = totalIncome(request) < BigDecimal(totalIncomeCapped)
      Ok(view(taxYear, request.user.isAgentMessageKey, isUnder85K))
  }

  private def totalIncome(request: DataRequest[AnyContent]): BigDecimal = {
    val incomeFromPropertyRentals = request.userAnswers.get(IncomeFromPropertyRentalsPage).getOrElse(BigDecimal(0))
    val leasePremiumCalculated = request.userAnswers.get(CalculatedFigureYourselfPage).flatMap(cf => cf.amount).getOrElse(BigDecimal(0))
    val reversePremiumsReceived = request.userAnswers.get(ReversePremiumsReceivedPage).flatMap(cf => cf.amount).getOrElse(BigDecimal(0))
    val premiumsGrantLease = request.userAnswers.get(PremiumsGrantLeasePage).getOrElse(BigDecimal(0))
    val otherIncome = request.userAnswers.get(OtherIncomeFromPropertyPage).map(i => i.amount).getOrElse(BigDecimal(0))

    incomeFromPropertyRentals + leasePremiumCalculated + premiumsGrantLease + reversePremiumsReceived + otherIncome

  }
}
