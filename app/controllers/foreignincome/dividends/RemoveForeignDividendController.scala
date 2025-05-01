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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.foreignincome.dividends.RemoveForeignDividendFormProvider
import models.{NormalMode, UserAnswers}
import navigation.ForeignIncomeNavigator
import pages.foreignincome.CountryReceiveDividendIncomePage
import pages.foreignincome.dividends.RemoveForeignDividendPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.language.LanguageUtils
import viewmodels.checkAnswers.foreignincome.dividends.RemoveForeignDividendSummary
import views.html.foreignincome.dividends.RemoveForeignDividendView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveForeignDividendController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ForeignIncomeNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: RemoveForeignDividendFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveForeignDividendView,
  languageUtils: LanguageUtils
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, index: Int): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val currentLang = languageUtils.getCurrentLang.locale.toString
      val row = RemoveForeignDividendSummary.row(index, request.userAnswers, currentLang)
      val form: Form[Boolean] = formProvider(row.map(_.country.name).getOrElse(""))
      Ok(view(form, taxYear, index, row))
    }

  def onSubmit(taxYear: Int, index: Int): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val currentLang = languageUtils.getCurrentLang.locale.toString
      val row = RemoveForeignDividendSummary.row(index, request.userAnswers, currentLang)
      val form: Form[Boolean] = formProvider(row.map(_.country.name).getOrElse(""))
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, taxYear, index, row))),
          {
            case true =>
              removeForeignDividend(request.userAnswers, taxYear, index)
            case false =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(RemoveForeignDividendPage, false))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(
                routes.YourForeignDividendsByCountryController.onPageLoad(taxYear, NormalMode)
              )
          }
        )
    }

  private def removeForeignDividend(userAnswers: UserAnswers, taxYear: Int, index: Int): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(userAnswers.set(RemoveForeignDividendPage, true))
      removeCountry  <- Future.fromTry(updatedAnswers.remove(CountryReceiveDividendIncomePage(index)))
      _              <- sessionRepository.set(removeCountry)
    } yield Redirect(
      navigator.nextPage(RemoveForeignDividendPage, taxYear, NormalMode, userAnswers, removeCountry)
    )

}
