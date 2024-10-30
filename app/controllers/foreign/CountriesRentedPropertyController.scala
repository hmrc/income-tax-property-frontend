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
import forms.foreign.CountriesRentedPropertyFormProvider

import javax.inject.Inject
import models.{UserAnswers, Mode}
import navigation.Navigator
import pages.foreign.{CountriesRentedPropertyPage, CountryGroup}
import play.api.i18n.{MessagesApi, Messages, I18nSupport}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.foreign.CountriesRentedPropertyView
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.summarylist._
import viewmodels.checkAnswers.foreign.CountriesRentedPropertySummary

import scala.concurrent.{ExecutionContext, Future}

class CountriesRentedPropertyController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: CountriesRentedPropertyFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: CountriesRentedPropertyView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val list: SummaryList = summaryList(taxYear, request.userAnswers)

      Ok(view(form, list, taxYear, request.user.isAgentMessageKey, mode))
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val list: SummaryList = summaryList(taxYear, request.userAnswers)

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, list, taxYear, request.user.isAgentMessageKey, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(CountriesRentedPropertyPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(CountriesRentedPropertyPage, taxYear, mode, request.userAnswers, updatedAnswers))
      )
  }

  private def summaryList(taxYear: Int, userAnswers: UserAnswers)(implicit messages: Messages)
  = {
    val countries = userAnswers.get(CountryGroup()).toSeq.flatten
    val rows = countries.zipWithIndex.flatMap { case (_, index) =>
    CountriesRentedPropertySummary.row(taxYear, index, userAnswers)
    }
    SummaryListViewModel(rows)
  }
}
