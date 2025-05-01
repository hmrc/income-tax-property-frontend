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
import forms.foreignincome.dividends.YourForeignDividendsByCountryFormProvider
import models.{Mode, UserAnswers, YourForeignDividendsByCountryRow}
import navigation.ForeignIncomeNavigator
import pages.foreignincome.DividendIncomeSourceCountries
import pages.foreignincome.dividends.YourForeignDividendsByCountryPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import service.CountryNamesDataSource
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.language.LanguageUtils
import viewmodels.checkAnswers.foreignincome.dividends.YourForeignDividendsByCountrySummary
import views.html.foreignincome.dividends.YourForeignDividendsByCountryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class YourForeignDividendsByCountryController @Inject()(
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ForeignIncomeNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: YourForeignDividendsByCountryFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: YourForeignDividendsByCountryView,
  languageUtils: LanguageUtils
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val currentLang = languageUtils.getCurrentLang.locale.toString
      val rows = YourForeignDividendsByCountrySummary.tableRows(taxYear, request.userAnswers, currentLang)
      val form: Form[Boolean] = formProvider(request.user.isAgentMessageKey)
      Ok(view(form, rows, taxYear, request.user.isAgentMessageKey, mode))
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val currentLang = languageUtils.getCurrentLang.locale.toString
      val rows = YourForeignDividendsByCountrySummary.tableRows(taxYear, request.userAnswers, currentLang)
      val form: Form[Boolean] = formProvider(request.user.isAgentMessageKey)
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, rows, taxYear, request.user.isAgentMessageKey, mode))),
        addAnotherCountry =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(YourForeignDividendsByCountryPage, addAnotherCountry))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(YourForeignDividendsByCountryPage, taxYear, mode, request.userAnswers, updatedAnswers))
      )
  }
}
