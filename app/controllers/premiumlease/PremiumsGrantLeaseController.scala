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
import models.Mode
import navigation.Navigator
import pages.premiumlease.PremiumsGrantLeasePage
import pages.premiumlease.{PremiumsGrantLeasePage, RecievedGrantLeaseAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PremiumsGrantLeaseController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              sessionRepository: SessionRepository,
                                              navigator: Navigator,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: PremiumsGrantLeaseFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: PremiumsGrantLeaseView
                                            )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val receivedGrantLeaseAmount: Option[Int] = request.userAnswers.get(RecievedGrantLeaseAmountPage)
      val totalYearPeriods: Option[Int] = request.userAnswers.get(YearLeaseAmountPage)

      (receivedGrantLeaseAmount, totalYearPeriods) match {
        case (None, _) => Redirect(routes.RecievedGrantLeaseAmountController.onPageLoad(taxYear, mode))
        case (_, None) => Redirect(routes.RecievedGrantLeaseAmountController.onPageLoad(taxYear, mode))
        case (amount, period) =>
          val preparedForm = request.userAnswers.get(PremiumsGrantLeasePage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, taxYear, amount.get, period.get, mode))
      }
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val receivedGrantLeaseAmount: Option[Int] = request.userAnswers.get(RecievedGrantLeaseAmountPage)
      val totalYearPeriods: Option[Int] = request.userAnswers.get(YearLeaseAmountPage)

      (receivedGrantLeaseAmount, totalYearPeriods) match {
        case (None, _) => Future.successful(Redirect(routes.RecievedGrantLeaseAmountController.onPageLoad(taxYear, mode)))
        case (_, None) => Future.successful(Redirect(routes.YearLeaseAmountController.onPageLoad(taxYear, mode)))
        case (amount, period) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, taxYear, amount.get, period.get, mode))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(PremiumsGrantLeasePage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(PremiumsGrantLeasePage, taxYear, mode, updatedAnswers))
          )
      }
  }
}
