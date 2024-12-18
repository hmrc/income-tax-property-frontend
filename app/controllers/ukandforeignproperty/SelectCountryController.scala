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
import forms.ukandforeignproperty.SelectCountryFormProvider
import models.{Index, Mode}
import navigation.UkAndForeignPropertyNavigator
import pages.ukandforeignproperty.SelectCountryPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import service.CountryNamesDataSource
import service.CountryNamesDataSource.countrySelectItems
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ukandforeignproperty.SelectCountryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelectCountryController @Inject() (
                                          override val messagesApi: MessagesApi,
                                          sessionRepository: SessionRepository,
                                          navigator: UkAndForeignPropertyNavigator,
                                          identify: IdentifierAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: SelectCountryFormProvider,
                                          val controllerComponents: MessagesControllerComponents,
                                          view: SelectCountryView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val form: Form[String] = formProvider(request.user.isAgentMessageKey)

      val preparedForm = request.userAnswers.get(SelectCountryPage) match {
        case None => form
        case Some(countrySet) =>
          countrySet.toList.lift(index.position - 1) match {
            case Some(country) => form.fill(country.code)
            case _             => form
          }
      }

      Ok(view(preparedForm, taxYear, index, request.user.isAgentMessageKey, mode, countrySelectItems))
    }

  def onSubmit(taxYear: Int, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form: Form[String] = formProvider(request.user.isAgentMessageKey)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(view(formWithErrors, taxYear, index, request.user.isAgentMessageKey, mode, countrySelectItems))
            ),
          countryCode =>
            for {
              existingCountryList <- Future(request.userAnswers.get(SelectCountryPage))
              country             <- Future(CountryNamesDataSource.getCountry(countryCode))
              updatedCountryList =
                existingCountryList
                  .map(_ ++ country)
                  .getOrElse(country)
                  .toSet
              updatedUserAnswers <- Future.fromTry(request.userAnswers.set(SelectCountryPage, updatedCountryList))
              _ <- sessionRepository.set(updatedUserAnswers)
            } yield Redirect(
                navigator.nextPage(SelectCountryPage, taxYear, mode, request.userAnswers, updatedUserAnswers)
              )
        )
    }
}
