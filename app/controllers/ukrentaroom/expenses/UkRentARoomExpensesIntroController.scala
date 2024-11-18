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

package controllers.ukrentaroom.expenses

import controllers.actions._
import controllers.routes
import models.TotalIncomeUtils.isTotalIncomeUnder85K
import models.{NormalMode, RentARoom, TotalIncome}
import pages.TotalIncomePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.CYADiversionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ukrentaroom.UkRentARoomExpensesIntroView

import javax.inject.Inject

class UkRentARoomExpensesIntroController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  diversionService: CYADiversionService,
  val controllerComponents: MessagesControllerComponents,
  view: UkRentARoomExpensesIntroView
) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val under85KUrl =
        if (isTotalIncomeUnder85K(request.userAnswers, RentARoom)) {
          controllers.ukrentaroom.expenses.routes.ConsolidatedExpensesRRController
            .onPageLoad(taxYear, NormalMode)
            .url
        } else {
          controllers.ukrentaroom.expenses.routes.RentsRatesAndInsuranceRRController
            .onPageLoad(taxYear, NormalMode)
            .url
        }
      request.userAnswers.get(TotalIncomePage) match {
        case None        => Redirect(routes.JourneyRecoveryController.onPageLoad())
        case Some(value) => Ok(view(value != TotalIncome.Over, under85KUrl))
      }
  }
}
