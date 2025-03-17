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
import forms.ukandforeignproperty.UkAndForeignPropertyForeignPremiumsGrantLeaseFormProvider
import models.Mode
import models.ukAndForeign.UkAndForeignPropertyForeignPremiumsGrantLease
import navigation.UkAndForeignPropertyNavigator
import pages.ukandforeignproperty.{ForeignLeaseGrantAmountReceivedPage, ForeignYearLeaseAmountPage, UkAndForeignPropertyForeignPremiumsGrantLeasePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ukandforeignproperty.UkAndForeignPropertyForeignPremiumsGrantLeaseView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UkAndForeignPropertyForeignPremiumsGrantLeaseController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: UkAndForeignPropertyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: UkAndForeignPropertyForeignPremiumsGrantLeaseFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: UkAndForeignPropertyForeignPremiumsGrantLeaseView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)

      val receivedGrantLeaseAmount: Option[BigDecimal] = request.userAnswers.get(ForeignLeaseGrantAmountReceivedPage)
      val foreignTotalYearPeriods: Option[Int] = request.userAnswers.get(ForeignYearLeaseAmountPage)

      (receivedGrantLeaseAmount, foreignTotalYearPeriods) match {
        case (None, _) =>
          Redirect(routes.LeaseGrantAmountReceivedController.onPageLoad(taxYear, mode))
        case (_, None) =>
          Redirect(routes.ForeignYearLeaseAmountController.onPageLoad(taxYear, mode))
        case (Some(amount), Some(period)) =>
          val preparedForm = request.userAnswers.get(UkAndForeignPropertyForeignPremiumsGrantLeasePage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, taxYear, period, amount, request.user.isAgentMessageKey, mode))
      }
    }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      val receivedGrantLeaseAmount: Option[BigDecimal] = request.userAnswers.get(ForeignLeaseGrantAmountReceivedPage)
      val foreignTotalYearPeriods: Option[Int] = request.userAnswers.get(ForeignYearLeaseAmountPage)

      (receivedGrantLeaseAmount, foreignTotalYearPeriods) match {
        case (None, _) =>
          Future.successful(
            Redirect(routes.LeaseGrantAmountReceivedController.onPageLoad(taxYear, mode))
          )
        case (_, None) =>
          Future.successful(
            Redirect(routes.ForeignYearLeaseAmountController.onPageLoad(taxYear, mode))
          )
        case (Some(amount), Some(period)) =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(formWithErrors, taxYear, period, amount, request.user.isAgentMessageKey, mode)
                  )
                ),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(
                                      request.userAnswers.set(
                                        UkAndForeignPropertyForeignPremiumsGrantLeasePage,
                                        UkAndForeignPropertyForeignPremiumsGrantLease(
                                          value.premiumsOfLeaseGrantAgreed,
                                          Option(
                                            value.premiumsOfLeaseGrant.getOrElse(
                                              UkAndForeignPropertyForeignPremiumsGrantLease
                                                .calculateTaxableAmount(amount, period)
                                            )
                                          )
                                        )
                                      )
                                    )
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(
                  navigator.nextPage(
                    UkAndForeignPropertyForeignPremiumsGrantLeasePage,
                    taxYear,
                    mode,
                    request.userAnswers,
                    updatedAnswers
                  )
                )
            )
      }
    }

}
