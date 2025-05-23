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

package controllers.adjustments

import controllers.actions._
import controllers.adjustments.routes.{PrivateUseAdjustmentController, UnusedLossesBroughtForwardController}
import models.{NormalMode, Rentals}
import pages.isUkAndForeignAboutJourneyComplete
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.CYADiversionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.adjustments.AdjustmentsStartView

import javax.inject.Inject

class AdjustmentsStartController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  diversionService: CYADiversionService,
  val controllerComponents: MessagesControllerComponents,
  view: AdjustmentsStartView
) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, expensesOrPIA: Boolean): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val isUkAndForeignJourney: Boolean = isUkAndForeignAboutJourneyComplete(request.userAnswers)
      val continueLink = if(isUkAndForeignJourney && expensesOrPIA){
        UnusedLossesBroughtForwardController.onPageLoad(taxYear, NormalMode, Rentals).url
      } else {
        PrivateUseAdjustmentController.onPageLoad(taxYear, NormalMode, Rentals).url
      }
      Ok(view(taxYear, expensesOrPIA, request.user.isAgentMessageKey, isUkAndForeignJourney, continueLink))
    }
}
