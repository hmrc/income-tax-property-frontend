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

package controllers.foreign.structuresbuildingallowance

import controllers.actions._
import forms.foreign.structurebuildingallowance.ForeignStructuresBuildingAllowanceAddressFormProvider
import models.Mode
import navigation.ForeignPropertyNavigator
import pages.foreign.structurebuildingallowance.ForeignStructuresBuildingAllowanceAddressPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.foreign.structurebuildingallowance.ForeignStructuresBuildingAllowanceAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignStructuresBuildingAllowanceAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  foreignNavigator: ForeignPropertyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ForeignStructuresBuildingAllowanceAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ForeignStructuresBuildingAllowanceAddressView
)(implicit val ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, index: Int, countryCode: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val form = formProvider(request.userAnswers, countryCode, index)
      val preparedForm =
        request.userAnswers.get(ForeignStructuresBuildingAllowanceAddressPage(index, countryCode)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

      Ok(view(preparedForm, taxYear, index, countryCode, mode))
    }

  def onSubmit(taxYear: Int, index: Int, countryCode: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form = formProvider(request.userAnswers, countryCode, index)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, taxYear, index, countryCode, mode))),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(
                  request.userAnswers.set(ForeignStructuresBuildingAllowanceAddressPage(index, countryCode), value)
                )
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              foreignNavigator.nextPage(
                ForeignStructuresBuildingAllowanceAddressPage(index, countryCode),
                taxYear,
                mode,
                request.userAnswers,
                updatedAnswers
              )
            )
        )
    }
}
