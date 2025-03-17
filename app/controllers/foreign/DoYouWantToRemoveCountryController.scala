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
import forms.DoYouWantToRemoveCountryFormProvider
import models.Mode
import navigation.ForeignPropertyNavigator
import pages.foreign.{Country, DoYouWantToRemoveCountryPage, SelectIncomeCountryPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import service.CountryNamesDataSource
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.language.LanguageUtils
import views.html.foreign.DoYouWantToRemoveCountryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class DoYouWantToRemoveCountryController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ForeignPropertyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: DoYouWantToRemoveCountryFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DoYouWantToRemoveCountryView,
  languageUtils: LanguageUtils
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(taxYear: Int, index: Int, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers
        .get(SelectIncomeCountryPage(index))
        .map { country =>
          CountryNamesDataSource
            .getCountry(country.code, languageUtils.getCurrentLang.locale.toString)
            .getOrElse(Country("", ""))
            .name
        }
        .fold(Future.successful(InternalServerError("Country not found")))(name => //TODO we need to better handle this error we should not be displaying this to the user
          Future.successful(Ok(view(form, taxYear, index, mode, name)))
        )

    }

  def onSubmit(taxYear: Int, index: Int, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers
        .get(SelectIncomeCountryPage(index))
        .map { country =>
          country.name
        }
        .fold(Future.successful(InternalServerError("Country not found")))(
          name => // TODO we need to better handle this error we should not be displaying this to the user
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, taxYear, index, mode, name))),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(DoYouWantToRemoveCountryPage, value))
                    updatedAnswers <- Future.fromTry {
                                        if (value) {
                                          updatedAnswers.remove(SelectIncomeCountryPage(index))
                                        } else {
                                          Success(updatedAnswers)
                                        }
                                      }
                    _ <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(
                    navigator.nextPage(DoYouWantToRemoveCountryPage, taxYear, mode, request.userAnswers, updatedAnswers)
                  )
              )
        )
    }
}
