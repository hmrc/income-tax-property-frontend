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

package controllers.rentalsandrentaroom

import audit.AuditModel._
import audit.{AuditModel, AuditService}
import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.exceptions.{InternalErrorFailure, SaveJourneyAnswersFailed}
import models.JourneyPath.RentalsAndRentARoomAbout
import models._
import models.requests.DataRequest
import pages.foreign.Country
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.propertyrentals.ClaimPropertyIncomeAllowanceSummary
import viewmodels.checkAnswers.propertyrentals.income.IncomeFromPropertySummary
import viewmodels.checkAnswers.ukrentaroom.{ClaimExpensesOrReliefSummary, JointlyLetSummary, TotalIncomeAmountSummary}
import viewmodels.govuk.all.SummaryListViewModel
import views.html.rentalsandrentaroom.RentalsAndRaRCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class RentalsAndRaRCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  propertySubmissionService: PropertySubmissionService,
  view: RentalsAndRaRCheckYourAnswersView,
  audit: AuditService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val summaryRows = buildSummaryRows(taxYear, request.userAnswers, request.user.isAgentMessageKey)
      val list = SummaryListViewModel(
        rows = summaryRows.flatten
      )
      Ok(view(list, taxYear))
    }

  def onSubmit(taxYear: Int): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val context =
        JourneyContext(taxYear, request.user.mtditid, request.user.nino, RentalsAndRentARoomAbout)

      val rentalsAndRaRAboutMaybe = request.userAnswers.get(RentalsAndRaRAbout)
      sendRentalsAndRaRAbout(taxYear, request, context, rentalsAndRaRAboutMaybe)
    }

  private def buildSummaryRows(taxYear: Int, userAnswers: UserAnswers, isAgent: String)(implicit messages: Messages) =
    Seq(
      JointlyLetSummary.row(taxYear, userAnswers, isAgent, RentalsRentARoom),
      TotalIncomeAmountSummary.row(taxYear, userAnswers, isAgent, RentalsRentARoom),
      ClaimExpensesOrReliefSummary.rows(taxYear, isAgent, userAnswers, RentalsRentARoom).getOrElse(Seq.empty),
      ClaimPropertyIncomeAllowanceSummary.rows(taxYear, userAnswers, isAgent, RentalsRentARoom),
      IncomeFromPropertySummary.row(taxYear, userAnswers, RentalsRentARoom)
    )

  private def sendRentalsAndRaRAbout(
    taxYear: Int,
    request: DataRequest[AnyContent],
    context: JourneyContext,
    aboutMaybe: Option[RentalsAndRaRAbout]
  )(implicit
    hc: HeaderCarrier
  ): Future[Result] =
    aboutMaybe.fold[Future[Result]] {
      logger.error("Rentals and Rent A Room Section is not present in userAnswers")
      Future.failed(InternalErrorFailure("Rentals and Rent A Room Section is not present in userAnswers"))
    } { about =>
      propertySubmissionService
        .saveJourneyAnswers[RentalsAndRaRAbout](context, about)
        .flatMap {
          case Right(_) =>
            auditCYA(taxYear, request, about, isFailed = false)
            Future.successful(
              Redirect(controllers.rentalsandrentaroom.routes.RentalsRaRAboutCompleteController.onPageLoad(taxYear))
            )
          case Left(error) =>
            logger.error(error.toString)
            auditCYA(taxYear, request, about, isFailed = true)
            Future.failed(SaveJourneyAnswersFailed("Failed to save rentals and rent a room about section"))
        }
    }

  private def auditCYA(taxYear: Int, request: DataRequest[AnyContent], about: RentalsAndRaRAbout, isFailed: Boolean)(
    implicit hc: HeaderCarrier
  ): Unit = {

    val auditModel = AuditModel(
      userType = request.user.affinityGroup,
      nino = request.user.nino,
      mtdItId = request.user.mtditid,
      taxYear = taxYear,
      propertyType = AuditPropertyType.UKProperty,
      countryCode = Country.UK.code,
      journeyName = JourneyName.RentalsRentARoom,
      sectionName = SectionName.About,
      accountingMethod = AccountingMethod.Cash,
      isUpdate = false,
      isFailed = isFailed,
      agentReferenceNumber = request.user.agentRef,
      userEnteredDetails = about
    )

    audit.sendAuditEvent(auditModel)
  }

}
