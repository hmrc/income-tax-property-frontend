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
import forms.DoYouWantToRemoveCountryFormProvider

import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.foreign.{DoYouWantToRemoveCountryPage, SelectIncomeCountryPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.foreign.DoYouWantToRemoveCountryView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class DoYouWantToRemoveCountryController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: DoYouWantToRemoveCountryFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DoYouWantToRemoveCountryView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(taxYear: Int, index: Int, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers
        .get(SelectIncomeCountryPage(index))
        .map { country =>
          country.name
        }
        .fold(Future.successful(InternalServerError("Country not found")))(name =>
          request.userAnswers.get(DoYouWantToRemoveCountryPage) match {
            case Some(value) => Future.successful(Ok(view(form.fill(value), taxYear, index, mode, name)))
            case _           => Future.successful(Ok(view(form, taxYear, index, mode, name)))
          }
        )

    }

  def onSubmit(taxYear: Int, index: Int, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers
        .get(SelectIncomeCountryPage(index))
        .map { country =>
          country.name
        }
        .fold(Future.successful(InternalServerError("Country not found")))(name =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, taxYear, index, mode, name))),
              value =>
                for {
                  updatedAnswers <- if (value) {
                                      Future.fromTry(request.userAnswers.remove(SelectIncomeCountryPage(index)))
                                    } else {
                                      Future.fromTry(Success(request.userAnswers))
                                    }
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(
                  navigator.nextPage(DoYouWantToRemoveCountryPage, taxYear, mode, request.userAnswers, updatedAnswers)
                )
            )
        )
    }
}
