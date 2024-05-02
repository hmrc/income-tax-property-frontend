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

package controllers.allowances

import controllers.actions._
import controllers.routes
import models.backend.PropertyDetails
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.BusinessService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import viewmodels.AllowancesStartPage
import views.html.allowances.AllowancesStartView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AllowancesStartController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  view: AllowancesStartView,
  businessService: BusinessService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = identify.async { implicit request =>
    val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    businessService.getBusinessDetails(request.user)(hc).map {
      case Right(businessDetails) if businessDetails.propertyData.exists(existsUkProperty) =>
        val propertyData = businessDetails.propertyData.find(existsUkProperty).get
        Ok(view(AllowancesStartPage(taxYear, request.user.isAgentMessageKey, propertyData.cashOrAccruals.get)))
      case _ => Redirect(routes.SummaryController.show(taxYear))
    }
  }

  private def existsUkProperty(property: PropertyDetails): Boolean =
    property.incomeSourceType.contains(
      "uk-property"
    ) && property.tradingStartDate.isDefined && property.cashOrAccruals.isDefined
}
