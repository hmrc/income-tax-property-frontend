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

package controllers.propertyrentals

import audit.{AuditModel, AuditService, PropertyRentalsAbout}
import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes
import models.requests.DataRequest
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.propertyrentals.{ClaimPropertyIncomeAllowanceSummary, ExpensesLessThan1000Summary}
import viewmodels.govuk.summarylist._
import views.html.propertyrentals.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class PropertyRentalsCheckYourAnswersController @Inject()(
                                                           override val messagesApi: MessagesApi,
                                                           identify: IdentifierAction,
                                                           getData: DataRetrievalAction,
                                                           requireData: DataRequiredAction,
                                                           val controllerComponents: MessagesControllerComponents,
                                                           view: CheckYourAnswersView,
                                                           audit: AuditService,
                                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val list = SummaryListViewModel(
        rows = Seq(
          ExpensesLessThan1000Summary.row(taxYear, request.userAnswers, request.user.isAgentMessageKey),
          ClaimPropertyIncomeAllowanceSummary.row(taxYear, request.userAnswers, request.user.isAgentMessageKey)
        ).flatten
      )

      Ok(view(list, taxYear))
  }


  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(PropertyRentalsAbout) match {
        case Some(propertyRentalsAbout) =>
          auditCYA(taxYear, request, propertyRentalsAbout)
        case None =>
          logger.error("PropertyAbout Section is not present in userAnswers")
      }
      Future.successful(Redirect(routes.SummaryController.show(taxYear)))
  }

  private def auditCYA(taxYear: Int, request: DataRequest[AnyContent], propertyAbout: PropertyRentalsAbout)(implicit hc: HeaderCarrier): Unit = {
    val auditModel = AuditModel(
      request.user.nino,
      request.user.affinityGroup,
      request.user.mtditid,
      taxYear,
      isUpdate = false,
      "PropertyRentalsAbout",
      propertyAbout)

    audit.sendPropertyAboutAudit(auditModel)
  }
}
