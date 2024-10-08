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

package controllers.enhancedstructuresbuildingallowance

import audit.{AuditService, RentalsAuditModel}
import controllers.actions._
import models.requests.DataRequest
import models.{EsbasWithSupportingQuestions, JourneyContext, PropertyType}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.enhancedstructurebuildingallowance._
import viewmodels.govuk.summarylist._
import views.html.enhancedstructuresbuildingallowance.EsbaCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EsbaCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  propertySubmissionService: PropertySubmissionService,
  val controllerComponents: MessagesControllerComponents,
  audit: AuditService,
  view: EsbaCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, index: Int, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          EsbaQualifyingDateSummary.row(taxYear, index, request.userAnswers, propertyType),
          EsbaQualifyingAmountSummary.row(taxYear, index, request.userAnswers, propertyType),
          EsbaClaimAmountSummary.row(taxYear, index, request.userAnswers, propertyType),
          EsbaAddressSummary.row(taxYear, index, request.userAnswers, propertyType)
        ).flatten
      )
      Ok(view(list, taxYear, propertyType))
    }
}
