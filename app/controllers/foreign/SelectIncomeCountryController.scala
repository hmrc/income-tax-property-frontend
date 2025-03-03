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
import forms.foreign.SelectIncomeCountryFormProvider
import models.Mode
import navigation.ForeignPropertyNavigator
import pages.foreign.SelectIncomeCountryPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import service.CountryNamesDataSource
import service.CountryNamesDataSource.{countrySelectItems, countrySelectItemsWithUSA}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.language.LanguageUtils
import views.html.foreign.SelectIncomeCountryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

class SelectIncomeCountryController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ForeignPropertyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: SelectIncomeCountryFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: SelectIncomeCountryView,
  languageUtils: LanguageUtils
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, index: Int, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val form: Form[String] = formProvider(request.user.isAgentMessageKey, request.userAnswers)
      val preparedForm = request.userAnswers.get(SelectIncomeCountryPage(index)) match {
        case None        => form
        case Some(value) => form.fill(value.code)
      }
      Ok(
        view(
          preparedForm,
          taxYear,
          index: Int,
          request.user.isAgentMessageKey,
          mode,
          countrySelectItemsWithUSA(languageUtils.getCurrentLang.locale.toString)
        )
      )
    }

  def onSubmit(taxYear: Int, index: Int, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form: Form[String] = formProvider(request.user.isAgentMessageKey, request.userAnswers)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(
                view(
                  formWithErrors,
                  taxYear,
                  index: Int,
                  request.user.isAgentMessageKey,
                  mode,
                  countrySelectItems(languageUtils.getCurrentLang.locale.toString)
                )
              )
            ),
          countryCode =>
            for {
              updatedAnswers <-
                Future.fromTry(
                  CountryNamesDataSource
                    .getCountry(countryCode, languageUtils.getCurrentLang.locale.toString)
                    .map(country => request.userAnswers.set(SelectIncomeCountryPage(index), country))
                    .getOrElse(Failure(new NoSuchElementException(s"Country code '$countryCode' not recognized")))
                )
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator.nextPage(SelectIncomeCountryPage(index), taxYear, mode, request.userAnswers, updatedAnswers)
            )
        )
    }
}
