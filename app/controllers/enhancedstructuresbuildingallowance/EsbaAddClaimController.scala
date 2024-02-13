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

package controllers.enhancedstructuresbuildingallowance

import controllers.actions._
import models.requests.OptionalDataRequest
import pages.enhancedstructuresbuildingallowance.EsbaInfo
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.enhancedstructurebuildingallowance.EsbaAddClaimPage
import views.html.enhancedstructuresbuildingallowance.EsbaAddClaimView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class EsbaAddClaimController @Inject()(override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: EsbaAddClaimView)
                                      (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData) {
    implicit request =>
      withNumberOfEsbaEntriesSoFar(taxYear) { numberOfEsbaEntriesSoFar =>
        Ok(view(EsbaAddClaimPage(taxYear, request.user.isAgentMessageKey), numberOfEsbaEntriesSoFar))
      }
  }

  private def withNumberOfEsbaEntriesSoFar[T, U](taxYear: Int)(block: Int => U)(implicit request: OptionalDataRequest[T]): U = {
    val number: Int = request.userAnswers.map(ua => {
      ua.get(EsbaInfo(taxYear)).fold(0)(_.size)
    }).getOrElse(0)
    block(number)
  }
}
