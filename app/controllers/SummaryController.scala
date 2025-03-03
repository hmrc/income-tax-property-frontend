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

package controllers

import controllers.actions.{DataRetrievalAction, IdentifierAction}
import controllers.session.SessionRecovery
import models.IncomeSourcePropertyType.{ForeignProperty, UKProperty}
import models.backend.{NoPropertyDataError, UnexpectedPropertyDataError}
import models.requests.OptionalDataRequest
import pages._
import pages.foreign.{ForeignPropertySummaryPage, ForeignSummaryPage, IncomeSourceCountries}
import pages.ukandforeignproperty.UkAndForeignPropertySummaryPage
import play.api.i18n.I18nSupport
import play.api.i18n.Lang.logger
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.{BusinessService, CYADiversionService, CountryNamesDataSource, ForeignCYADiversionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.language.LanguageUtils
import views.html.SummaryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SummaryController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  sessionRecovery: SessionRecovery,
  cyaDiversionService: CYADiversionService,
  foreignCYADiversionService: ForeignCYADiversionService,
  view: SummaryView,
  businessService: BusinessService,
  languageUtils: LanguageUtils
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def show(taxYear: Int): Action[AnyContent] = (identify andThen getData).async { implicit requestBeforeUpdate =>
    val summaryPage = SummaryPage(cyaDiversionService)
    val foreignSummaryPage = ForeignSummaryPage(foreignCYADiversionService)
    withUpdatedData(taxYear) { request =>
      businessService.getAllPropertyDetails(request.user.nino, request.user.mtditid)(hc).flatMap {

        case Right(data) if data.isEmpty =>
          logger.warn(NoPropertyDataError(request.user.nino, request.user.mtditid).toString)
          Future.failed(PropertyDataError)

        case Right(propertyData) =>
          val ukAccrualsOrCash: Boolean = propertyData
            .find(_.incomeSourceType.contains(UKProperty.toString))
            .flatMap(_.accrualsOrCash)
            .getOrElse(true)
          val foreignAccrualsOrCash: Boolean = propertyData
            .find(_.incomeSourceType.contains(ForeignProperty.toString))
            .flatMap(_.accrualsOrCash)
            .getOrElse(true)

          val propertyRentalsRows =
            summaryPage
              .createUkPropertyRows(request.userAnswers, taxYear, ukAccrualsOrCash)
          val ukRentARoomRows = summaryPage.createUkRentARoomRows(request.userAnswers, taxYear)
          val startItems = summaryPage.propertyAboutItems(request.userAnswers, taxYear)
          val combinedItems =
            summaryPage
              .createRentalsAndRentARoomRows(request.userAnswers, taxYear, ukAccrualsOrCash)
          val foreignCountries = request.userAnswers
            .flatMap(_.get(IncomeSourceCountries))
            .map(_.array.toList.flatMap { country =>
              CountryNamesDataSource.getCountry(country.code, languageUtils.getCurrentLang.locale.toString)
            })
          val maybeCountries = foreignCountries.getOrElse(List.empty)
          val foreignPropertyItems = maybeCountries.map { country =>
            country.code -> foreignSummaryPage
              .foreignPropertyItems(taxYear, foreignAccrualsOrCash, country.code, request.userAnswers)
          }.toMap
          Future.successful(
            Ok(
              view(
                UKPropertySummaryPage(taxYear, startItems, propertyRentalsRows, ukRentARoomRows, combinedItems),
                ForeignPropertySummaryPage(
                  taxYear = taxYear,
                  startItems = foreignSummaryPage.foreignPropertyAboutItems(taxYear, request.userAnswers),
                  foreignPropertyItems = foreignPropertyItems,
                  foreignIncomeCountries = maybeCountries,
                  userAnswers = request.userAnswers
                ),
                UkAndForeignPropertySummaryPage(
                  taxYear = taxYear,
                  startItems = UkAndForeignPropertySummaryPage.ukAndForeignPropertyAboutItems(
                    taxYear,
                    request.userAnswers,
                    cyaDiversionService,
                    foreignCYADiversionService
                  )
                )
              )
            )
          )
        case ex =>
          logger.error(UnexpectedPropertyDataError(request.user.nino, request.user.mtditid, ex).toString)
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
