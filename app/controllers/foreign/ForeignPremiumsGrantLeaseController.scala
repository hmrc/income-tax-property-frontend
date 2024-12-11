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

package controllers.foreign

import controllers.actions._
import forms.foreign.ForeignPremiumsGrantLeaseFormProvider
import models.{ForeignPremiumsGrantLease, Mode}
import navigation.ForeignPropertyNavigator
import pages.foreign.{ForeignPremiumsGrantLeasePage, ForeignReceivedGrantLeaseAmountPage, TwelveMonthPeriodsInLeasePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.foreign.ForeignPremiumsGrantLeaseView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignPremiumsGrantLeaseController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ForeignPropertyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ForeignPremiumsGrantLeaseFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ForeignPremiumsGrantLeaseView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, countryCode: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)

      val receivedGrantLeaseAmount: Option[BigDecimal] =
        request.userAnswers.get(ForeignReceivedGrantLeaseAmountPage(countryCode))
      val foreignTotalYearPeriods: Option[Int] = request.userAnswers.get(TwelveMonthPeriodsInLeasePage(countryCode))

      (receivedGrantLeaseAmount, foreignTotalYearPeriods) match {
        case (None, _) =>
          Redirect(routes.ForeignReceivedGrantLeaseAmountController.onPageLoad(taxYear, countryCode, mode))
        case (_, None) => Redirect(routes.TwelveMonthPeriodsInLeaseController.onPageLoad(taxYear, countryCode, mode))
        case (Some(amount), Some(period)) =>
          val preparedForm = request.userAnswers.get(ForeignPremiumsGrantLeasePage(countryCode)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, taxYear, period, amount, request.user.isAgentMessageKey, countryCode, mode))
      }
    }

  def onSubmit(taxYear: Int, countryCode: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      val receivedGrantLeaseAmount: Option[BigDecimal] =
        request.userAnswers.get(ForeignReceivedGrantLeaseAmountPage(countryCode))
      val foreignTotalYearPeriods: Option[Int] = request.userAnswers.get(TwelveMonthPeriodsInLeasePage(countryCode))

      (receivedGrantLeaseAmount, foreignTotalYearPeriods) match {
        case (None, _) =>
          Future.successful(
            Redirect(routes.ForeignReceivedGrantLeaseAmountController.onPageLoad(taxYear, countryCode, mode))
          )
        case (_, None) =>
          Future.successful(Redirect(routes.TwelveMonthPeriodsInLeaseController.onPageLoad(taxYear, countryCode, mode)))
        case (Some(amount), Some(period)) =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(formWithErrors, taxYear, period, amount, request.user.isAgentMessageKey, countryCode, mode)
                  )
                ),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(
                                      request.userAnswers.set(
                                        ForeignPremiumsGrantLeasePage(countryCode),
                                        ForeignPremiumsGrantLease(
                                          value.premiumsOfLeaseGrantAgreed,
                                          Some(
                                            value.premiumsOfLeaseGrant.getOrElse(
                                              ForeignPremiumsGrantLease.calculateTaxableAmount(amount, period)
                                            )
                                          )
                                        )
                                      )
                                    )
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(
                  navigator.nextPage(
                    ForeignPremiumsGrantLeasePage(countryCode),
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
