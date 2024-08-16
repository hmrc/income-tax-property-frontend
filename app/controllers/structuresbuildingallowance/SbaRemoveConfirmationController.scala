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

package controllers.structuresbuildingallowance

import controllers.actions._
import forms.structurebuildingallowance.SbaRemoveConfirmationFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.Navigator
import pages.structurebuildingallowance.{SbaRemoveConfirmationPage, StructureBuildingAllowanceClaimPage, StructureBuildingAllowanceWithIndex}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.FormatUtils.bigDecimalCurrency
import views.html.structurebuildingallowance.SbaRemoveConfirmationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class SbaRemoveConfirmationController @Inject()(
                                                 override val messagesApi: MessagesApi,
                                                 sessionRepository: SessionRepository,
                                                 navigator: Navigator,
                                                 identify: IdentifierAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 formProvider: SbaRemoveConfirmationFormProvider,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 view: SbaRemoveConfirmationView
                                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def onPageLoad(taxYear: Int, index: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form: Form[Boolean] = formProvider(request.user.isAgentMessageKey)
      Ok(view(form, taxYear, index, mode, claimValue(index, request)))
  }

  private def claimValue(index: Int, request: DataRequest[AnyContent]): String = {
    val value = request.userAnswers.get(StructureBuildingAllowanceClaimPage(index)).getOrElse(BigDecimal(0))
    bigDecimalCurrency(value)
  }

  def onSubmit(taxYear: Int, index: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form: Form[Boolean] = formProvider(request.user.isAgentMessageKey)

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, taxYear, index, mode, claimValue(index, request)))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(SbaRemoveConfirmationPage, value))
            updatedAnswers <- Future.fromTry {
              if (value) {
                updatedAnswers.remove(StructureBuildingAllowanceWithIndex(index))
              } else {
                Success(updatedAnswers)
              }
            }
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(SbaRemoveConfirmationPage, taxYear, mode, request.userAnswers, updatedAnswers))
      )
  }
}
