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

package controllers.foreign.structuresbuildingallowance

import controllers.actions._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.foreign.structurebuildingallowance.{ForeignStructureBuildingAllowanceClaimSummary, ForeignStructureBuildingQualifyingAmountSummary, ForeignStructureBuildingQualifyingDateSummary, ForeignStructuresBuildingAllowanceAddressSummary}
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.structurebuildingallowance.ForeignSbaCheckYourAnswersView

import javax.inject.Inject

class ForeignSbaCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ForeignSbaCheckYourAnswersView
) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, countryCode: String, index: Int): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          ForeignStructureBuildingQualifyingDateSummary.row(taxYear, countryCode, index, request.userAnswers),
          ForeignStructureBuildingQualifyingAmountSummary.row(taxYear, countryCode, index, request.userAnswers),
          ForeignStructureBuildingAllowanceClaimSummary.row(taxYear, countryCode, index, request.userAnswers),
          ForeignStructuresBuildingAllowanceAddressSummary.row(taxYear, index, request.userAnswers, countryCode)
        ).flatten
      )
      Ok(view(list, taxYear, countryCode))
    }
}
