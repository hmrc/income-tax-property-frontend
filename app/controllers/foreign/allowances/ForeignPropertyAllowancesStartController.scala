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

package controllers.foreign.allowances

import controllers.{PropertyDataError, PropertyDetailsHandler, routes}
import controllers.actions._
import controllers.exceptions.InternalErrorFailure
import models.backend.PropertyDetails
import pages.foreign.{Country, IncomeSourceCountries}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.BusinessService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import viewmodels.{AllowancesStartPage, PropertyDetailsPage}
import views.html.JourneyRecoveryStartAgainView
import views.html.foreign.allowances.ForeignPropertyAllowancesStartView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class ForeignPropertyAllowancesStartController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       foreignNavigator: ForeignPropertyNavigator,
                                       requireData: DataRequiredAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: ForeignPropertyAllowancesStartView,
                                       businessService: BusinessService
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with PropertyDetailsHandler {

  def onPageLoad(taxYear: Int, countryCode: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val maybeCountryName = request.userAnswers.get(IncomeSourceCountries).flatMap(_.find(_.code==countryCode)).map(_.name)
      val countryName = maybeCountryName.getOrElse("")

      val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      withUkPropertyDetails[Result](businessService, request.user.nino, request.user.mtditid) {
        (propertyData: PropertyDetails) =>
          propertyData.accrualsOrCash match {
            case Some(true) =>
              Future.successful(
                Ok(view(taxYear, countryCode, countryName, request.user.isAgentMessageKey, accrualsOrCash = true))
              )
            case Some(false) =>
              Future.successful(
                Ok(view(taxYear, countryCode, countryName, request.user.isAgentMessageKey, accrualsOrCash = false))
              )
            case _ =>
              Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
          }
        }(hc, ec)
  }

  def onSubmit(taxYear: Int, countryCode: String, accrualsOrCash: Boolean, mode: Mode): Action[AnyContent] = identify {
    implicit request =>
      if (accrualsOrCash) {
        Redirect(controllers.foreign.allowances.routes.ForeignZeroEmissionGoodsVehiclesController.onPageLoad(taxYear, countryCode, mode))
      } else {
        Redirect(controllers.foreign.allowances.routes.ForeignZeroEmissionGoodsVehiclesController.onPageLoad(taxYear, countryCode, mode))
      }
  }

  def nextPage(taxYear: Int, countryCode: String, accrualsOrCash: Boolean, mode: Mode): Action[AnyContent] = identify {
    implicit request =>
    if (accrualsOrCash) {
      Redirect(controllers.foreign.allowances.routes.ForeignZeroEmissionCarAllowanceController.onPageLoad(taxYear, countryCode, mode))
    } else {
      Redirect(routes.JourneyRecoveryController.onPageLoad())
    }
  }
}
