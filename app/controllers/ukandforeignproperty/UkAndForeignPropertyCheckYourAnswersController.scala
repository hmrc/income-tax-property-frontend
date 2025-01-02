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
import models.Mode
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
//import service.PropertySubmissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.ReportIncomeSummary
import viewmodels.checkAnswers.ukandforeignproperty.TotalPropertyIncomeSummary
import viewmodels.govuk.all.SummaryListViewModel
import views.html.ukandforeignproperty.UkAndForeignPropertyCheckYourAnswersView

import javax.inject.Inject
//import scala.concurrent.ExecutionContext

class UkAndForeignPropertyCheckYourAnswersController @Inject() (
                                                       override val messagesApi: MessagesApi,
                                                       identify: IdentifierAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       //propertySubmissionService: PropertySubmissionService,
                                                       view: UkAndForeignPropertyCheckYourAnswersView
                                                     )//(implicit ec: ExecutionContext)
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
}
