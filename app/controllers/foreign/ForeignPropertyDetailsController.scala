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

package controllers.foreign

import controllers.actions._
import controllers.exceptions.InternalErrorFailure
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.BusinessService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.PropertyDetailsPage
import views.html.ForeignPropertyDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignPropertyDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  view: ForeignPropertyDetailsView,
  businessService: BusinessService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = identify.async { implicit request =>
    businessService.getForeignPropertyDetails(request.user.nino, request.user.mtditid).flatMap {
      case Right(Some(propertyData)) =>
        Future.successful(
          Ok(
            view(
              PropertyDetailsPage(
                taxYear,
                request.user.isAgentMessageKey,
                propertyData.tradingStartDate.get,
                propertyData.accrualsOrCash.get
              )
            )
          )
        )
      case _ => Future.failed(InternalErrorFailure("Foreign property details not found from 1171 API"))
    }
  }
}
