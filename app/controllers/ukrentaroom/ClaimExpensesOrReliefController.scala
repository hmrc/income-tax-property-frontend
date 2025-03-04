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

package controllers.ukrentaroom

import controllers.actions._
import forms.ukrentaroom.ClaimExpensesOrReliefFormProvider
import models.requests.DataRequest
import models.{BusinessConstants, Mode, PropertyType}
import navigation.Navigator
import pages.ukrentaroom.{ClaimExpensesOrReliefPage, JointlyLetPage, TotalIncomeAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ukrentaroom.ClaimExpensesOrReliefView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClaimExpensesOrReliefController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ClaimExpensesOrReliefFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ClaimExpensesOrReliefView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, mode: Mode, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      maxAllowedClaim(request, propertyType)
        .map { maxIncome =>
          val form = formProvider(request.user.isAgentMessageKey, maxIncome)
          val preparedForm = request.userAnswers.get(ClaimExpensesOrReliefPage(propertyType)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          Future.successful(
            Ok(view(preparedForm, taxYear, mode, request.user.isAgentMessageKey, maxIncome, propertyType))
          )
        }
        .getOrElse(Future.failed(NotFoundException))
    }

  def onSubmit(taxYear: Int, mode: Mode, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      maxAllowedClaim(request, propertyType)
        .map { maxClaim =>
          val form = formProvider(request.user.isAgentMessageKey, maxClaim)
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future
                  .successful(
                    BadRequest(
                      view(formWithErrors, taxYear, mode, request.user.isAgentMessageKey, maxClaim, propertyType)
                    )
                  ),
              value =>
                for {
                  updatedAnswers <-
                    Future.fromTry(request.userAnswers.set(ClaimExpensesOrReliefPage(propertyType), value))
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(
                  navigator
                    .nextPage(
                      ClaimExpensesOrReliefPage(propertyType),
                      taxYear,
                      mode,
                      request.userAnswers,
                      updatedAnswers
                    )
                )
            )
        }
        .getOrElse(Future.failed(NotFoundException))
    }

  private def maxAllowedClaim(request: DataRequest[AnyContent], propertyType: PropertyType): Option[BigDecimal] =
    for {
      isJointlyLet <- request.userAnswers.get(JointlyLetPage(propertyType))
      income       <- request.userAnswers.get(TotalIncomeAmountPage(propertyType))
    } yield {
      val maxAllowedIncome =
        if (isJointlyLet) BusinessConstants.jointlyLetTaxFreeAmount else BusinessConstants.notJointlyLetTaxFreeAmount
      income min maxAllowedIncome
    }

}

case object NotFoundException extends Exception("TotalIncomeAmount is not present in userAnswers")
