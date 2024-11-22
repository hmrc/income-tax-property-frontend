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
import pages.foreign.{ForeignPropertySummaryPage, IncomeSourceCountries}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.{BusinessService, CYADiversionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SummaryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SummaryController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  sessionRecovery: SessionRecovery,
  cyaDiversionService: CYADiversionService,
  view: SummaryView,
  businessService: BusinessService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def show(taxYear: Int): Action[AnyContent] = (identify andThen getData).async { implicit requestBeforeUpdate =>
    withUpdatedData(taxYear) { request =>
      businessService.getUkPropertyDetails(request.user.nino, request.user.mtditid)(hc).flatMap {
        case Right(Some(propertyData)) =>
          val propertyRentalsRows =
            SummaryPage(cyaDiversionService)
              .createUkPropertyRows(request.userAnswers, taxYear, propertyData.accrualsOrCash.get)
          val ukRentARoomRows = SummaryPage(cyaDiversionService).createUkRentARoomRows(request.userAnswers, taxYear)
          val startItems = SummaryPage(cyaDiversionService).propertyAboutItems(request.userAnswers, taxYear)
          val combinedItems =
            SummaryPage(cyaDiversionService)
              .createRentalsAndRentARoomRows(request.userAnswers, taxYear, propertyData.accrualsOrCash.get)

          val foreignCountries = request.userAnswers.flatMap(_.get(IncomeSourceCountries)).map(_.array.toList)
          val maybeCountries = foreignCountries.getOrElse(List.empty)
          Future.successful(
            Ok(
              view(
                UKPropertySummaryPage(taxYear, startItems, propertyRentalsRows, ukRentARoomRows, combinedItems),
                ForeignPropertySummaryPage(
                  taxYear = taxYear,
                  startItems = ForeignPropertySummaryPage.foreignPropertyAboutItems(taxYear, request.userAnswers),
                  foreignIncomeCountries = maybeCountries,
                  userAnswers = request.userAnswers
                )
              )
            )
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
