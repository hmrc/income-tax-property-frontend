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
import controllers.exceptions.InternalErrorFailure
import forms.foreignincome.dividends.HowMuchForeignTaxDeductedFromDividendIncomeFormProvider
import models.Mode
import navigation.ForeignIncomeNavigator
import pages.foreignincome.dividends.HowMuchForeignTaxDeductedFromDividendIncomePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import service.CountryNamesDataSource
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.language.LanguageUtils
import views.html.foreignincome.dividends.HowMuchForeignTaxDeductedFromDividendIncomeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HowMuchForeignTaxDeductedFromDividendIncomeController @Inject()(
                                                                       override val messagesApi: MessagesApi,
                                                                       sessionRepository: SessionRepository,
                                                                       navigator: ForeignIncomeNavigator,
                                                                       identify: IdentifierAction,
                                                                       getData: DataRetrievalAction,
                                                                       requireData: DataRequiredAction,
                                                                       formProvider: HowMuchForeignTaxDeductedFromDividendIncomeFormProvider,
                                                                       val controllerComponents: MessagesControllerComponents,
                                                                       view: HowMuchForeignTaxDeductedFromDividendIncomeView,
                                                                       languageUtils: LanguageUtils
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {



  def onPageLoad(taxYear: Int, countryCode: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider()
      val preparedForm = request.userAnswers.get(HowMuchForeignTaxDeductedFromDividendIncomePage(countryCode)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      CountryNamesDataSource.getCountry(countryCode, languageUtils.getCurrentLang.locale.toString) match {
        case Some(country) =>
          Future.successful(Ok(view(preparedForm, taxYear, country, request.user.isAgentMessageKey, mode)))
        case _ => Future.failed(InternalErrorFailure(s"Country code '$countryCode' not recognized"))
      }
  }

  def onSubmit(taxYear: Int, countryCode: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider()
      form.bindFromRequest().fold(
        formWithErrors =>
          CountryNamesDataSource.getCountry(countryCode, languageUtils.getCurrentLang.locale.toString) match {
            case Some(country) =>
              Future
                .successful(BadRequest(view(formWithErrors, taxYear, country, request.user.isAgentMessageKey, mode)))
            case _ => Future.failed(InternalErrorFailure(s"Country code '$countryCode' not recognized"))
          },
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(HowMuchForeignTaxDeductedFromDividendIncomePage(countryCode), value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(HowMuchForeignTaxDeductedFromDividendIncomePage(countryCode), taxYear, mode, request.userAnswers, updatedAnswers))
      )
  }
}
