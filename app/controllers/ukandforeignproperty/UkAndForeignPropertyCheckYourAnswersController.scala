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

package controllers.ukandforeignproperty

import controllers.actions._
import controllers.exceptions.{NotFoundException, SaveJourneyAnswersFailed}
import models.JourneyPath.UkAndForeignPropertyAbout
import models.ukAndForeign.UkAndForeignAbout
import models.{JourneyContext, Mode, ReportIncome, TotalPropertyIncome}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsSuccess, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.PropertySubmissionService

import scala.concurrent.Future
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.ReportIncomeSummary
import viewmodels.checkAnswers.ukandforeignproperty.TotalPropertyIncomeSummary
import viewmodels.govuk.all.SummaryListViewModel
import views.html.ukandforeignproperty.UkAndForeignPropertyCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class UkAndForeignPropertyCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  propertySubmissionService: PropertySubmissionService,
  view: UkAndForeignPropertyCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          TotalPropertyIncomeSummary.row(taxYear = taxYear, answers = request.userAnswers),
          ReportIncomeSummary.row(taxYear = taxYear, request.user.isAgentMessageKey, answers = request.userAnswers)
        ).flatten
      )

      Ok(view(list, taxYear, mode))
    }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val totalPropertyIncome = (request.userAnswers.data \ "totalPropertyIncome").validate[TotalPropertyIncome]
      val reportIncome = (request.userAnswers.data \ "reportIncome").validate[ReportIncome]

      (totalPropertyIncome, reportIncome) match {
        case (JsSuccess(totalIncome, _), JsSuccess(report, _)) =>
          val ukAndForeignAbout = UkAndForeignAbout(totalIncome, Some(report))
          val context = JourneyContext(
            taxYear = taxYear,
            mtditid = request.user.mtditid,
            nino = request.user.nino,
            journeyPath = UkAndForeignPropertyAbout
          )
          propertySubmissionService.saveJourneyAnswers(context, ukAndForeignAbout).flatMap {
            case Right(_) =>
              // TODO redirect to a 'are you finished' page / completion controller
              Future.successful(Redirect(controllers.routes.IndexController.onPageLoad.url))

            case Left(error) =>
              logger.error(s"Failed to save UK and foreign property income details section: ${error.toString}")
              Future.failed(SaveJourneyAnswersFailed("Failed to save UK and foreign property income details"))
          }
        case _ =>
          logger.error(
            s"Uk and foreign about section is not present in userAnswers for userId: ${request.userId} in UK and foreign section."
          )
          Future.failed(
            NotFoundException(
              "Uk and foreign about section is not present in UK and foreign property answers"
            )
          )
      }
  }
}
