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
import forms.foreign.CalculatedPremiumLeaseTaxableFormProvider
import models.{Mode, PremiumCalculated}
import navigation.{ForeignPropertyNavigator, Navigator}
import pages.foreign.CalculatedPremiumLeaseTaxablePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.foreign.CalculatedPremiumLeaseTaxableView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CalculatedPremiumLeaseTaxableController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         foreignPropertyNavigator: ForeignPropertyNavigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: CalculatedPremiumLeaseTaxableFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: CalculatedPremiumLeaseTaxableView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear:Int, countryCode: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form: Form[PremiumCalculated] = formProvider(request.user.isAgentMessageKey)
      val preparedForm = request.userAnswers.get(CalculatedPremiumLeaseTaxablePage(countryCode)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, taxYear, countryCode, mode, request.user.isAgentMessageKey))
  }

  def onSubmit(taxYear: Int, countryCode: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form: Form[PremiumCalculated] = formProvider(request.user.isAgentMessageKey)
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, taxYear, countryCode, mode, request.user.isAgentMessageKey))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(CalculatedPremiumLeaseTaxablePage(countryCode), value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(
            foreignPropertyNavigator.nextPage(
              CalculatedPremiumLeaseTaxablePage(countryCode),
              taxYear,
              mode,
              request.userAnswers,
              updatedAnswers))
      )
  }
}
