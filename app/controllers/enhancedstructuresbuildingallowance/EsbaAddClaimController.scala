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
                                       requireData: DataRequiredAction,
                                       getData: DataRetrievalAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: EsbaAddClaimView)
                                      (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val numberOfEsbaEntries = request.userAnswers.get(EsbaInfo(taxYear)).fold(0)(_.length)
      Ok(view(EsbaAddClaimPage(taxYear, request.user.isAgentMessageKey), numberOfEsbaEntries))
  }
}
