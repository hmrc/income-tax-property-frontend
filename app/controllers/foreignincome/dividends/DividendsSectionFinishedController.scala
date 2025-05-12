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

package controllers.foreignincome.dividends

import controllers.ControllerUtils.statusForPage
import controllers.actions._
import controllers.exceptions.InternalErrorFailure
import forms.foreignincome.dividends.DividendsSectionFinishedFormProvider
import models.JourneyPath.ForeignIncomeDividends
import models.{JourneyContext, NormalMode}
import navigation.ForeignIncomeNavigator
import pages.foreignincome.dividends.DividendsSectionFinishedPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import service.JourneyAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.foreignincome.dividends.DividendsSectionFinishedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DividendsSectionFinishedController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: ForeignIncomeNavigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: DividendsSectionFinishedFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: DividendsSectionFinishedView,
                                         journeyAnswersService: JourneyAnswersService
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(DividendsSectionFinishedPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, taxYear))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(DividendsSectionFinishedPage, value))
            _              <- sessionRepository.set(updatedAnswers)
            status <- journeyAnswersService
              .setForeignIncomeStatus(
                JourneyContext(
                  taxYear = taxYear,
                  mtditid = request.user.mtditid,
                  nino = request.user.nino,
                  journeyPath = ForeignIncomeDividends
              ),
                status = statusForPage(value),
                user = request.user
              )
              .flatMap {
                case Right(_) =>
                  Future.successful(
                    Redirect(
                      navigator
                        .nextPage(
                          DividendsSectionFinishedPage,
                          taxYear,
                          NormalMode,
                          request.userAnswers,
                          updatedAnswers
                        )
                    )
                  )
                case Left(_) =>
                  Future.failed(
                    InternalErrorFailure(s"Failed to save the status for Foreign Income Dividend section in tax year: $taxYear")
                  )
              }
          } yield status
      )
  }
}
