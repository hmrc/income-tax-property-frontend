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
import controllers.session.SessionRecovery
import models.requests.OptionalDataRequest
import pages._
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.BusinessService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.SummaryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SummaryController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  sessionRecovery: SessionRecovery,
  view: SummaryView,
  businessService: BusinessService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def show(taxYear: Int): Action[AnyContent] = (identify andThen getData).async { implicit requestBeforeUpdate =>
    withUpdatedData(taxYear) { request =>
      val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      businessService.getUkPropertyDetails(request.user.nino, request.user.mtditid)(hc).flatMap {
        case Right(Some(propertyData)) =>
          val propertyRentalsRows =
            SummaryPage.createUkPropertyRows(request.userAnswers, taxYear, propertyData.cashOrAccruals.get)
          val ukRentARoomRows = SummaryPage.createUkRentARoomRows(request.userAnswers, taxYear)
          val startItems = SummaryPage.propertyAboutItems(request.userAnswers, taxYear)
          val combinedItems = SummaryPage.createRentalsAndRentARoomRows(request.userAnswers, taxYear, propertyData.cashOrAccruals.get)
          Future.successful(
            Ok(view(taxYear, startItems, propertyRentalsRows, ukRentARoomRows, combinedItems))
          )
        case _ =>
          Future.failed(PropertyDataError)
      }
    }(requestBeforeUpdate, controllerComponents.executionContext, hc)
  }

  private def withUpdatedData(taxYear: Int)(
    block: OptionalDataRequest[AnyContent] => Future[Result]
  )(implicit request: OptionalDataRequest[AnyContent], ec: ExecutionContext, hc: HeaderCarrier): Future[Result] =
    sessionRecovery.withUpdatedData(taxYear)(block)

}

case object PropertyDataError extends Exception("Encountered an issue retrieving property data from the business API")
