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

package controllers.ukrentaroom.allowances

import controllers.actions._
import controllers.routes
import models.IncomeSourcePropertyType.UKProperty
import models.backend.PropertyDetails
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.{BusinessService, CYADiversionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import viewmodels.RRAllowancesStartPage
import views.html.ukrentaroom.allowances.RRAllowancesStartView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RRAllowancesStartController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  diversionService: CYADiversionService,
  val controllerComponents: MessagesControllerComponents,
  view: RRAllowancesStartView,
  businessService: BusinessService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      businessService.getBusinessDetails(request.user)(hc).map {
        case Right(businessDetails) if businessDetails.propertyData.exists(existsUkProperty) =>
          val propertyData = businessDetails.propertyData.find(existsUkProperty).get
          Ok(view(RRAllowancesStartPage(taxYear, request.user.isAgentMessageKey, propertyData.accrualsOrCash.get)))
        case _ => Redirect(routes.SummaryController.show(taxYear))
      }
  }

  private def existsUkProperty(property: PropertyDetails): Boolean =
    property.incomeSourceType.contains(
      UKProperty.toString
    ) && property.tradingStartDate.isDefined && property.accrualsOrCash.isDefined
}
