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

package controllers.foreign.income

import audit.{AuditModel, AuditService}
import controllers.PropertyDetailsHandler
import controllers.actions._
import controllers.exceptions.SaveJourneyAnswersFailed
import controllers.foreign.income.routes.ForeignIncomeCompleteController
import models._
import models.requests.DataRequest
import pages.foreign.Country
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.{BusinessService, PropertySubmissionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.foreign._
import viewmodels.checkAnswers.foreign.income.ForeignPropertyRentalIncomeSummary
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.income.ForeignPropertyIncomeCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ForeignIncomeCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  propertySubmissionService: PropertySubmissionService,
  audit: AuditService,
  businessService: BusinessService,
  view: ForeignPropertyIncomeCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with PropertyDetailsHandler {

  def onPageLoad(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          ForeignPropertyRentalIncomeSummary.row(taxYear, request.userAnswers, countryCode),
          PremiumsGrantLeaseYNSummary.row(request.userAnswers, taxYear, countryCode, request.user.isAgentMessageKey),
          CalculatedPremiumLeaseTaxableSummary.row(taxYear, countryCode, request.userAnswers, request.user.isAgentMessageKey),
          CalculatedPremiumLeaseTaxableAmountSummary.row(taxYear, countryCode, request.userAnswers),
          ForeignReceivedGrantLeaseAmountSummary.row(taxYear, countryCode, request.userAnswers),
          TwelveMonthPeriodsInLeaseSummary.row(taxYear, countryCode, request.userAnswers),
          ForeignPremiumsGrantLeaseSummary.row(taxYear, countryCode, request.userAnswers),
          ForeignOtherIncomeFromPropertySummary.row(taxYear, countryCode, request.userAnswers)
        ).flatten
      )

      Ok(view(list, taxYear, countryCode))
    }

  def onSubmit(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers
        .get(ReadForeignPropertyIncome(countryCode))
        .fold {
          val errorMsg =
            s"Foreign property income section is missing for userId: ${request.userId}, taxYear: $taxYear, countryCode: $countryCode"
          logger.error(errorMsg)
          Future.successful(NotFound(errorMsg))
        } { foreignPropertyIncome =>
          saveForeignPropertyIncome(taxYear, request, foreignPropertyIncome, countryCode)
        }
    }
  private def saveForeignPropertyIncome(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignPropertyIncome: ForeignPropertyIncome,
    countryCode: String
  )(implicit hc: HeaderCarrier): Future[Result] =
    withForeignPropertyDetails(businessService, request.user.nino, request.user.mtditid) { propertyDetails =>
      val context =
        JourneyContext(taxYear, request.user.mtditid, request.user.nino, JourneyPath.ForeignPropertyIncome)
      val accrualsOrCash = propertyDetails.accrualsOrCash.getOrElse(true)

      propertySubmissionService
        .saveForeignPropertyJourneyAnswers(context, foreignPropertyIncome)
        .map {
          case Right(_) => Redirect(ForeignIncomeCompleteController.onPageLoad(taxYear, countryCode))
          case Left(error) =>
            logger.error(s"Failed to save Foreign Income section: ${error.toString}")
            throw SaveJourneyAnswersFailed("Failed to save Foreign Income section")
        }
        .andThen {
          case Success(_) =>
            auditCYA(taxYear, request, foreignPropertyIncome, isFailed = false, accrualsOrCash)
          case Failure(_) =>
            auditCYA(taxYear, request, foreignPropertyIncome, isFailed = true, accrualsOrCash)
        }
    }

  private def auditCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignPropertyIncome: ForeignPropertyIncome,
    isFailed: Boolean,
    accrualsOrCash: Boolean
  )(implicit
    hc: HeaderCarrier
  ): Unit = {
    val auditModel = AuditModel(
      request.user.affinityGroup,
      request.user.nino,
      request.user.mtditid,
      taxYear,
      propertyType = AuditPropertyType.ForeignProperty,
      countryCode = Country.UK.code,
      journeyName = JourneyName.ForeignProperty,
      sectionName = SectionName.ForeignPropertyIncome,
      accountingMethod = if (accrualsOrCash) AccountingMethod.Traditional else AccountingMethod.Cash,
      isUpdate = false,
      isFailed = isFailed,
      request.user.agentRef,
      foreignPropertyIncome
    )

    audit.sendAuditEvent(auditModel)
  }
}
