/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.ukandforeignproperty

import controllers.actions._
import forms.ukandforeignproperty.UKPremiumsGrantLeaseFormProvider
import models.Mode
import models.ukAndForeign.UKPremiumsGrantLease
import navigation.UkAndForeignPropertyNavigator
import pages.ukandforeignproperty.{UKPremiumsGrantLeasePage, UkAmountReceivedForGrantOfLeasePage, UkYearLeaseAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ukandforeignproperty.UKPremiumsGrantLeaseView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UKPremiumsGrantLeaseController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: UkAndForeignPropertyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: UKPremiumsGrantLeaseFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: UKPremiumsGrantLeaseView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val receivedGrantLeaseAmount: Option[BigDecimal] =
        request.userAnswers.get(UkAmountReceivedForGrantOfLeasePage).map(_.amountReceivedForGrantOfLease)
      val totalYearPeriods: Option[Int] = request.userAnswers.get(UkYearLeaseAmountPage)

      (receivedGrantLeaseAmount, totalYearPeriods) match {
        case (None, _) =>
          Redirect(routes.UkAndForeignPropertyAmountReceivedForGrantOfLeaseController.onPageLoad(taxYear, mode))
        case (_, None) => Redirect(routes.UkYearLeaseAmountController.onPageLoad(taxYear, mode))
        case (Some(amount), Some(period)) =>
          val preparedForm = request.userAnswers.get(UKPremiumsGrantLeasePage) match {
            case None        => formProvider(request.user.isAgentMessageKey)
            case Some(value) => formProvider(request.user.isAgentMessageKey).fill(value)
          }

          Ok(view(preparedForm, taxYear, period, amount, mode, request.user.isAgentMessageKey))
      }
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val receivedGrantLeaseAmount: Option[BigDecimal] =
        request.userAnswers.get(UkAmountReceivedForGrantOfLeasePage).map(_.amountReceivedForGrantOfLease)
      val totalYearPeriods: Option[Int] = request.userAnswers.get(UkYearLeaseAmountPage)

      (receivedGrantLeaseAmount, totalYearPeriods) match {
        case (None, _) =>
          Future.successful(
            Redirect(routes.UkAndForeignPropertyAmountReceivedForGrantOfLeaseController.onPageLoad(taxYear, mode))
          )
        case (_, None) =>
          Future.successful(Redirect(routes.UkYearLeaseAmountController.onPageLoad(taxYear, mode)))
        case (Some(amount), Some(period)) =>
          formProvider(request.user.isAgentMessageKey)
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(formWithErrors, taxYear, period, amount, mode, request.user.isAgentMessageKey)
                  )
                ),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(
                                      request.userAnswers.set(
                                        UKPremiumsGrantLeasePage,
                                        UKPremiumsGrantLease(
                                          value.premiumsGrantLeaseReceived,
                                          Some(
                                            value.premiumsGrantLease.getOrElse(
                                              UKPremiumsGrantLeasePage
                                                .calculateTaxableAmount(amount, period)
                                            )
                                          )
                                        )
                                      )
                                    )
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(
                  navigator
                    .nextPage(UKPremiumsGrantLeasePage, taxYear, mode, request.userAnswers, updatedAnswers)
                )
            )
      }
  }
}
