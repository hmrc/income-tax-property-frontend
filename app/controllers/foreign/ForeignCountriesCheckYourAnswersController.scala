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

import audit.{AuditModel, AuditService, ForeignPropertySelectCountry => ForeignPropertySelectCountryAudit}
import controllers.PropertyDetailsHandler
import controllers.actions._
import controllers.exceptions.SaveJourneyAnswersFailed
import controllers.foreign.routes.ForeignSelectCountriesCompleteController
import models.requests.DataRequest
import models.{AccountingMethod, AuditPropertyType, ForeignPropertySelectCountry, JourneyContext, JourneyName, JourneyPath, SectionName}
import pages.foreign.Country
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.{BusinessService, PropertySubmissionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.language.LanguageUtils
import viewmodels.checkAnswers.PropertyIncomeReportSummary
import viewmodels.checkAnswers.foreign._
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.ForeignCountriesCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ForeignCountriesCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  propertySubmissionService: PropertySubmissionService,
  audit: AuditService,
  businessService: BusinessService,
  view: ForeignCountriesCheckYourAnswersView,
  languageUtils: LanguageUtils
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with PropertyDetailsHandler {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          TotalIncomeSummary.row(taxYear, request.userAnswers),
          PropertyIncomeReportSummary.row(taxYear, request.user.isAgentMessageKey, request.userAnswers),
          CountriesRentedPropertySummary
            .rowList(taxYear, request.userAnswers, languageUtils.getCurrentLang.locale.toString),
          ClaimPropertyIncomeAllowanceOrExpensesSummary.row(taxYear, request.userAnswers)
        ).flatten
      )
      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers
        .get(ForeignPropertySelectCountry)
        .fold {
          val errorMsg =
            s"Foreign property select country section is missing for userId: ${request.userId}, taxYear: $taxYear"
          logger.error(errorMsg)
          Future.successful(NotFound(errorMsg))
        } { foreignPropertySelectCountry =>
          saveForeignPropertySelectCountry(taxYear, request, foreignPropertySelectCountry)
        }
  }

  private def saveForeignPropertySelectCountry(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignPropertySelectCountry: ForeignPropertySelectCountry
  )(implicit hc: HeaderCarrier): Future[Result] =
    withForeignPropertyDetails(businessService, request.user.nino, request.user.mtditid) { propertyDetails =>
      val context =
        JourneyContext(taxYear, request.user.mtditid, request.user.nino, JourneyPath.ForeignSelectCountry)
      val accrualsOrCash = propertyDetails.accrualsOrCash.getOrElse(true)

      propertySubmissionService
        .saveForeignPropertyJourneyAnswers(context, foreignPropertySelectCountry)
        .map {
          case Right(_) => Redirect(ForeignSelectCountriesCompleteController.onPageLoad(taxYear))
          case Left(error) =>
            logger.error(s"Failed to save Foreign Property Select Country section: ${error.toString}")
            throw SaveJourneyAnswersFailed("Failed to save Foreign Property Select Country section")
        }
        .andThen {
          case Success(_) =>
            auditCYA(taxYear, request, foreignPropertySelectCountry, isFailed = false, accrualsOrCash)
          case Failure(_) =>
            auditCYA(taxYear, request, foreignPropertySelectCountry, isFailed = true, accrualsOrCash)
        }
    }

  private def auditCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignPropertySelectCountry: ForeignPropertySelectCountry,
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
      sectionName = SectionName.ForeignPropertySelectCountry,
      accountingMethod = if (accrualsOrCash) AccountingMethod.Traditional else AccountingMethod.Cash,
      isUpdate = false,
      isFailed = isFailed,
      request.user.agentRef,
      ForeignPropertySelectCountryAudit(foreignPropertySelectCountry)
    )

    audit.sendAuditEvent(auditModel)
  }
}
