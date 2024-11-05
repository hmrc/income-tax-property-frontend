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

import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.foreign.{SelectIncomeCountryPage, DoYouWantToRemoveCountryPage}
import play.api.data.Form
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.foreign.DoYouWantToRemoveCountryView

import scala.concurrent.{ExecutionContext, Future}

class DoYouWantToRemoveCountryController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: DoYouWantToRemoveCountryFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: DoYouWantToRemoveCountryView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val name: String = request.userAnswers.get(SelectIncomeCountryPage).getOrElse("")
//
//        .map {
//        country =>
//          val value = s"${country}"
//      }


      Ok(view(form, taxYear, mode, name))
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val name: String = request.userAnswers.get(SelectIncomeCountryPage).getOrElse("")

//        .map {
//        country =>
//          val value = s"${country}"
//      }

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, taxYear, mode, name))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(DoYouWantToRemoveCountryPage, value))
//            updatedAnswers <- Future.fromTry(request.userAnswers.set(SelectIncomeCountryPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(DoYouWantToRemoveCountryPage, taxYear, mode, request.userAnswers, updatedAnswers))
      )
  }
}
