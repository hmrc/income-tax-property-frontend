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

package controllers.ukandforeignproperty

import controllers.actions._
import controllers.exceptions.SaveJourneyAnswersFailed
import forms.ukandforeignproperty.CountriesListFormProvider
import models.JourneyPath.ForeignSelectCountry
import models.requests.DataRequest
import models.{JourneyContext, Mode}
import navigation.Navigator
import play.api.i18n.Lang.logger
import pages.ukandforeignproperty.CountriesListPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.foreign.CountriesRentedPropertySummary
import viewmodels.govuk.summarylist._
import views.html.ukandforeignproperty.CountriesListView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class CountriesListController @Inject() (
                                          override val messagesApi: MessagesApi,
                                          sessionRepository: SessionRepository,
                                          navigator: Navigator,
                                          identify: IdentifierAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: CountriesListFormProvider,
                                          propertySubmissionService: PropertySubmissionService,
                                          val controllerComponents: MessagesControllerComponents,
                                          view: CountriesListView
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val form: Form[Boolean] = formProvider()

  def onPageLoad(taxYear: Int, mode: Mode, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          CountriesRentedPropertySummary.rowList(taxYear, request.userAnswers)
        ).flatten
      )

      val preparedForm = request.userAnswers.get(CountriesListPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, list, taxYear, mode, countryCode))
    }

  def onSubmit(taxYear: Int, mode: Mode, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          CountriesRentedPropertySummary.rowList(taxYear, request.userAnswers)
        ).flatten
      )

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, list, taxYear, mode, countryCode))),

        value => handleValidForm(value, taxYear, mode, request, countryCode)
      )
    }

  private def handleValidForm(
                               addAnotherCountry: Boolean,
                               taxYear: Int,
                               mode: Mode,
                               request: DataRequest[AnyContent],
                               countryCode: String
                             )(implicit hc: HeaderCarrier): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(CountriesListPage, addAnotherCountry))
      _ <- sessionRepository.set(updatedAnswers)
      result <- if (addAnotherCountry) {
        // TODO - next page is back to previous page/uk-foreign-property/select-country/select-country
        Future.successful(Redirect(navigator.nextPage(CountriesListPage, taxYear, mode, request.userAnswers, updatedAnswers)))
      } else {
        saveJourneyAnswers(taxYear, request, countryCode)
      }
    } yield result

  private def saveJourneyAnswers(
                                  taxYear: Int,
                                  request: DataRequest[AnyContent],
                                  countryCode: String
                                )(implicit hc: HeaderCarrier): Future[Result] = {
    val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, ForeignSelectCountry)
    propertySubmissionService.saveJourneyAnswers(context, countryCode).flatMap {
      case Right(_) =>
        //TODO redirect to claim property income allowance or expenses(next page in journey)
        ///uk-foreign-property/select-country/pia-yes-no
        Future.successful(Redirect(controllers.routes.SummaryController.show(taxYear)))
      case Left(error) =>
        logger.error(s"Failed to save where you rent Foreign Countries section : ${error.toString}")
        Future.failed(SaveJourneyAnswersFailed("Failed to save where you rent Foreign Countries section"))
    }
  }
}
