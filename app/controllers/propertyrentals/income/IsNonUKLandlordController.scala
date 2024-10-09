/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.propertyrentals.income

import controllers.actions._
import forms.propertyrentals.income.IsNonUKLandlordFormProvider
import models.{Mode, PropertyType}
import navigation.Navigator
import pages.propertyrentals.income.IsNonUKLandlordPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import service.CYADiversionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.propertyrentals.income.IsNonUKLandlordView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsNonUKLandlordController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  diversionService: CYADiversionService,
  formProvider: IsNonUKLandlordFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: IsNonUKLandlordView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, mode: Mode, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      diversionService
        .redirectToCYAIfFinished[Result](taxYear, request.userAnswers, "income", propertyType, mode) {

          val form = formProvider(request.user.isAgentMessageKey)

          val preparedForm = request.userAnswers.get(IsNonUKLandlordPage(propertyType)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, taxYear, mode, request.user.isAgentMessageKey, propertyType))
        }(Redirect(_))
    }

  def onSubmit(taxYear: Int, mode: Mode, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(view(formWithErrors, taxYear, mode, request.user.isAgentMessageKey, propertyType))
            ),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(IsNonUKLandlordPage(propertyType), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator.nextPage(IsNonUKLandlordPage(propertyType), taxYear, mode, request.userAnswers, updatedAnswers)
            )
        )
    }
}
