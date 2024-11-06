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
import service.CountryNamesDataSource.countrySelectItems
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.foreign.SelectIncomeCountryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelectIncomeCountryController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ForeignPropertyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: SelectIncomeCountryFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: SelectIncomeCountryView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, index: Int, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val form: Form[String] = formProvider(request.user.isAgentMessageKey)
      val preparedForm = request.userAnswers.get(SelectIncomeCountryPage(index)) match {
        case None        => form
        case Some(value) => form.fill(value.code)
      }
      Ok(view(preparedForm, taxYear, index: Int, request.user.isAgentMessageKey, mode, countrySelectItems))
    }

  def onSubmit(taxYear: Int, index: Int, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form: Form[String] = formProvider(request.user.isAgentMessageKey)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(
                view(formWithErrors, taxYear, index: Int, request.user.isAgentMessageKey, mode, countrySelectItems)
              )
            ),
          countryCode =>
            for {
              updatedAnswers <-
                Future
                  .fromTry(
                    request.userAnswers
                      .set(SelectIncomeCountryPage(index), CountryNamesDataSource.getCountry(countryCode))
                  )
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator.nextPage(SelectIncomeCountryPage(index), taxYear, mode, request.userAnswers, updatedAnswers)
            )
        )
    }
}
