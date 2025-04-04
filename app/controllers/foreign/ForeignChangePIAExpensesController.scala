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

package controllers.foreign

import controllers.actions._
import controllers.exceptions.InternalErrorFailure
import models.JourneyPath.{ForeignPropertyAdjustments, ForeignPropertyAllowances, ForeignPropertyExpenses, ForeignPropertyIncome, ForeignPropertyTax, ForeignSelectCountry, ForeignStructureBuildingAllowance}

import javax.inject.Inject
import models.{DeleteJourneyAnswers, JourneyContext, Mode}
import models.requests.DataRequest
import navigation.ForeignPropertyNavigator
import pages.foreign.ForeignChangePIAExpensesPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.foreign.ForeignChangePIAExpensesView

import scala.concurrent.{ExecutionContext, Future}

class ForeignChangePIAExpensesController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: ForeignPropertyNavigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         val controllerComponents: MessagesControllerComponents,
                                         propertySubmissionService: PropertySubmissionService,
                                         view: ForeignChangePIAExpensesView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      Ok(view(taxYear, mode))
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      deleteJourneyAnswers(taxYear, request) {
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.set(ForeignChangePIAExpensesPage, true))
          _              <- sessionRepository.set(updatedAnswers)
        } yield
          Redirect(navigator.nextPage(ForeignChangePIAExpensesPage, taxYear, mode, request.userAnswers, updatedAnswers))
      }
  }

  def deleteJourneyAnswers(
    taxYear: Int,
    request: DataRequest[AnyContent])(
    block: => Future[Result]
  )(implicit hc: HeaderCarrier
  ): Future[Result] = {
    val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, ForeignSelectCountry)
    val deleteJourneyAnswers = DeleteJourneyAnswers(journeyNames = Seq(
      ForeignPropertyTax,
      ForeignPropertyIncome,
      ForeignPropertyExpenses,
      ForeignPropertyAllowances,
      ForeignPropertyAdjustments,
      ForeignStructureBuildingAllowance
    ).map(_.toString))
    propertySubmissionService.deleteForeignPropertyJourneyAnswers(context, deleteJourneyAnswers)
      .flatMap {
        case Left(e) =>
          logger.error(s"[ForeignChangePIAExpensesController][deleteJourneyAnswers]: Couldn't delete answers from income-tax-property, ServiceError: ${e.toString}")
          Future.failed(InternalErrorFailure("Couldn't delete answers from income-tax-property"))
        case Right(_) => block
      }
  }
}
