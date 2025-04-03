/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.foreignincome.dividends

import controllers.actions._
import forms.foreignincome.dividends.CountryReceiveDividendIncomeFormProvider

import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.CountryReceiveDividendIncomePage
import play.api.data.Form
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import service.CountryNamesDataSource
import service.CountryNamesDataSource.countrySelectItemsWithUSA
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.language.LanguageUtils
import views.html.foreignincome.dividends.CountryReceiveDividendIncomeView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

class CountryReceiveDividendIncomeController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: CountryReceiveDividendIncomeFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: CountryReceiveDividendIncomeView,
                                        languageUtils: LanguageUtils
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form: Form[String] = formProvider(request.userAnswers)
      val preparedForm = request.userAnswers.get(CountryReceiveDividendIncomePage) match {
        case None => form
        case Some(value) => form.fill(value.code)
      }

      Ok(view(preparedForm, taxYear, mode, countrySelectItemsWithUSA(languageUtils.getCurrentLang.locale.toString)))
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form: Form[String] = formProvider(request.userAnswers)
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, taxYear, mode, countrySelectItemsWithUSA(languageUtils.getCurrentLang.locale.toString)))),

        countryCode =>
          for {
            updatedAnswers <- Future.fromTry(
              CountryNamesDataSource
                .getCountry(countryCode, languageUtils.getCurrentLang.locale.toString)
                .map(country => request.userAnswers.set(CountryReceiveDividendIncomePage, country))
                .getOrElse(Failure(new NoSuchElementException(s"Country code '$countryCode' not recognised")))
            )
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(CountryReceiveDividendIncomePage, taxYear, mode, request.userAnswers, updatedAnswers))
      )
  }
}
