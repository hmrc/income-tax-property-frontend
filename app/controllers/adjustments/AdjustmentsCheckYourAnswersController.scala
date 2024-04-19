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

package controllers.adjustments

import audit.Adjustments._
import audit.{Adjustments, AuditModel, AuditService}
import controllers.actions._
import models.requests.DataRequest
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.adjustments._
import viewmodels.govuk.summarylist._
import views.html.adjustments.AdjustmentsCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.Future

class AdjustmentsCheckYourAnswersController @Inject()(
                                                       override val messagesApi: MessagesApi,
                                                       identify: IdentifierAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       audit: AuditService,
                                                       view: AdjustmentsCheckYourAnswersView
                                                     ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val list = SummaryListViewModel(
        rows = Seq(
          PrivateUseAdjustmentSummary.row(taxYear, request.userAnswers),
          BalancingChargeSummary.row(taxYear, request.userAnswers),
          PropertyIncomeAllowanceSummary.row(taxYear, request.userAnswers),
          RenovationAllowanceBalancingChargeSummary.row(taxYear, request.userAnswers),
          ResidentialFinanceCostSummary.row(taxYear, request.userAnswers),
          UnusedResidentialFinanceCostSummary.row(taxYear, request.userAnswers)
        ).flatten
      )

      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(Adjustments) match {
        case Some(propertyAbout) =>
          auditCYA(taxYear, request, propertyAbout)
        case None =>
          logger.error("Adjustments Section is not present in userAnswers")
      }
      Future.successful(Redirect(controllers.routes.SummaryController.show(taxYear)))
  }

  private def auditCYA(taxYear: Int, request: DataRequest[AnyContent], adjustments: Adjustments)(implicit hc: HeaderCarrier): Unit = {
    val auditModel = AuditModel(
      request.user.nino,
      request.user.affinityGroup,
      request.user.mtditid,
      taxYear,
      isUpdate = false,
      "PropertyRentalsAdjustments",
      adjustments
    )

    audit.sendPropertyAboutAudit(auditModel)
  }
}
