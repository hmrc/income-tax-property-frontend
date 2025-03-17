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
import controllers.{PropertyDetailsHandler, routes}
import models.PropertyType
import models.backend.PropertyDetails
import pages.propertyrentals.ClaimPropertyIncomeAllowancePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.{BusinessService, CYADiversionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import viewmodels.AllowancesStartPage
import views.html.allowances.AllowancesStartView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AllowancesStartController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  diversionService: CYADiversionService,
  val controllerComponents: MessagesControllerComponents,
  view: AllowancesStartView,
  businessService: BusinessService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with PropertyDetailsHandler {

  def onPageLoad(taxYear: Int, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      withUkPropertyDetails[Result](businessService, request.user.nino, request.user.mtditid) {
        (propertyDetails: PropertyDetails) =>
          val isPIA: Boolean = request.userAnswers
            .get(ClaimPropertyIncomeAllowancePage(propertyType))
            .getOrElse(false)
          propertyDetails.accrualsOrCash match {
            case Some(true) =>
              Future(
                Ok(
                  view(
                    AllowancesStartPage(
                      taxYear,
                      request.user.isAgentMessageKey,
                      cashOrAccruals = true,
                      request.userAnswers,
                      propertyType
                    ),
                    isPIA
                  )
                )
              )
            case Some(false) =>
              Future(
                Ok(
                  view(
                    AllowancesStartPage(
                      taxYear,
                      request.user.isAgentMessageKey,
                      cashOrAccruals = false,
                      request.userAnswers,
                      propertyType
                    ),
                    isPIA
                  )
                )
              )
            case _ =>
              Future(
                Redirect(routes.JourneyRecoveryController.onPageLoad())
              )
          }
      }(hc, ec)
    }

}
