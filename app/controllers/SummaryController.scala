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

package controllers

import controllers.actions.{DataRetrievalAction, IdentifierAction}
import pages.SummaryPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SummaryView
import service.BusinessService
import models.backend.PropertyDetails

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SummaryController @Inject()(
                                 val controllerComponents: MessagesControllerComponents,
                                 identify: IdentifierAction,
                                 getData: DataRetrievalAction,
                                 view: SummaryView,
                                 businessService: BusinessService
                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def show(taxYear: Int): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      businessService.getBusinessDetails(request.user).map {
        case Right(businessDetails) if businessDetails.propertyData.exists(existsUkProperty) =>
          val propertyData = businessDetails.propertyData.find(existsUkProperty).get
          Ok(view(taxYear, SummaryPage.createUkPropertyRows(request.userAnswers, taxYear, propertyData.cashOrAccruals.get)))
        case _ => Redirect(routes.SummaryController.show(taxYear))
      }
  }

      private def existsUkProperty(property: PropertyDetails): Boolean =
        property.incomeSourceType.contains("uk-property") && property.tradingStartDate.isDefined && property.cashOrAccruals.isDefined
  }

