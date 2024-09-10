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

package controllers.enhancedstructuresbuildingallowance

import controllers.actions._
import models.PropertyType
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.enhancedstructurebuildingallowance.ClaimEnhancedSBASummary
import viewmodels.govuk.summarylist._
import views.html.enhancedstructuresbuildingallowance.ClaimEsbaCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.Future

class ClaimEsbaCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ClaimEsbaCheckYourAnswersView
) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, propertyType: PropertyType): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          ClaimEnhancedSBASummary.row(taxYear, request.userAnswers, request.user.isAgentMessageKey)
        ).flatten
      )
      Ok(view(list, taxYear, propertyType))
  }

  def onSubmit(taxYear: Int, propertyType: PropertyType): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      Future.successful(
        Redirect(
          controllers.enhancedstructuresbuildingallowance.routes.EsbaSectionFinishedController.onPageLoad(taxYear, propertyType)
        )
      )
  }
}
