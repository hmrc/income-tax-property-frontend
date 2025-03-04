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
import controllers.exceptions.InternalErrorFailure
import forms.foreign.ForeignIncomeTaxFormProvider
import models.Mode
import navigation.ForeignPropertyNavigator
import pages.foreign.ForeignIncomeTaxPage
import pages.foreign.income.ForeignPropertyTaxSectionAddCountryCode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import service.CountryNamesDataSource
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.language.LanguageUtils
import views.html.foreign.ForeignIncomeTaxView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignIncomeTaxController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  foreignPropertyNavigator: ForeignPropertyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ForeignIncomeTaxFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ForeignIncomeTaxView,
  languageUtils: LanguageUtils
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, countryCode: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)

      val preparedForm = request.userAnswers.get(ForeignIncomeTaxPage(countryCode)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      CountryNamesDataSource.getCountry(countryCode, languageUtils.getCurrentLang.locale.toString) match {
        case Some(country) =>
          Future.successful(Ok(view(preparedForm, taxYear, request.user.isAgentMessageKey, country, mode)))
        case _ => Future.failed(InternalErrorFailure(s"Country code '$countryCode' not recognized"))
      }
    }

  def onSubmit(taxYear: Int, countryCode: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            CountryNamesDataSource.getCountry(countryCode, languageUtils.getCurrentLang.locale.toString) match {
              case Some(country) =>
                Future
                  .successful(BadRequest(view(formWithErrors, taxYear, request.user.isAgentMessageKey, country, mode)))
              case _ => Future.failed(InternalErrorFailure(s"Country code '$countryCode' not recognized"))
            },
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ForeignIncomeTaxPage(countryCode), value))
              updatedAnswersWithCountryCode <-
                Future.fromTry(updatedAnswers.set(ForeignPropertyTaxSectionAddCountryCode(countryCode), countryCode))
              _ <- sessionRepository.set(updatedAnswersWithCountryCode)
            } yield Redirect(
              foreignPropertyNavigator
                .nextPage(ForeignIncomeTaxPage(countryCode), taxYear, mode, request.userAnswers, updatedAnswers)
            )
        )
    }

}
