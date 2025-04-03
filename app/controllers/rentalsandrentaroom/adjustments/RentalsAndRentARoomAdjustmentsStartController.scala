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

package controllers.rentalsandrentaroom.adjustments

import controllers.actions._
import controllers.adjustments.routes.PrivateUseAdjustmentController
import controllers.rentalsandrentaroom.adjustments.routes.BusinessPremisesRenovationBalancingChargeController
import models.{NormalMode, RentalsRentARoom}
import pages.isUkAndForeignAboutJourneyComplete
import pages.ukandforeignproperty.UkAndForeignPropertyClaimExpensesOrReliefPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.CYADiversionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rentalsandrentaroom.adjustments.RentalsAndRentARoomAdjustmentsStartView

import javax.inject.Inject

class RentalsAndRentARoomAdjustmentsStartController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  diversionService: CYADiversionService,
  val controllerComponents: MessagesControllerComponents,
  view: RentalsAndRentARoomAdjustmentsStartView
) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, expensesOrPIA: Boolean): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val claimRentARoomRelief = request.userAnswers.get(UkAndForeignPropertyClaimExpensesOrReliefPage).exists(_.isClaimExpensesOrRelief)
      val isUkAndForeignJourney = isUkAndForeignAboutJourneyComplete(request.userAnswers)
      val continueLink = (
        isUkAndForeignJourney,
        expensesOrPIA,
        claimRentARoomRelief
        ) match {
        case (true, true, true) => BusinessPremisesRenovationBalancingChargeController.onPageLoad(taxYear, NormalMode).url
        case (_, _, _) => PrivateUseAdjustmentController.onPageLoad(taxYear, NormalMode, RentalsRentARoom).url
      }
      Ok(view(taxYear, expensesOrPIA, claimRentARoomRelief, isUkAndForeignJourney, continueLink))
    }
}
