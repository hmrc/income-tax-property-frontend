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

package controllers.enhancedstructuresbuildingallowance

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.enhancedstructuresbuildingallowance.EsbaAddressFormProvider
import models.{Mode, PropertyType}
import navigation.Navigator
import pages.enhancedstructuresbuildingallowance.EsbaAddressPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.enhancedstructuresbuildingallowance.EsbaAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EsbaAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: EsbaAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: EsbaAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, mode: Mode, index: Int, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val form = formProvider(request.userAnswers, propertyType, index)
      val preparedForm = request.userAnswers.get(EsbaAddressPage(index, propertyType)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, taxYear, mode, index, propertyType))
    }

  def onSubmit(taxYear: Int, mode: Mode, index: Int, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form = formProvider(request.userAnswers, propertyType, index)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, taxYear, mode, index, propertyType))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(EsbaAddressPage(index, propertyType), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator.esbaNextPage(
                EsbaAddressPage(index, propertyType),
                taxYear,
                mode,
                index,
                request.userAnswers,
                updatedAnswers
              )
            )
        )
    }
}
