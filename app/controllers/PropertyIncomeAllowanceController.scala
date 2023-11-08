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
import forms.PropertyIncomeAllowanceFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.Navigator
import pages.premiumlease.PremiumsGrantLeasePage
import pages.{BalancingChargePage, CalculatedFigureYourselfPage, IncomeFromPropertyRentalsPage,
  OtherIncomeFromPropertyPage, PropertyIncomeAllowancePage, ReversePremiumsReceivedPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.PropertyIncomeAllowanceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PropertyIncomeAllowanceController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: PropertyIncomeAllowanceFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: PropertyIncomeAllowanceView)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form = formProvider(request.user.isAgentMessageKey, maxAllowanceCombined(request))
      val preparedForm = request.userAnswers.get(PropertyIncomeAllowancePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, taxYear, request.user.isAgentMessageKey))
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider(request.user.isAgentMessageKey, maxAllowanceCombined(request))

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, taxYear, request.user.isAgentMessageKey))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PropertyIncomeAllowancePage, value))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(PropertyIncomeAllowancePage, taxYear, mode, request.userAnswers, updatedAnswers))
      )
  }

  private def maxAllowanceCombined(request: DataRequest[AnyContent]): BigDecimal = {
    val incomeFromPropertyRentals = request.userAnswers.get(IncomeFromPropertyRentalsPage).getOrElse(BigDecimal(0))
    val leasePremiumCalculated = request.userAnswers.get(CalculatedFigureYourselfPage).flatMap(cf => cf.amount).getOrElse(BigDecimal(0))
    val reversePremiumsReceived = request.userAnswers.get(ReversePremiumsReceivedPage).flatMap(cf => cf.amount).getOrElse(BigDecimal(0))
    val premiumsGrantLease = request.userAnswers.get(PremiumsGrantLeasePage).getOrElse(BigDecimal(0))
    val otherIncome = request.userAnswers.get(OtherIncomeFromPropertyPage).map(i => i.amount).getOrElse(BigDecimal(0))
    val balancingCharge = request.userAnswers.get(BalancingChargePage).flatMap(bc => bc.balancingChargeAmount).getOrElse(BigDecimal(0))

    val totalIncome = incomeFromPropertyRentals + leasePremiumCalculated + premiumsGrantLease + reversePremiumsReceived + otherIncome
    totalIncome + balancingCharge
  }
}
