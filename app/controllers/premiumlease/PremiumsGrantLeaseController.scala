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

package controllers.premiumlease

import controllers.actions._
import forms.premiumlease.PremiumsGrantLeaseFormProvider
import models.{Mode, PremiumsGrantLease, PropertyType}
import navigation.Navigator
import pages.premiumlease.{PremiumsGrantLeasePage, ReceivedGrantLeaseAmountPage, YearLeaseAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.premiumlease.PremiumsGrantLeaseView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PremiumsGrantLeaseController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: PremiumsGrantLeaseFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PremiumsGrantLeaseView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, mode: Mode, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val receivedGrantLeaseAmount: Option[BigDecimal] =
        request.userAnswers.get(ReceivedGrantLeaseAmountPage(propertyType))
      val totalYearPeriods: Option[Int] = request.userAnswers.get(YearLeaseAmountPage(propertyType))

      (receivedGrantLeaseAmount, totalYearPeriods) match {

        case (None, _) => Redirect(routes.ReceivedGrantLeaseAmountController.onPageLoad(taxYear, mode, propertyType))
        case (_, None) => Redirect(routes.YearLeaseAmountController.onPageLoad(taxYear, mode, propertyType))
        case (Some(amount), Some(period)) =>
          val preparedForm = request.userAnswers.get(PremiumsGrantLeasePage(propertyType)) match {
            case None        => formProvider(request.user.isAgentMessageKey)
            case Some(value) => formProvider(request.user.isAgentMessageKey).fill(value)
          }

          Ok(view(preparedForm, taxYear, period, amount, mode, request.user.isAgentMessageKey, propertyType))
      }
    }

  def onSubmit(taxYear: Int, mode: Mode, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val receivedGrantLeaseAmount: Option[BigDecimal] =
        request.userAnswers.get(ReceivedGrantLeaseAmountPage(propertyType))
      val totalYearPeriods: Option[Int] = request.userAnswers.get(YearLeaseAmountPage(propertyType))

      (receivedGrantLeaseAmount, totalYearPeriods) match {
        case (None, _) =>
          Future.successful(Redirect(routes.ReceivedGrantLeaseAmountController.onPageLoad(taxYear, mode, propertyType)))
        case (_, None) =>
          Future.successful(Redirect(routes.YearLeaseAmountController.onPageLoad(taxYear, mode, propertyType)))
        case (Some(amount), Some(period)) =>
          formProvider(request.user.isAgentMessageKey)
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(formWithErrors, taxYear, period, amount, mode, request.user.isAgentMessageKey, propertyType)
                  )
                ),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(
                                      request.userAnswers.set(
                                        PremiumsGrantLeasePage(propertyType),
                                        PremiumsGrantLease(
                                          value.premiumsGrantLeaseReceived,
                                          Some(
                                            value.premiumsGrantLease.getOrElse(
                                              PremiumsGrantLeasePage(propertyType)
                                                .calculateTaxableAmount(amount, period)
                                            )
                                          )
                                        )
                                      )
                                    )
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(
                  navigator
                    .nextPage(PremiumsGrantLeasePage(propertyType), taxYear, mode, request.userAnswers, updatedAnswers)
                )
            )
      }
    }
}
