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

package controllers.propertyrentals.expenses

import controllers.actions._
import models.TotalIncomeUtils.isTotalIncomeUnder85K
import models.{NormalMode, PropertyType}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.CYADiversionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.propertyrentals.expenses.ExpensesStartView

import javax.inject.Inject

class ExpensesStartController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  diversionService: CYADiversionService,
  val controllerComponents: MessagesControllerComponents,
  view: ExpensesStartView
) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val under85KUrl = if (isTotalIncomeUnder85K(request.userAnswers, propertyType)) {
        routes.ConsolidatedExpensesController.onPageLoad(taxYear, NormalMode, propertyType).url
      } else {
        routes.RentsRatesAndInsuranceController.onPageLoad(taxYear, NormalMode, propertyType).url
      }
      Ok(
        view(
          taxYear,
          request.user.isAgentMessageKey,
          isTotalIncomeUnder85K(request.userAnswers, propertyType),
          under85KUrl
        )
      )
    }
}
