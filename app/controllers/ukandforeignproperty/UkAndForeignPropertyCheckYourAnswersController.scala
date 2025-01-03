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
import models.JourneyPath.{ForeignSelectCountry, UkAndForeignPropertyAbout}
import models.ukAndForeign.UkAndForeignAbout
import models.{ForeignPropertySelectCountry, JourneyContext, Mode, ReportIncome, TotalPropertyIncome}
import pages.ukandforeignproperty.{ReportIncomePage, TotalPropertyIncomePage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
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
      request.userAnswers
        .get(UkAndForeignAbout)
        .map { ukAndForeignAbout =>
          val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, UkAndForeignPropertyAbout)

          propertySubmissionService.saveJourneyAnswers(context, ukAndForeignAbout).flatMap {
            case Right(_) =>
              // TODO redirect to a 'are you finished' page / completion controller
              Future.successful(Redirect(controllers.routes.IndexController.onPageLoad.url))

            case Left(error) =>
              logger.error(s"\n Failed to save UK and foreign property income details section: ${error.toString}")
              Future.failed(SaveJourneyAnswersFailed("Failed to save UK and foreign property income details"))
          }
        }
        .getOrElse {
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

//  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
//    implicit request =>
//      request.userAnswers
//        .get(ForeignPropertySelectCountry)
//        .map { foreignPropertySelectCountry =>
//          val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, ForeignSelectCountry)
//
//          propertySubmissionService.saveJourneyAnswers(context, foreignPropertySelectCountry).flatMap {
//            case Right(_) =>
//              Future.successful(Redirect(controllers.routes.IndexController.onPageLoad.url))
//
//              // TODO redirect to a 'are you finished' page / completion controller
//              Future.successful(Redirect(controllers.routes.IndexController.onPageLoad.url))
//
//            case Left(error) =>
//              logger.error(s"\n Failed to save UK and Foreign Property income details section: ${error.toString}")
//              Future.failed(SaveJourneyAnswersFailed("Failed to save UK and Foreign Property income details"))
//          }
//        }
//        .getOrElse {
//          logger.error(
//            s"Foreign property select country section is not present in userAnswers for userId: ${request.userId} in UK and foreign section."
//          )
//          Future.failed(
//            NotFoundException(
//              "Foreign property select country section is not present in UK and foreign property answers"
//            )
//          )
//        }
//  }

}
