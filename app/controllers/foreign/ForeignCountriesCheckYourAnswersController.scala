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

package controllers.foreign

import controllers.actions._
import controllers.exceptions.InternalErrorFailure
import models.JourneyPath.ForeignSelectCountry
import models.requests.DataRequest
import models.{ForeignPropertiesSelectCountry, ForeignTotalIncome, JourneyContext}
import pages.foreign.{Country, IncomeSourceCountries}
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.PropertyIncomeReportSummary
import viewmodels.checkAnswers.foreign._
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.ForeignCountriesCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.Future

class ForeignCountriesCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  propertySubmissionService: PropertySubmissionService,
  view: ForeignCountriesCheckYourAnswersView
) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          TotalIncomeSummary.row(taxYear, request.userAnswers),
          PropertyIncomeReportSummary.row(taxYear, request.user.isAgentMessageKey, request.userAnswers),
          CountriesRentedPropertySummary.rowList(taxYear, request.userAnswers),
          ClaimPropertyIncomeAllowanceOrExpensesSummary.row(taxYear, request.userAnswers)
        ).flatten
      )
      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val foreignPropertiesSelectCountryOpt: Option[ForeignPropertiesSelectCountry] =
        for {
          countryCodes: Array[Country]           <- request.userAnswers.get(IncomeSourceCountries)
          foreignTotalIncome: ForeignTotalIncome <- request.userAnswers.get(pages.foreign.TotalIncomePage)
          claimPropertyIncomeAllowanceOrExpenses: Boolean <-
            request.userAnswers.get(pages.foreign.ClaimPropertyIncomeAllowanceOrExpensesPage)
        } yield ForeignPropertiesSelectCountry(
          countryCodes.toList.map(_.code),
          foreignTotalIncome.toString,
          claimPropertyIncomeAllowanceOrExpenses.toString
        )

      foreignPropertiesSelectCountryOpt match {
        case Some(foreignPropertiesSelectCountry) =>
          saveForeignCountriesInformation(taxYear, request, foreignPropertiesSelectCountry)
        case None =>
          logger.error("Foreign select country section is not present in userAnswers")
          Future.failed(
            InternalErrorFailure("Foreign select country section is not present in userAnswers")
          )
      }
  }

  private def saveForeignCountriesInformation(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignCountryAbout: ForeignPropertiesSelectCountry
  )(implicit hc: HeaderCarrier): Future[Result] = {
    val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, ForeignSelectCountry)
    propertySubmissionService.saveJourneyAnswers(context, foreignCountryAbout)
    Future.successful(
      Redirect(controllers.foreign.routes.ForeignSelectCountriesCompleteController.onPageLoad(taxYear))
    )

  }
}
