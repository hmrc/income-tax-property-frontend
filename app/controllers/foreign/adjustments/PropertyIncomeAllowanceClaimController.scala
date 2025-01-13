/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.foreign.adjustments

import controllers.actions._
import forms.foreign.adjustments.PropertyIncomeAllowanceClaimFormProvider
import views.html.foreign.adjustments.PropertyIncomeAllowanceClaimView
import models.ForeignTotalIncomeUtils.{incomeAndBalancingChargeCombined, maxAllowedPIA}
import javax.inject.Inject
import models.Mode
import navigation.ForeignPropertyNavigator
import pages.foreign.adjustments.PropertyIncomeAllowanceClaimPage
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class PropertyIncomeAllowanceClaimController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: ForeignPropertyNavigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: PropertyIncomeAllowanceClaimFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: PropertyIncomeAllowanceClaimView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, countryCode: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val combinedAmount = incomeAndBalancingChargeCombined(request.userAnswers, countryCode)
      val form = formProvider(request.user.isAgentMessageKey, combinedAmount)
      val preparedForm = request.userAnswers.get(PropertyIncomeAllowanceClaimPage(countryCode)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, taxYear, countryCode, request.user.isAgentMessageKey, maxAllowedPIA(combinedAmount), mode))
  }

  def onSubmit(taxYear: Int, countryCode: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val combinedAllowance = incomeAndBalancingChargeCombined(request.userAnswers, countryCode)
      val form = formProvider(request.user.isAgentMessageKey, combinedAllowance)
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, taxYear, countryCode, request.user.isAgentMessageKey, maxAllowedPIA(combinedAllowance), mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PropertyIncomeAllowanceClaimPage(countryCode), value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(PropertyIncomeAllowanceClaimPage(countryCode), taxYear, mode, request.userAnswers, updatedAnswers))
      )
  }
}
