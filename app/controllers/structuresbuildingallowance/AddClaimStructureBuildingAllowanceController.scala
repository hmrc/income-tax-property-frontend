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

package controllers.structuresbuildingallowance

import controllers.actions._
import controllers.routes
import models.backend.PropertyDetails
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.BusinessService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.StructureBuildingAllowancePage
import views.html.structurebuildingallowance.StructureBuildingAllowanceView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AddClaimStructureBuildingAllowanceController @Inject()(override val messagesApi: MessagesApi,
                                                             identify: IdentifierAction,
                                                             val controllerComponents: MessagesControllerComponents,
                                                             view: StructureBuildingAllowanceView,
                                                             businessService: BusinessService)
                                                            (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = identify.async {
    implicit request =>
      businessService.getBusinessDetails(request.user).map {
        case Right(businessDetails) if businessDetails.propertyData.exists(existsUkProperty) =>
          Ok(view(StructureBuildingAllowancePage(taxYear, request.user.isAgentMessageKey)))
        case _ => Redirect(routes.SummaryController.show(taxYear))
      }
  }

  def existsUkProperty(property: PropertyDetails): Boolean =
    property.incomeSourceType.contains("uk-property") && property.tradingStartDate.isDefined && property.cashOrAccruals.isDefined
}
