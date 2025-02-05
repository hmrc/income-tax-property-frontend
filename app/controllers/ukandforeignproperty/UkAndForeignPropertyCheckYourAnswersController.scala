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

package controllers.ukandforeignproperty

import controllers.actions._
import controllers.exceptions.NotFoundException
import models.ReportIncome.WantToReport
import models.UkAndForeignPropertyRentalTypeUk.PropertyRentals
import models.requests.DataRequest
import models.ukAndForeign.{AboutForeign, AboutUk, AboutUkAndForeign, UkAndForeignAbout}
import models.{JourneyContext, JourneyPath, UserAnswers}
import pages.UkAndForeignPropertyRentalTypeUkPage
import pages.ukandforeignproperty.{ReportIncomePage, UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.PropertySubmissionService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.ukandforeignproperty._
import viewmodels.checkAnswers.{ReportIncomeSummary, UkAndForeignPropertyRentalTypeUkSummary}
import viewmodels.govuk.all.SummaryListViewModel
import views.html.ukandforeignproperty.UkAndForeignPropertyCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

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

  def onPageLoad(taxYear: Int): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val reportIncome: Boolean = request.userAnswers.get(ReportIncomePage) match {
        case Some(reportIncome) => reportIncome == WantToReport
        case None               => true
      }
      val ukAndForeignPropertyList =
        getUkAndForeignSummaryList(taxYear, reportIncome, request.user.isAgentMessageKey, request.userAnswers)

      val hasPropertyRentals: Boolean =
        request.userAnswers.get(UkAndForeignPropertyRentalTypeUkPage).forall(_.contains(PropertyRentals))
      val isPIA: Boolean = request.userAnswers
        .get(UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage)
        .exists(_.claimPropertyIncomeAllowanceOrExpensesYesNo)

      val ukPropertyList: Option[SummaryList] = (reportIncome, isPIA) match {
        case (true, true) =>
          if (hasPropertyRentals) {
            Some(getUkSummaryList(taxYear, request.user.isAgentMessageKey, request.userAnswers))
          } else {
            None
          }
        case (_, _) => None
      }

      val foreignPropertyList: Option[SummaryList] = (reportIncome, isPIA) match {
        case (true, true) => Some(getForeignSummaryList(taxYear, request.user.isAgentMessageKey, request.userAnswers))
        case (_, _)       => None
      }

      Ok(view(ukAndForeignPropertyList, ukPropertyList, foreignPropertyList, taxYear))
    }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val maybeUkAndForeignAbout = request.userAnswers.get(AboutUkAndForeign)
      val maybeUkAbout = request.userAnswers.get(AboutUk)
      val maybeForeignAbout = request.userAnswers.get(AboutForeign)

      (maybeUkAndForeignAbout, maybeUkAbout, maybeForeignAbout) match {
        case (Some(ukAndForeign), _, _) =>
          savePropertyAbout(taxYear, request, UkAndForeignAbout(ukAndForeign, maybeUkAbout, maybeForeignAbout))

        case _ =>
          logger.error(
            s"Uk and foreign property about section is not present in userAnswers for userId: ${request.userId}"
          )
          Future.failed(NotFoundException("Uk and foreign property about section is not present in userAnswers"))
      }
  }

  private def savePropertyAbout(taxYear: Int, request: DataRequest[AnyContent], ukAndForeignAbout: UkAndForeignAbout)(
    implicit hc: HeaderCarrier
  ): Future[Result] = {

    val context =
      JourneyContext(taxYear, request.user.mtditid, request.user.nino, JourneyPath.UkAndForeignPropertyAbout)

    propertySubmissionService.saveJourneyAnswers(context, ukAndForeignAbout).flatMap {
      case Right(_) =>
        // TODO : Update logic once backend endpoint has been created
        Future.successful(
          Redirect(
            routes.SectionCompleteController.onPageLoad(taxYear: Int)
          )
        )
      case Left(error) =>
        logger.error(s"Failed to save uk and foreign property about section: ${error.toString}")
        // TODO : Update logic once backend endpoint has been created - throw error
        //  Future.failed(SaveJourneyAnswersFailed("Failed to save uk and foreign property about section"))
        Future.successful(
          Redirect(
            routes.SectionCompleteController.onPageLoad(taxYear: Int)
          )
        )
    }
  }

  private def getUkAndForeignSummaryList(
    taxYear: Int,
    reportIncome: Boolean,
    individualOrAgent: String,
    userAnswers: UserAnswers
  )(implicit messages: Messages): SummaryList =
    if (reportIncome) {
      SummaryListViewModel(
        rows = Seq(
          TotalPropertyIncomeSummary.row(taxYear, userAnswers),
          UkAndForeignPropertyRentalTypeUkSummary.row(taxYear, userAnswers, individualOrAgent),
          ForeignCountriesRentedPropertySummary.rowList(taxYear, userAnswers, individualOrAgent),
          UKAndForeignClaimExpensesOrReliefSummary.row(taxYear, userAnswers),
          ClaimPropertyIncomeAllowanceOrExpensesSummary.row(taxYear, userAnswers)
        ).flatten
      )
    } else {
      SummaryListViewModel(
        rows = Seq(
          TotalPropertyIncomeSummary.row(taxYear, userAnswers),
          ReportIncomeSummary.row(taxYear, individualOrAgent, userAnswers)
        ).flatten
      )
    }

  private def getUkSummaryList(
    taxYear: Int,
    individualOrAgent: String,
    userAnswers: UserAnswers
  )(implicit messages: Messages): SummaryList =
    SummaryListViewModel(
      rows = Seq(
        NonResidentLandlordUKSummary.row(taxYear, userAnswers),
        UkAndForeignPropertyDeductingTaxFromNonUkResidentLandlordSummary.row(taxYear, userAnswers),
        UkRentalPropertyIncomeSummary.row(taxYear, userAnswers),
        BalancingChargeSummary.row(taxYear, userAnswers),
        UkPremiumForLeaseSummary.row(taxYear, userAnswers),
        UkPremiumGrantLeaseTaxSummary.row(taxYear, userAnswers),
        UKLeaseGrantAmountReceivedSummary.row(taxYear, userAnswers, individualOrAgent),
        UkYearLeaseAmountSummary.row(taxYear, userAnswers),
        UKPremiumsGrantLeaseSummary.row(taxYear, userAnswers),
        ReversePremiumsReceivedSummary.row(taxYear, userAnswers),
        OtherIncomeFromUkPropertySummary.row(taxYear, userAnswers)
      ).flatten
    )

  private def getForeignSummaryList(
    taxYear: Int,
    individualOrAgent: String,
    userAnswers: UserAnswers
  )(implicit messages: Messages): SummaryList =
    SummaryListViewModel(
      rows = Seq(
        ForeignRentalPropertyIncomeSummary.row(taxYear, userAnswers),
        UkAndForeignBalancingChargeSummary.row(userAnswers, taxYear),
        UkAndForeignForeignPremiumsForTheGrantOfALeaseSummary.row(taxYear, userAnswers),
        UkAndForeignCalculatedForeignPremiumGrantLeaseTaxableSummary.rows(taxYear, userAnswers).getOrElse(Seq.empty),
        LeaseGrantAmountReceivedSummary.row(taxYear, individualOrAgent, userAnswers),
        ForeignYearLeaseAmountSummary.row(taxYear, userAnswers),
        ForeignPremiumsGrantLeaseSummary.row(taxYear, userAnswers),
        ForeignOtherIncomeFromForeignPropertySummary.row(taxYear, userAnswers),
        UkAndForeignPropertyIncomeAllowanceClaimSummary.row(taxYear, userAnswers)
      ).flatten
    )
}
