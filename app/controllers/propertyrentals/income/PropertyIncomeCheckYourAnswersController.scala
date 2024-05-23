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

package controllers.propertyrentals.income

import audit.{AuditModel, AuditService, PropertyRentalsIncome}
import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes
import models.requests.DataRequest
import pages.PageConstants
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.premiumlease._
import viewmodels.checkAnswers.propertyrentals.income._
import viewmodels.govuk.summarylist._
import views.html.propertyrentals.income.IncomeCheckYourAnswersView

import scala.concurrent.Future

class PropertyIncomeCheckYourAnswersController @Inject()(
                                                          override val messagesApi: MessagesApi,
                                                          identify: IdentifierAction,
                                                          getData: DataRetrievalAction,
                                                          requireData: DataRequiredAction,
                                                          val controllerComponents: MessagesControllerComponents,
                                                          view: IncomeCheckYourAnswersView,
                                                          audit: AuditService
                                                        ) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val list = SummaryListViewModel(
        rows = Seq(
          IsNonUKLandlordSummary.row(taxYear, request.userAnswers),
          DeductingTaxSummary.row(taxYear, request.userAnswers),
          IncomeFromPropertyRentalsSummary.row(taxYear, request.userAnswers),
          LeasePremiumPaymentSummary.row(taxYear, request.userAnswers),
          CalculatedFigureYourselfSummary.row(taxYear, request.userAnswers),
          ReceivedGrantLeaseAmountSummary.row(taxYear, request.userAnswers),
          YearLeaseAmountSummary.row(taxYear, request.userAnswers),
          PremiumsGrantLeaseSummary.row(taxYear, request.userAnswers),
          ReversePremiumsReceivedSummary.row(taxYear, request.userAnswers),
          OtherIncomeFromPropertySummary.row(taxYear, request.userAnswers)
        ).flatten
      )

      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(PropertyRentalsIncome) match {
        case Some(propertyRentalsIncome) =>
          auditCYA(taxYear, request, propertyRentalsIncome)
        case None =>
          logger.error(s"${PageConstants.propertyRentalsIncome} section is not present in userAnswers")
      }
      Future.successful(Redirect(routes.SummaryController.show(taxYear)))
  }

  private def auditCYA(taxYear: Int, request: DataRequest[AnyContent], propertyRentalsIncome: PropertyRentalsIncome)(implicit hc: HeaderCarrier): Unit = {
    val auditModel = AuditModel(
      request.user.nino,
      request.user.affinityGroup,
      request.user.mtditid,
      agentReferenceNumber = request.user.agentRef,
      taxYear,
      isUpdate = false,
      "PropertyRentalsIncome",
      propertyRentalsIncome)

    audit.sendRentalsAuditEvent(auditModel)
  }
}
