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

import controllers.actions._
import forms.enhancedstructuresbuildingallowance.EsbaRemoveConfirmationFormProvider
import models.requests.DataRequest
import models.{Mode, PropertyType}
import navigation.Navigator
import pages.enhancedstructuresbuildingallowance.{EnhancedStructuresBuildingAllowanceWithIndex, EsbaClaimPage, EsbaRemoveConfirmationPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.FormatUtils.bigDecimalCurrency
import views.html.enhancedstructuresbuildingallowance.EsbaRemoveConfirmationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class EsbaRemoveConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: EsbaRemoveConfirmationFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: EsbaRemoveConfirmationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, index: Int, mode: Mode, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val form: Form[Boolean] = formProvider(request.user.isAgentMessageKey)
      Ok(view(form, taxYear, index, mode, claimValue(index, request, propertyType), propertyType))
    }

  def onSubmit(taxYear: Int, index: Int, mode: Mode, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form: Form[Boolean] = formProvider(request.user.isAgentMessageKey)

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(
                view(formWithErrors, taxYear, index, mode, claimValue(index, request, propertyType), propertyType)
              )
            ),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(EsbaRemoveConfirmationPage(propertyType), value))
              updatedAnswers <-
                Future.fromTry {
                  if (value) {
                    updatedAnswers.remove(EnhancedStructuresBuildingAllowanceWithIndex(index, propertyType))
                  } else {
                    Success(updatedAnswers)
                  }
                }
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator
                .nextPage(EsbaRemoveConfirmationPage(propertyType), taxYear, mode, request.userAnswers, updatedAnswers)
            )
        )
    }

  private def claimValue(index: Int, request: DataRequest[AnyContent], propertyType: PropertyType): String = {
    val value = request.userAnswers.get(EsbaClaimPage(index, propertyType)).getOrElse(BigDecimal(0))
    bigDecimalCurrency(value)
  }
}
