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

package controllers.foreign.structuresbuildingallowance

import controllers.actions._
import pages.foreign.structurebuildingallowance.{ForeignStructureBuildingAllowanceGroup, ForeignStructureBuildingAllowancePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.foreign.structurebuildingallowance.ForeignStructureBuildingAllowanceView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ForeignAddClaimStructureBuildingAllowanceController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ForeignStructureBuildingAllowanceView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val nextIndex =
        request.userAnswers.get(ForeignStructureBuildingAllowanceGroup(countryCode)).map(_.length).getOrElse(0)
      Ok(view(ForeignStructureBuildingAllowancePage(taxYear, countryCode, nextIndex, request.user.isAgentMessageKey)))
    }
}
