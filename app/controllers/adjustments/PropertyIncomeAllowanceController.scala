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

package controllers.adjustments

import controllers.actions._
import forms.adjustments.PropertyIncomeAllowanceFormProvider
import models.TotalIncomeUtils.{incomeAndBalancingChargeCombined, maxAllowedPIA}
import models.{Mode, PropertyType}
import navigation.Navigator
import pages.adjustments.PropertyIncomeAllowancePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.adjustments.PropertyIncomeAllowanceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PropertyIncomeAllowanceController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: PropertyIncomeAllowanceFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PropertyIncomeAllowanceView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, mode: Mode, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val combinedAmount = incomeAndBalancingChargeCombined(request.userAnswers, propertyType)
      val form = formProvider(request.user.isAgentMessageKey, combinedAmount)
      val preparedForm = request.userAnswers.get(PropertyIncomeAllowancePage(propertyType)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, mode, taxYear, request.user.isAgentMessageKey, maxAllowedPIA(combinedAmount), propertyType))
    }

  def onSubmit(taxYear: Int, mode: Mode, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val combinedAllowance = incomeAndBalancingChargeCombined(request.userAnswers, propertyType)
      val form = formProvider(request.user.isAgentMessageKey, combinedAllowance)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(
                view(
                  formWithErrors,
                  mode,
                  taxYear,
                  request.user.isAgentMessageKey,
                  maxAllowedPIA(combinedAllowance),
                  propertyType
                )
              )
            ),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(request.userAnswers.set(PropertyIncomeAllowancePage(propertyType), value))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator
                .nextPage(PropertyIncomeAllowancePage(propertyType), taxYear, mode, request.userAnswers, updatedAnswers)
            )
        )
    }
}
