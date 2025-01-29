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
import controllers.exceptions.{NotFoundException, SaveJourneyAnswersFailed}
import models.requests.DataRequest
import models.ukAndForeign.UkAndForeignAbout
import models.{JourneyContext, JourneyPath}
import pages.ukandforeignproperty.{UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage, UkForeignPropertyAboutPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
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
      val ukAndForeignPropertyList =
        SummaryListViewModel(
          rows = Seq(
            TotalPropertyIncomeSummary.row(taxYear = taxYear, answers = request.userAnswers),
            ReportIncomeSummary.row(taxYear = taxYear, request.user.isAgentMessageKey, answers = request.userAnswers),
            UkAndForeignPropertyRentalTypeUkSummary.row(taxYear = taxYear, answers = request.userAnswers),
            ForeignCountriesRentedPropertySummary.rowList(taxYear = taxYear, answers = request.userAnswers),
            UKAndForeignClaimExpensesOrReliefSummary.row(taxYear = taxYear, answers = request.userAnswers),
            ClaimPropertyIncomeAllowanceOrExpensesSummary.row(taxYear = taxYear, answers = request.userAnswers)
          ).flatten
        )

      val isPIA =
        request.userAnswers
          .get(UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage)
          .exists(_.claimPropertyIncomeAllowanceOrExpensesYesNo)

      val ukPropertyList: Option[SummaryList] =
        if (isPIA) {
          Some(
            SummaryListViewModel(
              rows = Seq(
                NonResidentLandlordUKSummary.row(taxYear = taxYear, answers = request.userAnswers),
                UkAndForeignPropertyDeductingTaxFromNonUkResidentLandlordSummary.row(taxYear = taxYear, answers = request.userAnswers),
                UkRentalPropertyIncomeSummary.row(taxYear = taxYear, answers = request.userAnswers),
                BalancingChargeSummary.row(taxYear = taxYear, answers = request.userAnswers),
                UkAndForeignPropertyPremiumForLeaseSummary.row(taxYear = taxYear, answers = request.userAnswers),


                // have you calc the premium
                LeaseGrantAmountReceivedSummary.row(taxYear = taxYear, individualOrAgent = request.user.isAgentMessageKey, answers = request.userAnswers),
                UkYearLeaseAmountSummary.row(taxYear = taxYear, answers = request.userAnswers),
                UKPremiumsGrantLeaseSummary.row(taxYear = taxYear, answers = request.userAnswers),
                ReversePremiumsReceivedSummary.row(taxYear = taxYear, answers = request.userAnswers),
                OtherIncomeFromUkPropertySummary.row(taxYear = taxYear, answers = request.userAnswers)
              ).flatten
            )
          )
        } else {
          None
        }

      val foreignPropertyList: Option[SummaryList] =
        if (isPIA) {
          Some(
            SummaryListViewModel(
              rows = Seq(
                ForeignRentalPropertyIncomeSummary.row(taxYear = taxYear, answers = request.userAnswers),
                UkAndForeignBalancingChargeSummary.row(taxYear = taxYear, answers = request.userAnswers),
                UkAndForeignForeignPremiumsForTheGrantOfALeaseSummary.row(taxYear = taxYear, answers = request.userAnswers),
                UkAndForeignCalculatedForeignPremiumGrantLeaseTaxableSummary.row(taxYear = taxYear, answers = request.userAnswers),
                ForeignLeaseGrantAmountRecievedSummary.row(taxYear = taxYear, answers = request.userAnswers),
                ForeignYearLeaseAmountSummary.row(taxYear = taxYear, answers = request.userAnswers),
                UkAndForeignPropertyPremiumForLeaseSummary.row(taxYear = taxYear, answers = request.userAnswers),
                ForeignOtherIncomeFromForeignPropertySummary.row(taxYear = taxYear, answers = request.userAnswers),
                UkAndForeignPropertyIncomeAllowanceClaimSummary.row(taxYear = taxYear, answers = request.userAnswers)
              ).flatten
            )
          )
        } else {
          None
        }

      Ok(view(ukAndForeignPropertyList, ukPropertyList, foreignPropertyList, taxYear))
    }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers
        .get(UkForeignPropertyAboutPage)
        .map { ukAndForeignAbout =>
          savePropertyAbout(taxYear, request, ukAndForeignAbout)
        }
        .getOrElse {
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
        // TODO redirect to a 'Have you finished this section' page / completion controller
        Future.successful(
          Redirect(
            controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear: Int)
          )
        )
      case Left(error) =>
        logger.error(s"Failed to save uk and foreign property about section: ${error.toString}")
        Future.failed(SaveJourneyAnswersFailed("Failed to save uk and foreign property about section"))
    }
  }
}
