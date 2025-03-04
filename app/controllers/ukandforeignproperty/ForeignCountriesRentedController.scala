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
import forms.ukandforeignproperty.ForeignCountriesRentedFormProvider
import models.{Index, Mode, UserAnswers}
import navigation.UkAndForeignPropertyNavigator
import pages.ukandforeignproperty.{ForeignCountriesRentedPage, SelectCountryPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.ukandforeignproperty.SelectCountrySummary
import viewmodels.govuk.summarylist._
import views.html.ukandforeignproperty.ForeignCountriesRentedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignCountriesRentedController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: UkAndForeignPropertyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ForeignCountriesRentedFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ForeignCountriesRentedView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val list: SummaryList = summaryList(taxYear, request.userAnswers)

      // TODO: Update to call backend instead when completing backend tickets
      request.userAnswers.get(SelectCountryPage) match {
        case Some(countries) if countries.nonEmpty =>
          // Don't populate form, as the answer could change each time the user visits the page
          Ok(view(form, list, taxYear, request.user.isAgentMessageKey, mode))
        case _ =>
          Redirect(routes.SelectCountryController.onPageLoad(taxYear, Index(1), mode))
      }

  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val list: SummaryList = summaryList(taxYear, request.userAnswers)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, list, taxYear, request.user.isAgentMessageKey, mode))),
          addAnotherCountry =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ForeignCountriesRentedPage, addAnotherCountry))
              countries      <- Future(request.userAnswers.get(SelectCountryPage).getOrElse(List.empty))
              nextIndex = countries.size
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator
                .nextIndex(ForeignCountriesRentedPage, taxYear, mode, request.userAnswers, updatedAnswers, nextIndex)
            )
        )
  }

  private def summaryList(taxYear: Int, userAnswers: UserAnswers)(implicit messages: Messages) = {
    val rows = userAnswers.get(SelectCountryPage) match {
      case Some(countries) =>
        countries.zipWithIndex.map { case (cty, idx) =>
          SelectCountrySummary.row(taxYear, Index(idx + 1), cty.name)
        }
      case None => Nil
    }

    SummaryListViewModel(rows)
  }
}
